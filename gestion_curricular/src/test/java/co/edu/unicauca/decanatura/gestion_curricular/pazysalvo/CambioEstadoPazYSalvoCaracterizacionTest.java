package co.edu.unicauca.decanatura.gestion_curricular.pazysalvo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GenerarDocumentosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.SoporteMvc;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocumentosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudPazYSalvoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudPazYSalvoCUIntPort.RechazoCambioEstado;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudPazYSalvoCUIntPort.RechazoCambioEstado.Motivo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.SolicitudPazYSalvoRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.DocumentosMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudPazYSalvoMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.Autorizacion;

/**
 * Fija las respuestas de {@code PUT /api/solicitudes-pazysalvo/actualizarEstadoSolicitud} antes de tipar
 * el cuerpo de rechazo (ARQ-10): estado, cuerpo y orden de claves de las validaciones, de los dos
 * motivos de rechazo (con y sin estado actual) y del cambio permitido.
 */
class CambioEstadoPazYSalvoCaracterizacionTest {

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void conservaLasValidacionesYLosRechazos() throws Exception {
        var cu = mock(GestionarSolicitudPazYSalvoCUIntPort.class);
        var autorizacion = new Autorizacion(null, null, null, null) {
            @Override public boolean tieneRol(String rol) { return false; }
        };
        var controller = new SolicitudPazYSalvoRestController(cu, mock(SolicitudPazYSalvoMapperDominio.class),
                mock(GestionarArchivosCUIntPort.class), mock(GestionarDocumentosCUIntPort.class),
                mock(DocumentosMapperDominio.class), mock(GenerarDocumentosCUIntPort.class), json, autorizacion);
        MockMvc mvc = SoporteMvc.standaloneSetup(controller)
            .setControllerAdvice(new RestApiExceptionHandler()).build();

        // nombre, cuerpo de la petición, rechazo que devuelve el caso de uso (null = permitido)
        List<Object[]> casos = new ArrayList<>();
        casos.add(new Object[] {"sin id", "{\"nuevoEstado\":\"APROBADA\"}", null});
        casos.add(new Object[] {"sin estado", "{\"idSolicitud\":7}", null});
        casos.add(new Object[] {"estado en blanco", "{\"idSolicitud\":7,\"nuevoEstado\":\"  \"}", null});
        casos.add(new Object[] {"permitido", "{\"idSolicitud\":7,\"nuevoEstado\":\"APROBADA\"}", null});
        casos.add(new Object[] {"transicion no permitida", "{\"idSolicitud\":7,\"nuevoEstado\":\"APROBADA\"}",
            new RechazoCambioEstado(Motivo.TRANSICION_NO_PERMITIDA, "No se puede pasar", "ENVIADA")});
        casos.add(new Object[] {"rol no autorizado", "{\"idSolicitud\":7,\"nuevoEstado\":\"APROBADA\"}",
            new RechazoCambioEstado(Motivo.ROL_NO_AUTORIZADO, "Solo el Coordinador", "APROBADA_FUNCIONARIO")});
        casos.add(new Object[] {"rechazo sin estado actual", "{\"idSolicitud\":7,\"nuevoEstado\":\"APROBADA\"}",
            new RechazoCambioEstado(Motivo.TRANSICION_NO_PERMITIDA, "Estado desconocido", null)});
        casos.add(new Object[] {"rechazo sin mensaje", "{\"idSolicitud\":7,\"nuevoEstado\":\"APROBADA\"}",
            new RechazoCambioEstado(Motivo.ROL_NO_AUTORIZADO, null, "ENVIADA")});

        ObjectNode resultados = json.createObjectNode();
        for (Object[] caso : casos) {
            reset(cu);
            when(cu.cambiarEstadoSolicitud(anyInt(), anyString(), nullable(String.class), any()))
                .thenReturn(Optional.ofNullable((RechazoCambioEstado) caso[2]));
            var respuesta = mvc.perform(put("/api/solicitudes-pazysalvo/actualizarEstadoSolicitud")
                .contentType(MediaType.APPLICATION_JSON).content((String) caso[1])).andReturn().getResponse();
            byte[] cuerpo = respuesta.getContentAsByteArray();
            JsonNode body = cuerpo.length == 0 ? json.nullNode() : json.readTree(cuerpo);
            ObjectNode resultado = resultados.putObject((String) caso[0]);
            resultado.put("status", respuesta.getStatus());
            resultado.set("body", body);
            ObjectNode claves = resultado.putObject("claves");
            body.fields().forEachRemaining(campo -> claves.put(campo.getKey(), campo.getValue().getNodeType().name()));
        }

        Path referencia = Path.of("src/test/resources/referencias/pazysalvo/cambio-estado-rechazos.json");
        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(referencia)) {
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(resultados));
        }
        assertThat(resultados).isEqualTo(json.readTree(Files.readString(referencia)));
        // ObjectNode no compara el orden de las claves: se comprueba aparte
        JsonNode grabada = json.readTree(Files.readString(referencia));
        resultados.fields().forEachRemaining(caso -> assertThat(nombres(caso.getValue().get("claves")))
            .as(caso.getKey()).isEqualTo(nombres(grabada.get(caso.getKey()).get("claves"))));
    }

    private static List<String> nombres(JsonNode objeto) {
        List<String> nombres = new ArrayList<>();
        objeto.fieldNames().forEachRemaining(nombres::add);
        return nombres;
    }
}
