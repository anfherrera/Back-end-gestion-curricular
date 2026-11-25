package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.Correcciones;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.SoporteMvc;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.PeriodoAcademicoController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.TipoDocumentoController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;

/**
 * Fija las respuestas {success, data, message} de los catálogos de períodos y de tipos de
 * documento antes de tiparlas (ARQ-10), incluido el orden en que salen las claves, que la
 * igualdad de nodos no compara. Las listas y el período que dependen de la fecha se comparan
 * primero con el enum y luego se sustituyen por un marcador, para que la referencia no caduque.
 */
class CatalogosEnvueltosCaracterizacionTest {
    private final ObjectMapper json = new ObjectMapper();
    private final MockMvc mvc = SoporteMvc
        .standaloneSetup(new PeriodoAcademicoController(), new TipoDocumentoController())
        .setControllerAdvice(new RestApiExceptionHandler()).build();

    @Test
    void conservaLasRespuestasDelCatalogoDePeriodos() throws Exception {
        var actual = PeriodoAcademicoEnum.getPeriodoActual();
        assertThat(actual).as("la prueba supone una fecha dentro del catálogo").isNotNull();

        // EST-7 (paso 2): cada clave se pide ahora por su ruta nueva; /validar y /ultimo se retiraron (404)
        int año = java.time.Year.now().getValue();
        var resultados = json.createObjectNode();
        resultados.set("todos", respuesta("/api/periodos-academicos"));
        resultados.set("futuros", conDatoNormalizado(respuesta("/api/periodos-academicos?desde={a}", año),
            json.valueToTree(PeriodoAcademicoEnum.desde(Year.now().getValue())), "<periodos-futuros>"));
        resultados.set("recientes", conDatoNormalizado(
            respuesta("/api/periodos-academicos?desde={a}", Math.max(2020, año - 4)),
            json.valueToTree(PeriodoAcademicoEnum.getPeriodosRecientes()), "<periodos-recientes>"));
        resultados.set("actual", conDatoNormalizado(respuesta("/api/periodos-academicos/actual"),
            json.valueToTree(actual.getValor()), "<periodo-actual>"));
        resultados.set("ultimo", respuesta("/api/periodos-academicos/ultimo"));
        for (String periodo : new String[]{"2020-1", "2025-2", "2030-2", " 2025-1 ", "2025-3", "abc", "2019-1"}) {
            var caso = resultados.putObject(periodo);
            caso.set("validar", respuesta("/api/periodos-academicos/validar/{p}", periodo));
            caso.set("info", respuesta("/api/periodos-academicos/{p}", periodo));
        }
        comparar("periodos-academicos.json", resultados, "catalogos/correcciones/periodos-academicos-est7.json");
    }

    @Test
    void conservaLasRespuestasDelCatalogoDeTiposDeDocumento() throws Exception {
        var resultados = json.createObjectNode();
        resultados.set("todos", respuesta("/api/tipos-documento"));
        for (String codigo : new String[]{"CC", "NUIP", "cc", "XX"}) {
            var caso = resultados.putObject(codigo);
            caso.set("validar", respuesta("/api/tipos-documento/validar/{c}", codigo));
            caso.set("info", respuesta("/api/tipos-documento/{c}", codigo));
        }
        comparar("tipos-documento.json", resultados, "catalogos/correcciones/tipos-documento-est7.json");
    }

    private void comparar(String archivo, ObjectNode resultados, String correccion) throws Exception {
        var referencia = Path.of("src/test/resources/referencias/catalogos", archivo);
        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(referencia)) {
            Files.createDirectories(referencia.getParent());
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(resultados));
        }
        assertThat(resultados).isEqualTo(
            Correcciones.aplicar(json.readTree(Files.readString(referencia)), correccion));
    }

    private ObjectNode conDatoNormalizado(ObjectNode resultado, JsonNode esperado, String marcador) {
        var cuerpo = (ObjectNode) resultado.get("body");
        assertThat(cuerpo.get("data")).isEqualTo(esperado);
        cuerpo.put("data", marcador);
        return resultado;
    }

    private ObjectNode respuesta(String ruta, Object... variables) throws Exception {
        var respuesta = mvc.perform(get(ruta, variables)).andReturn().getResponse();
        var cuerpo = json.readTree(respuesta.getContentAsByteArray());
        var resultado = json.createObjectNode();
        resultado.put("status", respuesta.getStatus());
        resultado.set("body", cuerpo);
        resultado.set("claves", json.valueToTree(claves(cuerpo)));
        return resultado;
    }

    /** Claves del cuerpo y de su campo data, en el orden en que llegan. */
    private static List<String> claves(JsonNode cuerpo) {
        var claves = new ArrayList<String>();
        cuerpo.fieldNames().forEachRemaining(claves::add);
        if (cuerpo.path("data").isObject()) {
            cuerpo.get("data").fieldNames().forEachRemaining(campo -> claves.add("data." + campo));
        }
        return claves;
    }
}
