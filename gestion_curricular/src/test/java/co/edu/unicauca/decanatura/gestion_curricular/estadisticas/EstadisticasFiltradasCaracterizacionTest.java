package co.edu.unicauca.decanatura.gestion_curricular.estadisticas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GenerarReportesEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.SoporteMvc;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.ConteoSolicitudes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPorEstado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPorPeriodo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPorProceso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPorPrograma;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.GlobalesContadas;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.EstadisticasRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.EstadisticaMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.Correcciones;

/**
 * Fija {@code /api/estadisticas/filtradas} y el 400 de {@code /periodo} antes de tiparlos (ARQ-10). Las respuestas
 * del caso de uso son fijas; {@code claves} lista cada clave con su ruta y tipo JSON en orden de escritura.
 *
 * <p>Las referencias se grabaron cuando el caso de uso devolvía mapas que mezclaban tipos. Desde ARQ-13 las consultas
 * por proceso, programa, estado y periodo, y las globales, devuelven records, que no admiten esas entradas (desglose
 * anidado, totales ausentes, claves extra): las correcciones {@code *-arq13.json} recogen solo lo que cambia por el
 * nuevo fixture. Desde DUP-8 {@code /periodo} ya no existe: las correcciones {@code *-dup8.json} fijan su 404.
 * Con datos reales lo fija {@code EstadisticasEndpointsCaracterizacionTest#consultas}.
 */
class EstadisticasFiltradasCaracterizacionTest {
    private static final long MARZO = 1710522000000L;

    private final ObjectMapper json = new ObjectMapper();

