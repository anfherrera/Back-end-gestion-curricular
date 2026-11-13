package co.edu.unicauca.decanatura.gestion_curricular.verano;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.*;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.CupoAgotadoException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.CursoOfertadoRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladorExcepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.*;

/** Fija cómo {@code validar-pago} interpreta el cuerpo (ARQ-9), incluidos los tipos que hoy rechaza. */
class ValidarPagoCaracterizacionTest {
    private static final String RUTA = "/api/cursos-verano/inscripciones/7/validar-pago";

    @Test
    void conservaLaInterpretacionDelCuerpo() throws Exception {
        var cu = mock(GestionarSolicitudCursoVeranoCUIntPort.class);
        List<Object> llamadas = new ArrayList<>();
        var inscripcion = new SolicitudCursoVeranoIncripcion();
        inscripcion.setId_solicitud(7);
        when(cu.validarPago(anyInt(), anyBoolean(), nullable(String.class))).thenAnswer(i -> {
            Object[] argumentos = i.getArguments();
            llamadas.add(java.util.Arrays.asList(argumentos));
            if ("cupo".equals(argumentos[2])) {
                throw new CupoAgotadoException(3, 3);
            }
            return inscripcion;
        });
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new CursoOfertadoRestController(
                mock(GestionarCursoOfertadoVeranoCUIntPort.class), cu,
                mock(CursosOfertadosMapperDominio.class), mock(EstadoCursoOfertadoMapper.class)))
                .setControllerAdvice(new RestApiExceptionHandler()).build();
        String[] cuerpos = {"", "null", "{}", "[]", "\"x\"",
            "{\"esValido\":true}", "{\"esValido\":false,\"observaciones\":\"  nota  \"}",
            "{\"esValido\":null,\"observaciones\":\"x\"}", "{\"esValido\":\"true\"}", "{\"esValido\":1}",
            "{\"esValido\":{\"a\":1}}", "{\"esValido\":true,\"observaciones\":45}",
            "{\"esValido\":true,\"observaciones\":{\"a\":1}}", "{\"esValido\":true,\"observaciones\":null,\"extra\":[1]}",
            "{\"esValido\":true,\"observaciones\":\"cupo\"}", "{\"esValido\":false,\"observaciones\":\"cupo\"}"};
        var json = new ObjectMapper();
        Map<String, Object> resultados = new LinkedHashMap<>();
        for (String cuerpo : cuerpos) {
            llamadas.clear();
            var r = mvc.perform(put(RUTA).contentType(MediaType.APPLICATION_JSON).content(cuerpo)).andReturn().getResponse();
            Map<String, Object> caso = new LinkedHashMap<>();
            caso.put("status", r.getStatus());
            caso.put("body", r.getContentAsByteArray().length == 0 ? json.nullNode() : json.readTree(r.getContentAsByteArray()));
            caso.put("llamadas", new ArrayList<>(llamadas));
            resultados.put(cuerpo, caso);
        }
        var actual = json.valueToTree(resultados);
        Path referencia = Path.of("src/test/resources/referencias/verano-endpoints/validar-pago.json");
        if (Boolean.getBoolean("grabarReferencia")) {
            assertThat(Files.exists(referencia)).isFalse();
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(actual));
        }
        assertThat(actual).isEqualTo(json.readTree(Files.readString(referencia)));
    }
}
