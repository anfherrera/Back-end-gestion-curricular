package co.edu.unicauca.decanatura.gestion_curricular.estadisticas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarEstadisticasCUIntPort;

/**
 * Fija el código HTTP y el JSON de los endpoints de {@code EstadisticasRestController} que usan las reglas que
 * ARQ-6 saca del controlador: {@code normalizarPeriodoAcademico}, {@code normalizarProceso},
 * {@code aplicarKpisDelProceso} (y su copia en {@code /resumen-completo}), {@code calcularTasaProcesamiento} y
 * {@code calcularEficienciaAprobacion}.
 *
 * <p>A diferencia de {@link EstadisticasCaracterizacionTest}, que llama al caso de uso, aquí se pasa por el
 * controlador con MockMvc. El caso de uso es el real, sobre el mismo conjunto fijo de datos
 * ({@link EstadisticasCaracterizacionTest#crearCasoDeUso()}), así que las respuestas no dependen de lo que otras
 * pruebas dejen en la base.
 *
 * <p>Antes de comparar se ordenan los campos de cada objeto y se reemplazan por {@code "<ahora>"} las fechas que
 * dependen del momento de la consulta ({@code fechaConsulta}, {@code fechaGeneracion}, {@code fechaPrediccion}),
 * y en los PDF y Excel la línea "Fecha de generación". El orden de las listas JSON sí se compara; el de las
 * líneas del PDF y las filas del Excel no (ver {@link #ordenadas}).
 *
 * <p>Referencias en {@code src/test/resources/referencias/estadisticas-endpoints/}. Para regrabarlas tras un
 * cambio intencional: {@code mvnw test -Dtest=EstadisticasEndpointsCaracterizacionTest -DgrabarReferencia=true}
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Estadísticas - endpoints fijados (ARQ-6)")
class EstadisticasEndpointsCaracterizacionTest {

    private static final Path CARPETA = Path.of("src/test/resources/referencias/estadisticas-endpoints");
    private static final ObjectMapper JSON = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final String BASE = "/api/estadisticas";
    private static final Set<String> FECHAS_DE_CONSULTA = Set.of("fechaConsulta", "fechaGeneracion", "fechaPrediccion");
    private static final String FECHA_GENERACION = "Fecha de generación:";

    /** Valores de periodoAcademico que recorren cada rama de la normalización (null = sin el parámetro). */
    private static final String[] PERIODOS = {
            null, "", "   ", "2025-1", " 2025-2 ", "2025 - Primer Semestre", "Segundo Período 2025",
            "primer semestre 1999", "2025-3", "sin año" };

    /** Valores de proceso que recorren cada rama de la normalización (null = sin el parámetro). */
    private static final String[] PROCESOS = {
            null, "  ", "Reingreso", "homologación", "HOMOLOGACION", "paz_y_salvo", "ECAES", "Curso Intersemestral",
            "CURSO_VERANO", "verano", "homolog", "Desconocido" };

    @TestConfiguration
    static class CasoDeUsoConDatosFijos {
        @Bean
        @Primary
        GestionarEstadisticasCUIntPort estadisticasConDatosFijos() {
            return EstadisticasCaracterizacionTest.crearCasoDeUso();
        }
    }

    @Autowired private MockMvc mockMvc;

    private TimeZone zonaOriginal;

    @BeforeAll
    void fijarZona() {
        // Los meses y periodos se calculan con la zona del sistema (igual que en EstadisticasCaracterizacionTest)
        zonaOriginal = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
    }

    @AfterAll
    void restaurarZona() {
        TimeZone.setDefault(zonaOriginal);
    }

    // ==================== /globales ====================

    @Test
    @DisplayName("/globales: periodo y proceso")
    void globales() throws Exception {
        Map<String, JsonNode> r = new LinkedHashMap<>();
        for (String periodo : PERIODOS) {
            consultar(r, "/globales", "periodoAcademico", periodo);
        }
        for (String proceso : PROCESOS) {
            consultar(r, "/globales", "periodoAcademico", "2025-2", "proceso", proceso);
        }
        // Sin periodo: el proceso llega tal cual al caso de uso
        consultar(r, "/globales", "proceso", "Homologacion", "idPrograma", "1");
        consultar(r, "/globales", "proceso", "verano");
        // Con periodo y programa
        consultar(r, "/globales", "periodoAcademico", "2025-1", "idPrograma", "1", "proceso", "Paz y Salvo");
        consultar(r, "/globales", "periodoAcademico", "2025-1", "idPrograma", "3", "proceso", "Reingreso");
        compararConReferencia("globales", r);
    }

    // ==================== /resumen-completo ====================

    @Test
    @DisplayName("/resumen-completo: periodo y proceso")
    void resumenCompleto() throws Exception {
        Map<String, JsonNode> r = new LinkedHashMap<>();
        for (String periodo : PERIODOS) {
            consultar(r, "/resumen-completo", "periodoAcademico", periodo);
        }
        for (String proceso : PROCESOS) {
            consultar(r, "/resumen-completo", "proceso", proceso);
        }
        consultar(r, "/resumen-completo", "periodoAcademico", "2025-2", "idPrograma", "1", "proceso", "Cursos de verano");
        consultar(r, "/resumen-completo", "periodoAcademico", "2025-1", "idPrograma", "2", "proceso", "homologacion");
        compararConReferencia("resumen-completo", r);
    }

    // ==================== /dashboard ====================

    @Test
    @DisplayName("/dashboard: periodo, programa y proceso")
    void dashboard() throws Exception {
        Map<String, JsonNode> r = new LinkedHashMap<>();
        for (String periodo : PERIODOS) {
            consultar(r, "/dashboard", "periodoAcademico", periodo);
        }
        for (String proceso : PROCESOS) {
            consultar(r, "/dashboard", "periodoAcademico", "2025-2", "proceso", proceso);
        }
        // Sin periodo ni programa: el proceso llega tal cual a las estadísticas globales
        consultar(r, "/dashboard", "proceso", "ECAES");
        consultar(r, "/dashboard", "proceso", "Homologacion");
        // Solo programa (con y sin proceso)
        consultar(r, "/dashboard", "idPrograma", "2");
        consultar(r, "/dashboard", "idPrograma", "2", "proceso", "Homologación");
        compararConReferencia("dashboard", r);
    }

    // ==================== /rendimiento ====================

    @Test
    @DisplayName("/rendimiento: tasa de procesamiento y eficiencia de aprobación")
    void rendimiento() throws Exception {
        Map<String, JsonNode> r = new LinkedHashMap<>();
        consultar(r, "/rendimiento");
        compararConReferencia("rendimiento", r);
    }

    // ==================== Endpoints que solo normalizan el periodo ====================

    @Test
    @DisplayName("Endpoints del dashboard que normalizan el periodo")
    void endpointsConPeriodoNormalizado() throws Exception {
        Map<String, JsonNode> r = new LinkedHashMap<>();
        for (String ruta : new String[] { "/estudiantes-por-programa", "/estadisticas-por-proceso", "/estado-solicitudes",
                "/por-periodo", "/por-programa", "/tendencias-comparativas" }) {
            for (String periodo : new String[] { null, "  ", " 2025-1 ", "2025 - Primer Semestre", "sin año" }) {
                consultar(r, ruta, "periodoAcademico", periodo);
            }
            consultar(r, ruta, "periodoAcademico", "2025-2", "idPrograma", "2");
        }
        compararConReferencia("periodo-normalizado", r);
    }

    // ==================== Exportaciones ====================

    @Test
    @DisplayName("/export/pdf y /export/excel: el periodo normalizado llega al reporte")
    void exportaciones() throws Exception {
        Map<String, JsonNode> r = new LinkedHashMap<>();
        String[][] filtros = {
                {},
                { "periodoAcademico", "2025-1" },
                { "periodoAcademico", "2025 - Primer Semestre" },
                { "periodoAcademico", "  " },
                { "periodoAcademico", "2025-2", "idPrograma", "1", "proceso", "CURSO_VERANO" },
                { "proceso", "Homologacion", "idPrograma", "1" } };
        for (String ruta : new String[] { "/export/pdf", "/export/excel" }) {
            for (String[] f : filtros) {
                consultar(r, ruta, f);
            }
        }
        compararConReferencia("exportaciones", r);
    }

    // ==================== SOPORTE ====================

    /** Pide {@code ruta} con los pares parámetro/valor dados (un valor null omite el parámetro). */
    private void consultar(Map<String, JsonNode> r, String ruta, String... parametros) throws Exception {
        MockHttpServletRequestBuilder peticion = get(BASE + ruta);
        StringBuilder clave = new StringBuilder("GET " + ruta);
        char separador = '?';
        for (int i = 0; i < parametros.length; i += 2) {
            if (parametros[i + 1] == null) {
                continue;
            }
            peticion.param(parametros[i], parametros[i + 1]);
            clave.append(separador).append(parametros[i]).append("=[").append(parametros[i + 1]).append(']');
            separador = '&';
        }
        r.put(clave.toString(), respuesta(mockMvc.perform(peticion).andReturn().getResponse()));
    }

    /** {"estado": código, "cuerpo": JSON normalizado}; para PDF y Excel, las cabeceras y el texto del documento. */
    private static JsonNode respuesta(MockHttpServletResponse respuesta) throws Exception {
        ObjectNode salida = JSON.createObjectNode();
        salida.put("estado", respuesta.getStatus());
        String tipo = respuesta.getContentType();
        if (tipo != null && (tipo.contains("pdf") || tipo.contains("spreadsheet"))) {
            salida.put("contentType", tipo);
            salida.put("contentDisposition", respuesta.getHeader(HttpHeaders.CONTENT_DISPOSITION));
            byte[] contenido = respuesta.getContentAsByteArray();
            salida.set("contenido", tipo.contains("pdf") ? textoPdf(contenido) : celdasExcel(contenido));
            return salida;
        }
        String texto = respuesta.getContentAsString(StandardCharsets.UTF_8);
        salida.set("cuerpo", texto.isEmpty() ? null : normalizar(null, JSON.readTree(texto)));
        return salida;
    }

    private static JsonNode normalizar(String campo, JsonNode valor) {
        if (valor.isObject()) {
            ObjectNode ordenado = JSON.createObjectNode();
            Map<String, JsonNode> campos = new TreeMap<>();
            for (Iterator<Map.Entry<String, JsonNode>> it = valor.fields(); it.hasNext();) {
                Map.Entry<String, JsonNode> e = it.next();
                campos.put(e.getKey(), normalizar(e.getKey(), e.getValue()));
            }
            ordenado.setAll(campos);
            return ordenado;
        }
        if (valor.isArray()) {
            ArrayNode lista = JSON.createArrayNode();
            valor.forEach(v -> lista.add(normalizar(campo, v)));
            return lista;
        }
        if (campo != null && FECHAS_DE_CONSULTA.contains(campo) && esDeAhora(valor)) {
            return TextNode.valueOf("<ahora>");
        }
        return valor;
    }

    /** true si el valor es una fecha (texto ISO o milisegundos) de las últimas 48 horas. */
    private static boolean esDeAhora(JsonNode valor) {
        Instant instante = null;
        if (valor.isNumber()) {
            instante = Instant.ofEpochMilli(valor.asLong());
        } else if (valor.isTextual()) {
            try {
                instante = OffsetDateTime.parse(valor.asText()).toInstant();
            } catch (DateTimeParseException e) {
                return false;
            }
        }
        return instante != null && Math.abs(instante.toEpochMilli() - System.currentTimeMillis()) < 48 * 3_600_000L;
    }

    /** Líneas de texto del PDF, ordenadas (ver {@link #ordenadas}). */
    private static JsonNode textoPdf(byte[] contenido) throws Exception {
        List<String> lineas = new ArrayList<>();
        PdfReader lector = new PdfReader(contenido);
        try {
            for (int pagina = 1; pagina <= lector.getNumberOfPages(); pagina++) {
                for (String linea : PdfTextExtractor.getTextFromPage(lector, pagina).split("\n")) {
                    lineas.add(sinFechaDeGeneracion(linea));
                }
            }
        } finally {
            lector.close();
        }
        return ordenadas(lineas);
    }

    /** Filas de cada hoja (celdas separadas por " | "), ordenadas (ver {@link #ordenadas}). */
    private static JsonNode celdasExcel(byte[] contenido) throws Exception {
        ObjectNode hojas = JSON.createObjectNode();
        DataFormatter formato = new DataFormatter();
        try (XSSFWorkbook libro = new XSSFWorkbook(new ByteArrayInputStream(contenido))) {
            for (Sheet hoja : libro) {
                List<String> filas = new ArrayList<>();
                for (Row fila : hoja) {
                    List<String> celdas = new ArrayList<>();
                    for (Cell celda : fila) {
                        celdas.add(sinFechaDeGeneracion(formato.formatCellValue(celda)));
                    }
                    filas.add(String.join(" | ", celdas));
                }
                hojas.set(hoja.getSheetName(), ordenadas(filas));
            }
        }
        return hojas;
    }

    /**
     * Parte de lo que se imprime sale de mapas creados con {@code Map.of}, cuyo orden de iteración cambia entre
     * ejecuciones; por eso en los documentos se compara qué líneas hay, no en qué orden.
     */
    private static JsonNode ordenadas(List<String> lineas) {
        ArrayNode salida = JSON.createArrayNode();
        lineas.stream().sorted().forEach(salida::add);
        return salida;
    }

    private static String sinFechaDeGeneracion(String texto) {
        int i = texto.indexOf(FECHA_GENERACION);
        return i < 0 ? texto : texto.substring(0, i) + FECHA_GENERACION + " <ahora>";
    }

    private static void compararConReferencia(String nombre, Map<String, JsonNode> obtenido) throws Exception {
        Path archivo = CARPETA.resolve(nombre + ".json");
        if (Boolean.getBoolean("grabarReferencia")) {
            Files.createDirectories(CARPETA);
            Files.writeString(archivo, JSON.writeValueAsString(obtenido) + System.lineSeparator(), StandardCharsets.UTF_8);
        }
        JsonNode esperado = JSON.readTree(Files.readString(archivo, StandardCharsets.UTF_8));
        assertThat(JSON.valueToTree(obtenido).toString()).isEqualTo(esperado.toString());
    }
}