    /**
     * En el otro caso, {@code %20} dentro de la ruta se codifica de nuevo y llega como texto; aquí los blancos
     * llegan como parámetro.
     */
    @Test
    void conservaLosParametrosEnBlanco() throws Exception {
        MockMvc mvc = montar();
        ObjectNode resultados = json.createObjectNode();
        registrar(resultados, "proceso en blanco", mvc,
            get("/api/estadisticas/filtradas").param("nombreProceso", "  ").param("idPrograma", "2"));
        registrar(resultados, "estado en blanco", mvc,
            get("/api/estadisticas/filtradas").param("estado", " ").param("fechaInicio", "2024-01-01")
                .param("fechaFin", "2024-06-30"));
        registrar(resultados, "periodo academico con espacios", mvc,
            get("/api/estadisticas/periodo").param("periodoAcademico", " 2024-1 "));
        registrar(resultados, "periodo en blanco con inicio", mvc,
            get("/api/estadisticas/periodo").param("periodoAcademico", "  ").param("fechaInicio", "2024-01-01"));
        registrar(resultados, "periodo sin parametros", mvc, get("/api/estadisticas/periodo"));

        Path referencia = Path.of("src/test/resources/referencias/estadisticas-endpoints/filtradas-periodo-blancos.json");
        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(referencia)) {
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(resultados));
        }
        // DUP-8: /periodo se retira y responde 404
        assertThat(resultados).isEqualTo(Correcciones.aplicar(Correcciones.aplicar(
            json.readTree(Files.readString(referencia)),
            "estadisticas-endpoints/correcciones/filtradas-periodo-blancos-arq13.json"),
            "estadisticas-endpoints/correcciones/filtradas-periodo-blancos-dup8.json"));
    }

    @Test
    void conservaFiltradasYElRechazoDelPeriodo() throws Exception {
        MockMvc mvc = montar();
        ObjectNode resultados = json.createObjectNode();

        String base = "/api/estadisticas/filtradas";
        registrar(resultados, "proceso", mvc, base + "?nombreProceso=Reingreso&idPrograma=1");
        registrar(resultados, "proceso en blanco", mvc, base + "?nombreProceso=%20%20&idPrograma=2");
        registrar(resultados, "estado", mvc, base + "?idPrograma=0&estado=APROBADA");
        registrar(resultados, "fechas", mvc, base + "?fechaInicio=2024-01-01&fechaFin=2024-06-30");
        registrar(resultados, "solo inicio", mvc, base + "?fechaInicio=2024-01-01");
        registrar(resultados, "sin filtros", mvc, base);

        String periodo = "/api/estadisticas/periodo";
        registrar(resultados, "periodo academico", mvc, periodo + "?periodoAcademico=%202024-1%20");
        registrar(resultados, "periodo fechas", mvc, periodo + "?fechaInicio=2024-01-01&fechaFin=2024-06-30");
        registrar(resultados, "periodo sin datos", mvc, periodo + "?periodoAcademico=%20&fechaInicio=2024-01-01");

        Path referencia = Path.of("src/test/resources/referencias/estadisticas-endpoints/filtradas-periodo.json");
        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(referencia)) {
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(resultados));
        }
        // DUP-8: /periodo se retira y responde 404
        assertThat(resultados).isEqualTo(Correcciones.aplicar(Correcciones.aplicar(
            json.readTree(Files.readString(referencia)),
            "estadisticas-endpoints/correcciones/filtradas-periodo-arq13.json"),
            "estadisticas-endpoints/correcciones/filtradas-periodo-dup8.json"));
    }

    private MockMvc montar() {
        var cu = mock(GestionarEstadisticasCUIntPort.class);
        when(cu.obtenerEstadisticasPorProceso(anyString())).thenAnswer(i -> new EstadisticasPorProceso(
            conteo(10, 4, 66.7), new Date(MARZO), i.getArgument(0), "proceso"));
        when(cu.obtenerEstadisticasPorPrograma(anyInt())).thenAnswer(i -> new EstadisticasPorPrograma(
            conteo(8, 2, 25.0), new Date(MARZO), i.getArgument(0)));
        // Casi vacío: solo el total, sin desgloses ni fecha
        when(cu.obtenerEstadisticasPorEstado(anyString())).thenAnswer(i -> new EstadisticasPorEstado(
            3, 0, 0, 0, 0, 0.0, null, null, null, null, i.getArgument(0), null));
        when(cu.obtenerEstadisticasPorPeriodo(any(Date.class), any(Date.class))).thenAnswer(i -> new EstadisticasPorPeriodo(
            conteo(5, 5, 100.0), new Date(MARZO), i.getArgument(0), i.getArgument(1)));
        when(cu.obtenerEstadisticasGlobales()).thenAnswer(i -> new GlobalesContadas(
            0, 0, 1, 2, 0, 0.0, porEstado(), porTipoProceso(), porPrograma(), new Date(MARZO)));
        return SoporteMvc.standaloneSetup(new EstadisticasRestController(cu,
                mock(EstadisticaMapperDominio.class), mock(GenerarReportesEstadisticasCUIntPort.class)))
            .setControllerAdvice(new RestApiExceptionHandler()).build();
    }

    private void registrar(ObjectNode resultados, String nombre, MockMvc mvc, String ruta) throws Exception {
        registrar(resultados, nombre, mvc, get(ruta));
    }

    private void registrar(ObjectNode resultados, String nombre, MockMvc mvc, RequestBuilder peticion)
            throws Exception {
        var respuesta = mvc.perform(peticion).andReturn().getResponse();
        byte[] cuerpo = respuesta.getContentAsByteArray();
        JsonNode body = cuerpo.length == 0 ? json.nullNode() : json.readTree(cuerpo);
        ObjectNode resultado = resultados.putObject(nombre);
        resultado.put("status", respuesta.getStatus());
        resultado.set("body", body);
        ArrayNode claves = resultado.putArray("claves");
        recorrer(body, "", claves);
    }

    private static void recorrer(JsonNode nodo, String ruta, ArrayNode claves) {
        if (nodo.isObject()) {
            nodo.fields().forEachRemaining(campo -> {
                String hija = ruta + "." + campo.getKey();
                claves.add(hija + ":" + campo.getValue().getNodeType());
                recorrer(campo.getValue(), hija, claves);
            });
        } else if (nodo.isArray()) {
            for (int i = 0; i < nodo.size(); i++) {
                recorrer(nodo.get(i), ruta + "[" + i + "]", claves);
            }
        }
    }

    /** Conteo con 1 rechazada, 2 en proceso y los desgloses fijos de abajo. */
    private static ConteoSolicitudes conteo(int total, int aprobadas, double porcentaje) {
        return new ConteoSolicitudes(total, aprobadas, 1, 2, 0, porcentaje, porEstado(), porTipoProceso(),
            porPrograma());
    }

    private static Map<String, Integer> porEstado() {
        Map<String, Integer> porEstado = new HashMap<>();
        porEstado.put("APROBADA", 2);
        porEstado.put("RECHAZADA", 1);
        return porEstado;
    }

    private static Map<String, Integer> porTipoProceso() {
        Map<String, Integer> porTipo = new HashMap<>();
        porTipo.put("Reingreso", 3);
        return porTipo;
    }

    private static Map<String, Integer> porPrograma() {
        Map<String, Integer> porPrograma = new HashMap<>();
        porPrograma.put("Sistemas", 4);
        return porPrograma;
    }
}
