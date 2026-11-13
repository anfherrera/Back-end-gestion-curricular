package co.edu.unicauca.decanatura.gestion_curricular.estadisticas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GenerarReportesEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.EstadisticasRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.EstadisticaMapperDominio;

class EstadisticasEscrituraCaracterizacionTest {
    @Test
    void conservaCamposNulosAusentesYOrdenDeListas() throws Exception {
        var cu = mock(GestionarEstadisticasCUIntPort.class);
        when(cu.crearEstadistica(any())).thenAnswer(i -> i.getArgument(0));
        when(cu.actualizarEstadistica(any())).thenAnswer(i -> i.getArgument(0));
        var mvc = MockMvcBuilders.standaloneSetup(new EstadisticasRestController(cu,
                Mappers.getMapper(EstadisticaMapperDominio.class),
                mock(GenerarReportesEstadisticasCUIntPort.class))).build();
        var json = new ObjectMapper();
        Map<String, Object> resultados = new LinkedHashMap<>();
        String[] cuerpos = {
            "{}",
            "{\"nombre\":null,\"periodos_academico\":null,\"nombres_procesos\":null,\"nombres_programas\":null}",
            "{\"id_estadistica\":4,\"nombre\":\"\",\"total_solicitudes\":-1,\"total_aprobadas\":7,\"total_rechazadas\":2,\"periodos_academico\":[0,86400000],\"nombres_procesos\":[\"B\",\"A\"],\"nombres_programas\":[\"Z\",\"X\"],\"extra\":true}"
        };
        for (String metodo : new String[]{"POST", "PUT"}) {
            for (int i = 0; i < cuerpos.length; i++) {
                var request = metodo.equals("POST") ? MockMvcRequestBuilders.post("/api/estadisticas/crear")
                        : MockMvcRequestBuilders.put("/api/estadisticas/actualizar");
                var respuesta = mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(cuerpos[i]))
                        .andReturn().getResponse();
                resultados.put(metodo + i, Map.of("status", respuesta.getStatus(),
                        "contentType", respuesta.getContentType(),
                        "body", json.readTree(respuesta.getContentAsByteArray())));
            }
        }
        JsonNode actual = json.valueToTree(resultados);
        Path referencia = Path.of("src/test/resources/referencias/estadisticas-endpoints/escritura.json");
        if (Boolean.getBoolean("grabarReferencia")) {
            assertThat(Files.exists(referencia)).isFalse();
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(actual));
        }
        assertThat(actual).isEqualTo(json.readTree(Files.readString(referencia)));
    }
}
