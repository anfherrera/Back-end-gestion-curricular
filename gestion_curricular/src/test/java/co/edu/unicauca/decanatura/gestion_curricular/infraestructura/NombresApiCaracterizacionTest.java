package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GenerarDocumentosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.SoporteMvc;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort.DatosCreacion;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSalonesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.PlantillaDocumento;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.CursosVeranoRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.GeneradorDocumentosRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.CursosOfertadosDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.CursosOfertadosMapperDominio;

class NombresApiCaracterizacionTest {
    private final ObjectMapper json = new ObjectMapper();

    @Test
    void conservaDocumentosYEscriturasDeCursos() throws Exception {
        var documentos = mock(GenerarDocumentosCUIntPort.class);
        when(documentos.generarDocumento(7, "OFICIO", Map.of("numeroDocumento", "004"), Map.of("nombreEstudiante", "Ana Pérez")))
                .thenReturn(new byte[]{4, 2});
        when(documentos.getTemplates("reingreso")).thenReturn(
                List.of(new PlantillaDocumento("oficio", "Oficio", "Plantilla", List.of("id"), List.of())));

        var cursos = mock(GestionarCursoOfertadoVeranoCUIntPort.class);
        var mapper = mock(CursosOfertadosMapperDominio.class);
        var curso = new CursoOfertadoVerano();
        curso.setIdCurso(71);
        curso.setCupoEstimado(19);
        var dto = new CursosOfertadosDTORespuesta();
        dto.setNombre_curso("Materia");
        when(cursos.crearCurso(any(DatosCreacion.class))).thenReturn(curso);
        when(cursos.modificarCurso(71, 19, null, null)).thenReturn(curso);
        when(mapper.mapearDeCursoOfertadoARespuesta(curso)).thenReturn(dto);
        var mvc = SoporteMvc.standaloneSetup(new GeneradorDocumentosRestController(documentos),
                new CursosVeranoRestController(cursos, null, mapper, mock(GestionarSalonesCUIntPort.class), null))
                .setControllerAdvice(new RestApiExceptionHandler()).build();

        var resultados = json.createObjectNode();
        resultados.set("documento", respuesta(mvc, post("/api/documentos/generar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"idSolicitud":7,"tipoDocumento":"OFICIO","datosDocumento":{"numeroDocumento":"004"},
                     "datosSolicitud":{"nombreEstudiante":"Ana Pérez"}}
                    """)));
        resultados.set("plantillas", respuesta(mvc, get("/api/documentos/templates/reingreso")));
        resultados.set("crear", respuesta(mvc, post("/api/cursos-intersemestrales/cursos-verano")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"id_materia":4,"id_docente":5,"cupo_estimado":19,
                     "fecha_inicio":"2025-07-01","fecha_fin":"2025-07-15","periodoAcademico":"2025-2"}
                    """)));
        resultados.set("actualizar", respuesta(mvc, put("/api/cursos-intersemestrales/cursos-verano/71")
                .contentType(MediaType.APPLICATION_JSON).content("{\"cupo_estimado\":19}")));
        assertThat(resultados.path("documento").path("status").asInt()).isEqualTo(200);
        assertThat(resultados.path("actualizar").path("status").asInt()).isEqualTo(200);
        assertThat(resultados.path("crear").path("status").asInt()).isIn(200, 201);

        var referencia = Path.of("src/test/resources/referencias/nombres-api/escrituras-y-documentos.json");
        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(referencia)) {
            Files.createDirectories(referencia.getParent());
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(resultados));
        }
        assertThat(resultados).isEqualTo(json.readTree(Files.readString(referencia)));
    }

    private ObjectNode respuesta(MockMvc mvc, RequestBuilder peticion) throws Exception {
        var respuesta = mvc.perform(peticion).andReturn().getResponse();
        var resultado = json.createObjectNode().put("status", respuesta.getStatus());
        resultado.put("contentType", respuesta.getContentType());
        resultado.put("contentDisposition", respuesta.getHeader("Content-Disposition"));
        byte[] cuerpo = respuesta.getContentAsByteArray();
        if (MediaType.APPLICATION_OCTET_STREAM_VALUE.equals(respuesta.getContentType())) {
            return resultado.put("body", Base64.getEncoder().encodeToString(cuerpo));
        }
        var datos = json.readTree(cuerpo);
        if (datos instanceof ObjectNode objeto) {
            for (String campo : new String[]{"fecha_inicio", "fecha_fin", "timestamp"}) {
                if (objeto.hasNonNull(campo)) {
                    objeto.put(campo, "<fecha>");
                }
            }
        }
        return resultado.set("body", datos);
    }
}
