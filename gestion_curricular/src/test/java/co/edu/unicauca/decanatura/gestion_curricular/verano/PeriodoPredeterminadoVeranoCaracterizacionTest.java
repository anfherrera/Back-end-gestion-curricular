package co.edu.unicauca.decanatura.gestion_curricular.verano;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.PreinscripcionesVeranoRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.*;

class PeriodoPredeterminadoVeranoCaracterizacionTest {
    @Test
    void aperturaDeCursoConservaPeriodoActualYRespuesta() throws Exception {
        var cu = mock(GestionarSolicitudCursoVeranoCUIntPort.class);
        when(cu.crearSolicitudCursoVeranoPreinscripcion(any())).thenAnswer(i -> {
            SolicitudCursoVeranoPreinscripcion solicitud = i.getArgument(0);
            var actual = PeriodoAcademicoEnum.getPeriodoActual();
            assertThat(solicitud.getPeriodo_academico()).isEqualTo(actual == null ? null : actual.getValor());
            solicitud.setId_solicitud(7);
            return solicitud;
        });
        var mvc = MockMvcBuilders.standaloneSetup(new PreinscripcionesVeranoRestController(cu,
            mock(CursosOfertadosMapperDominio.class), mock(SolicitudCursoDeVeranoPreinscripcionMapperDominio.class))).build();
        var json = new ObjectMapper();
        Map<String, Object> resultados = new LinkedHashMap<>();
        for (String condicion : new String[]{"Primera_Vez", "INVALIDA"}) {
            var respuesta = mvc.perform(post("/api/cursos-intersemestrales/solicitudes-curso-nuevo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idUsuario\":31,\"nombreCompleto\":\"Estudiante\",\"codigo\":\"123\",\"curso\":\"Materia\",\"condicion\":\"" + condicion + "\"}"))
                .andReturn().getResponse();
            var body = json.readTree(respuesta.getContentAsByteArray());
            if (body.hasNonNull("fecha")) ((ObjectNode) body).put("fecha", "<ahora>");
            resultados.put(condicion, Map.of("status", respuesta.getStatus(), "body", body));
        }
        verify(cu).crearSolicitudCursoVeranoPreinscripcion(any());
        var actual = json.valueToTree(resultados);
        Path referencia = Path.of("src/test/resources/referencias/verano-endpoints/periodo-apertura.json");
        if (Boolean.getBoolean("grabarReferencia")) {
            assertThat(Files.exists(referencia)).isFalse();
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(actual));
        }
        assertThat(actual).isEqualTo(json.readTree(Files.readString(referencia)));
    }
}
