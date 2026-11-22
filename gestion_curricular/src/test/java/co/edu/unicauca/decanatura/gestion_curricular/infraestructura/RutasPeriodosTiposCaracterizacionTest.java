package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.PeriodoAcademicoController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.TipoDocumentoController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.SoporteMvc;

/**
 * Caracterización previa a la migración de rutas de EST-7 (paso 2): períodos académicos y tipos de documento.
 * Cada consulta se pide por su ruta antigua y por la nueva. Los datos que dependen de la fecha (períodos futuros,
 * recientes y actual) se comparan con el enum y se sustituyen por un marcador, y el año de {@code ?desde=} sale
 * en la clave como marcador, para que la referencia no caduque.
 */
class RutasPeriodosTiposCaracterizacionTest {

    private final ObjectMapper json = new ObjectMapper();
    private final MockMvc mvc = SoporteMvc
            .standaloneSetup(new PeriodoAcademicoController(), new TipoDocumentoController())
            .setControllerAdvice(new RestApiExceptionHandler()).build();

    @Test
    void periodos() throws Exception {
        PeriodoAcademicoEnum actual = PeriodoAcademicoEnum.getPeriodoActual();
        assertThat(actual).as("la prueba supone una fecha dentro del catálogo").isNotNull();
        int año = Year.now().getValue();
        String añoRecientes = String.valueOf(Math.max(2020, año - 4));
        Map<String, String> marcadores = Map.of(
                String.valueOf(año), "<año actual>",
                añoRecientes, "<año recientes>",
                actual.getValor(), "<periodo actual>");

        String b = "/api/periodos-academicos";
        ObjectNode casos = json.createObjectNode();
        // Antiguas
        for (String ruta : List.of("/todos", "/futuros", "/recientes", "/actual", "/ultimo")) {
            caso(casos, b + ruta, marcadores);
        }
        for (String periodo : List.of("2020-1", "2025-2", "2025-3", "abc", "2019-1")) {
            caso(casos, b + "/validar/" + periodo, marcadores);
            caso(casos, b + "/info/" + periodo, marcadores);
        }
        caso(casos, b + "/info/" + actual.getValor(), marcadores);
        // Convención de EST-7
        caso(casos, b, marcadores);
        caso(casos, b + "?desde=" + año, marcadores);
        caso(casos, b + "?desde=" + añoRecientes, marcadores);
        caso(casos, b + "?desde=abc", marcadores);
        for (String periodo : List.of("2020-1", "2025-2", "2025-3", "abc", "2019-1")) {
            caso(casos, b + "/" + periodo, marcadores);
        }
        caso(casos, b + "/" + actual.getValor(), marcadores);
        // EST-7: las rutas nuevas responden como las antiguas, que pasan a 404
        GrafoObjetos.compararConReferencia(casos, "rutas/periodos-academicos.json", "rutas/correcciones/periodos-academicos-est7.json");
    }

    @Test
    void tiposDocumento() throws Exception {
        String b = "/api/tipos-documento";
        ObjectNode casos = json.createObjectNode();
        caso(casos, b + "/todos", Map.of());
        for (String codigo : List.of("CC", "NUIP", "cc", "XX")) {
            caso(casos, b + "/validar/" + codigo, Map.of());
            caso(casos, b + "/info/" + codigo, Map.of());
        }
        caso(casos, b, Map.of());
        for (String codigo : List.of("CC", "NUIP", "cc", "XX")) {
            caso(casos, b + "/" + codigo, Map.of());
        }
        // EST-7: las rutas nuevas responden como las antiguas, que pasan a 404
        GrafoObjetos.compararConReferencia(casos, "rutas/tipos-documento.json", "rutas/correcciones/tipos-documento-est7.json");
    }

    /** Estado y cuerpo; los valores que dependen de la fecha se sustituyen por marcadores, en la clave y en el cuerpo. */
    private void caso(ObjectNode casos, String ruta, Map<String, String> marcadores) throws Exception {
        var respuesta = mvc.perform(get(ruta)).andReturn().getResponse();
        ObjectNode caso = json.createObjectNode();
        caso.put("status", respuesta.getStatus());
        caso.put("tipo", respuesta.getContentType());
        String texto = respuesta.getContentAsString(StandardCharsets.UTF_8);
        JsonNode cuerpo = texto.isEmpty() ? json.nullNode() : json.readTree(texto);
        PeriodoAcademicoEnum actual = PeriodoAcademicoEnum.getPeriodoActual();
        if (cuerpo.has("data")) {
            JsonNode data = cuerpo.get("data");
            String marcador = null;
            if (data.equals(json.valueToTree(PeriodoAcademicoEnum.getPeriodosFuturos()))) {
                marcador = "<periodos futuros>";
            } else if (data.equals(json.valueToTree(PeriodoAcademicoEnum.getPeriodosRecientes()))) {
                marcador = "<periodos recientes>";
            } else if (data.isTextual() && data.asText().equals(actual.getValor())) {
                marcador = "<periodo actual>";
            } else if (data.isObject() && actual.getValor().equals(data.path("valor").asText())) {
                assertThat(data).isEqualTo(json.valueToTree(Map.of("valor", actual.getValor(), "año", actual.getAño(),
                        "numeroPeriodo", actual.getNumeroPeriodo(), "descripcion", actual.getDescripcion())));
                marcador = "<detalle del periodo actual>";
            }
            if (marcador != null) {
                ((ObjectNode) cuerpo).put("data", marcador);
            }
        }
        // En la clave: el año de ?desde= y el período actual en la ruta, que cambian con la fecha
        String clave = "GET " + ruta;
        for (var m : marcadores.entrySet()) {
            if (clave.contains("?desde=" + m.getKey())) {
                clave = clave.replace("?desde=" + m.getKey(), "?desde=" + m.getValue());
            } else if (clave.endsWith("/" + m.getKey())) {
                clave = clave.substring(0, clave.length() - m.getKey().length()) + m.getValue();
            }
        }
        if (cuerpo.has("url")) {
            ((ObjectNode) cuerpo).put("url", cuerpo.get("url").asText().replace(actual.getValor(), "<periodo actual>"));
        }
        caso.set("cuerpo", cuerpo);
        casos.set(clave, caso);
    }
}
