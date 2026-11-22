package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudEcaesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.SoporteMvc;
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
import co.edu.unicauca.decanatura.gestion_curricular.soporte.Correcciones;

/**
 * Fija la respuesta de los cambios de estado de ECAES, Homologación y Reingreso cuando la petición no trae un
 * {@code nuevoEstado} útil (BUG-28): sin la clave, null, vacío y solo espacios. La solicitud está en ENVIADA y
 * la pide un Funcionario, que es el rol de los pasos desde ENVIADA en los tres flujos.
 */
class CambioEstadoSinEstadoCaracterizacionTest {

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void respondeAlCambioSinEstado() throws Exception {
        var auth = new Autorizacion(null, null, null, null) {
            @Override public boolean tieneRol(String rol) { return "Funcionario".equals(rol); }
        };
        var validador = new ValidadorCambioEstado(auth);
        var mapper = new SolicitudMapperDominioImpl();
        var ecaes = mock(GestionarSolicitudEcaesCUIntPort.class);
        when(ecaes.buscarPorId(anyInt())).thenAnswer(i -> enviada(SolicitudEcaes::new));
        var homologacion = mock(GestionarSolicitudHomologacionCUIntPort.class);
        when(homologacion.buscarPorId(anyInt())).thenAnswer(i -> enviada(SolicitudHomologacion::new));
        var reingreso = mock(GestionarSolicitudReingresoCUIntPort.class);
        when(reingreso.obtenerSolicitudReingresoPorId(anyInt())).thenAnswer(i -> enviada(SolicitudReingreso::new));
        MockMvc mvc = SoporteMvc.standaloneSetup(
                new SolicitudEcaesRestController(ecaes, null, mapper, auth, validador),
                new SolicitudHomologacionRestController(homologacion, null, mapper, null, null, auth, validador),
                new SolicitudReingresoRestController(null, reingreso, mapper, null, auth, validador))
            .setControllerAdvice(new RestApiExceptionHandler()).build();

        // {clave de la referencia, ruta que se pide}: ECAES pasó a PUT /{id}/estado (EST-7, paso 6)
        String[][] rutas = {
            {"/api/solicitudes-ecaes/actualizarEstadoSolicitud", "/api/solicitudes-ecaes/3/estado"},
            {"/api/solicitudes-homologacion/actualizarEstadoSolicitud", "/api/solicitudes-homologacion/actualizarEstadoSolicitud"},
            {"/api/solicitudes-reingreso/actualizarEstadoSolicitud-Reingreso", "/api/solicitudes-reingreso/actualizarEstadoSolicitud-Reingreso"},
        };
        String[][] cuerpos = {
            {"sin clave", "{\"idSolicitud\":3}"},
            {"null", "{\"idSolicitud\":3,\"nuevoEstado\":null}"},
            {"vacio", "{\"idSolicitud\":3,\"nuevoEstado\":\"\"}"},
            {"espacios", "{\"idSolicitud\":3,\"nuevoEstado\":\"   \"}"},
        };
        ObjectNode resultados = json.createObjectNode();
        for (String[] claveYRuta : rutas) {
            String ruta = claveYRuta[1];
            ObjectNode porRuta = resultados.putObject(claveYRuta[0]);
            for (String[] cuerpo : cuerpos) {
                var respuesta = mvc.perform(put(ruta).contentType(MediaType.APPLICATION_JSON).content(cuerpo[1]))
                    .andReturn().getResponse();
                byte[] contenido = respuesta.getContentAsByteArray();
                JsonNode body = contenido.length == 0 ? json.nullNode() : json.readTree(contenido);
                ObjectNode resultado = porRuta.putObject(cuerpo[0]);
                resultado.put("status", respuesta.getStatus());
                resultado.set("body", body);
                List<String> claves = new ArrayList<>();
                body.fieldNames().forEachRemaining(claves::add);
                resultado.set("claves", json.valueToTree(claves));
            }
        }

        Path referencia = Path.of("src/test/resources/referencias/errores/cambio-estado-sin-estado.json");
        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(referencia)) {
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(resultados));
        }
        // BUG-28: cambio-estado-sin-estado.json fija el paso a ENVIADA anterior; la corrección, ESTADO_REQUERIDO
        assertThat(resultados).isEqualTo(Correcciones.aplicar(json.readTree(Files.readString(referencia)),
            "errores/correcciones/cambio-estado-sin-estado-bug28.json"));
    }

    private static <T extends Solicitud> T enviada(Supplier<T> nueva) {
        T solicitud = nueva.get();
        List<EstadoSolicitud> estados = new ArrayList<>();
        var estado = new EstadoSolicitud();
        estado.setEstadoActual("ENVIADA");
        estados.add(estado);
        solicitud.setEstadosSolicitud(estados);
        return solicitud;
    }
}
