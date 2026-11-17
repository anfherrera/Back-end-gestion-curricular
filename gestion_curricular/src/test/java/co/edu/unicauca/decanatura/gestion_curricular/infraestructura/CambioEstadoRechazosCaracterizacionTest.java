package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudEcaesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudHomologacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudReingresoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.SolicitudEcaesRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.SolicitudHomologacionRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.SolicitudReingresoRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion.ValidadorCambioEstado;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.Autorizacion;

/**
 * Fija las respuestas de los cambios de estado de ECAES, Homologación y Reingreso antes de tipar
 * el cuerpo de rechazo de {@link ValidadorCambioEstado} (ARQ-10): estado, cuerpo y orden de claves.
 * Las listas de estados de los mensajes salen de {@code Set.of}/{@code Map.of}, cuyo orden cambia
 * entre arranques de la JVM (BUG-27), así que se ordenan antes de comparar.
 */
class CambioEstadoRechazosCaracterizacionTest {
    private static final Pattern LISTA = Pattern.compile("\\[([^\\]]*)\\]");

    private final ObjectMapper json = new ObjectMapper();
    private String estadoActual;
    private Set<String> roles;

    @Test
    void conservaLosRechazosYLosCambiosPermitidos() throws Exception {
        var auth = new Autorizacion(null, null, null, null) {
            @Override public boolean tieneRol(String rol) { return roles.contains(rol); }
        };
        var validador = new ValidadorCambioEstado(auth);
        var mapper = new SolicitudMapperDominioImpl();

        var ecaes = mock(GestionarSolicitudEcaesCUIntPort.class);
        when(ecaes.buscarPorId(anyInt())).thenAnswer(i -> conEstado(SolicitudEcaes::new));
        var homologacion = mock(GestionarSolicitudHomologacionCUIntPort.class);
        when(homologacion.buscarPorId(anyInt())).thenAnswer(i -> conEstado(SolicitudHomologacion::new));
        var reingreso = mock(GestionarSolicitudReingresoCUIntPort.class);
        when(reingreso.obtenerSolicitudReingresoPorId(anyInt())).thenAnswer(i -> conEstado(SolicitudReingreso::new));

        MockMvc mvc = MockMvcBuilders.standaloneSetup(
                new SolicitudEcaesRestController(ecaes, null, mapper, auth, validador),
                new SolicitudHomologacionRestController(homologacion, null, mapper, null, null, auth, validador),
                new SolicitudReingresoRestController(null, reingreso, mapper, null, auth, validador))
            .setControllerAdvice(new RestApiExceptionHandler()).build();

        String[][] casos = {
            // actual, nuevo, roles
            {"ENVIADA", "APROBADA_FUNCIONARIO", "Funcionario"},
            {"Enviada", "aprobada funcionario", "Funcionario"},
            {"ENVIADA", "APROBADA_FUNCIONARIO", "Coordinador"},
            {"ENVIADA", "APROBADA_FUNCIONARIO", "Administrador"},
            {"ENVIADA", "APROBADA", "Funcionario"},
            {"ENVIADA", "PRE_REGISTRADO", "Funcionario"},
            {"ENVIADA", "PRE_REGISTRADO", "Coordinador"},
            {"aprobada_funcionario", "APROBADA_COORDINADOR", "Funcionario"},
            {"APROBADA", "RECHAZADA", "Administrador"},
            {"PRE_REGISTRADO", "RECHAZADA", "Funcionario"},
            {"ENVIADA", "XYZ", "Funcionario"},
            {"ENVIADA", null, "Funcionario"},
            {null, "RECHAZADA", "Funcionario"},
            {"Desconocido", "RECHAZADA", "Funcionario"},
        };
        String[] rutas = {
            "/api/solicitudes-ecaes/actualizarEstadoSolicitud",
            "/api/solicitudes-homologacion/actualizarEstadoSolicitud",
            "/api/solicitudes-reingreso/actualizarEstadoSolicitud-Reingreso",
        };

        ObjectNode resultados = json.createObjectNode();
        for (String ruta : rutas) {
            ObjectNode porRuta = resultados.putObject(ruta);
            for (String[] caso : casos) {
                estadoActual = caso[0];
                roles = Set.of(caso[2]);
                ObjectNode peticion = json.createObjectNode();
                peticion.put("idSolicitud", 3);
                peticion.put("nuevoEstado", caso[1]);
                var respuesta = mvc.perform(put(ruta).contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(peticion))).andReturn().getResponse();
                byte[] cuerpo = respuesta.getContentAsByteArray();
                JsonNode body = cuerpo.length == 0 ? json.nullNode() : json.readTree(cuerpo);
                ObjectNode resultado = porRuta.putObject(caso[0] + " -> " + caso[1] + " como " + caso[2]);
                resultado.put("status", respuesta.getStatus());
                resultado.set("body", normalizar(body));
                List<String> claves = new ArrayList<>();
                body.fieldNames().forEachRemaining(claves::add);
                resultado.set("claves", json.valueToTree(claves));
            }
        }

        Path referencia = Path.of("src/test/resources/referencias/errores/cambio-estado-rechazos.json");
        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(referencia)) {
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(resultados));
        }
        assertThat(resultados).isEqualTo(json.readTree(Files.readString(referencia)));
    }

    private <T extends Solicitud> T conEstado(Supplier<T> nueva) {
        T solicitud = nueva.get();
        List<EstadoSolicitud> estados = new ArrayList<>();
        var estado = new EstadoSolicitud();
        estado.setEstado_actual(estadoActual);
        estados.add(estado);
        solicitud.setEstadosSolicitud(estados);
        return solicitud;
    }

    /** Ordena el contenido de cada lista "[A, B]" de los textos. */
    private JsonNode normalizar(JsonNode body) {
        if (body instanceof ObjectNode objeto) {
            objeto.fields().forEachRemaining(campo -> {
                if (campo.getValue().isTextual()) {
                    Matcher m = LISTA.matcher(campo.getValue().asText());
                    StringBuilder ordenado = new StringBuilder();
                    while (m.find()) {
                        String[] elementos = m.group(1).split(", ");
                        Arrays.sort(elementos);
                        m.appendReplacement(ordenado, Matcher.quoteReplacement("[" + String.join(", ", elementos) + "]"));
                    }
                    m.appendTail(ordenado);
                    objeto.put(campo.getKey(), ordenado.toString());
                }
            });
        }
        return body;
    }
}
