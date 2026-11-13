package co.edu.unicauca.decanatura.gestion_curricular.verano;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.*;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.*;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladorExcepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.*;

class DecisionesVeranoCaracterizacionTest {
    @Test
    void cuerposDeDecisionConservanCoercionesYValidaciones() throws Exception {
        var cu = mock(GestionarSolicitudCursoVeranoCUIntPort.class);
        var pre = new SolicitudCursoVeranoPreinscripcion();
        pre.setId_solicitud(7);
        when(cu.aprobarPreinscripcion(anyInt(), nullable(String.class))).thenReturn(pre);
        when(cu.rechazarPreinscripcion(anyInt(), nullable(String.class))).thenReturn(pre);
        var curso = mock(GestionarCursoOfertadoVeranoCUIntPort.class);
        var mvc = MockMvcBuilders.standaloneSetup(
                new CursoOfertadoRestController(curso, cu, mock(CursosOfertadosMapperDominio.class), mock(EstadoCursoOfertadoMapper.class)),
                new PreinscripcionesVeranoRestController(cu, mock(CursosOfertadosMapperDominio.class), mock(SolicitudCursoDeVeranoPreinscripcionMapperDominio.class)),
                new InscripcionesVeranoRestController(curso, cu, mock(GestionarArchivosCUIntPort.class), mock(GestionarSolicitudCUIntPort.class)))
                .setControllerAdvice(new RestApiExceptionHandler()).build();
        String[] rutas = {
            "/api/cursos-verano/preinscripciones/7/aprobar",
            "/api/cursos-verano/preinscripciones/7/rechazar",
            "/api/cursos-intersemestrales/preinscripciones/7/aprobar",
            "/api/cursos-intersemestrales/preinscripciones/7/rechazar",
            "/api/cursos-intersemestrales/preinscripciones/7/observaciones",
            "/api/cursos-intersemestrales/inscripciones/7/aceptar",
            "/api/cursos-intersemestrales/inscripciones/7/rechazar"
        };
        String[] cuerpos = {"", "null", "{}", "{\"motivo\":null,\"comentarios\":null,\"observaciones\":null}",
            "{\"motivo\":\"  prueba  \",\"comentarios\":\"  nota  \",\"observaciones\":\"  texto  \"}",
            "{\"motivo\":12,\"comentarios\":false,\"observaciones\":45}",
            "{\"extra\":{\"x\":1}}", "[]"};
        var json = new ObjectMapper();
        Map<String, Object> resultados = new LinkedHashMap<>();
        for (String ruta : rutas) {
            for (int i = 0; i < cuerpos.length; i++) {
                var respuesta = mvc.perform(put(ruta).contentType(MediaType.APPLICATION_JSON).content(cuerpos[i])).andReturn().getResponse();
                resultados.put(ruta + " #" + i, Map.of("status", respuesta.getStatus(),
                    "body", respuesta.getContentAsByteArray().length == 0 ? json.nullNode() : json.readTree(respuesta.getContentAsByteArray())));
            }
        }
        var actual = json.valueToTree(resultados);
        Path referencia = Path.of("src/test/resources/referencias/verano-endpoints/decisiones.json");
        if (Boolean.getBoolean("grabarReferencia")) {
            assertThat(Files.exists(referencia)).isFalse();
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(actual));
        }
        assertThat(actual).isEqualTo(json.readTree(Files.readString(referencia)));
    }
}
