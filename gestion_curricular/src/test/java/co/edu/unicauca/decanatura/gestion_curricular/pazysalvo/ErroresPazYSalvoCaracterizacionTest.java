package co.edu.unicauca.decanatura.gestion_curricular.pazysalvo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unicauca.decanatura.gestion_curricular.Security.Autorizacion;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.*;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.*;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.SolicitudPazYSalvoRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladorExcepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.*;

class ErroresPazYSalvoCaracterizacionTest {
    @Test
    void conservaErroresLocalesYErroresFueraDeLaOperacion() throws Exception {
        var cu = mock(GestionarSolicitudPazYSalvoCUIntPort.class);
        var autorizacion = new Autorizacion(null, null, null, null) {
            @Override public boolean esStaff() { return true; }
        };
        var json = new ObjectMapper();
        var controller = new SolicitudPazYSalvoRestController(cu, mock(SolicitudPazYSalvoMapperDominio.class),
                mock(GestionarArchivosCUIntPort.class), mock(GestionarDocumentosCUIntPort.class),
                mock(DocumentosMapperDominio.class), mock(GenerarDocumentosCUIntPort.class), json, autorizacion);
        var mvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new RestApiExceptionHandler()).build();
        RuntimeException[] errores = {new ReglaNegocioExcepcion("regla"), new IllegalArgumentException("argumento"),
            new EntidadNoExisteException("inexistente"), new RuntimeException("consulta"),
            new AccessDeniedException("denegado")};
        Map<String, Object> resultados = new LinkedHashMap<>();
        for (int i = 0; i < errores.length; i++) {
            reset(cu);
            when(cu.guardar(any())).thenThrow(errores[i]);
            when(cu.cambiarEstadoSolicitud(anyInt(), anyString(), nullable(String.class), any())).thenThrow(errores[i]);
            var crear = mvc.perform(post("/api/solicitudes-pazysalvo/crearSolicitud-PazYSalvo")
                .contentType(MediaType.APPLICATION_JSON).content("{\"idUsuario\":7}")).andReturn().getResponse();
            resultados.put("crear #" + i, Map.of("status", crear.getStatus(), "body", json.readTree(crear.getContentAsByteArray())));
            var estado = mvc.perform(put("/api/solicitudes-pazysalvo/actualizarEstadoSolicitud")
                .contentType(MediaType.APPLICATION_JSON).content("{\"idSolicitud\":7,\"nuevoEstado\":\"APROBADA\"}"))
                .andReturn().getResponse();
            resultados.put("estado #" + i, Map.of("status", estado.getStatus(), "body",
                estado.getContentAsByteArray().length == 0 ? json.nullNode() : json.readTree(estado.getContentAsByteArray())));
        }
        for (String cuerpo : new String[]{"", "null", "{"}) {
            var respuesta = mvc.perform(post("/api/solicitudes-pazysalvo/crearSolicitud-PazYSalvo")
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo)).andReturn().getResponse();
            resultados.put("binding " + cuerpo, Map.of("status", respuesta.getStatus(), "body", json.readTree(respuesta.getContentAsByteArray())));
        }
        var actual = json.valueToTree(resultados);
        Path referencia = Path.of("src/test/resources/referencias/pazysalvo/errores-operaciones.json");
        if (Boolean.getBoolean("grabarReferencia")) {
            assertThat(Files.exists(referencia)).isFalse();
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(actual));
        }
        assertThat(actual).isEqualTo(json.readTree(Files.readString(referencia)));
    }
}
