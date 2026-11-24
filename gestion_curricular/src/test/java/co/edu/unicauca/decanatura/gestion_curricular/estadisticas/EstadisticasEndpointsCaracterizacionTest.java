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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
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

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.Correcciones;

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
        // BUG-12: el año ya no cuenta como indicador del período
        compararConReferencia("globales", r, "estadisticas-endpoints/correcciones/globales-bug13.json",
                "estadisticas-endpoints/correcciones/globales-bug12.json");
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
        // BUG-12: el año ya no cuenta como indicador del período
        compararConReferencia("resumen-completo", r, "estadisticas-endpoints/correcciones/resumen-completo-bug13.json",
                "estadisticas-endpoints/correcciones/resumen-completo-bug12.json");
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
        // BUG-12: el año ya no cuenta como indicador del período
        compararConReferencia("dashboard", r, "estadisticas-endpoints/correcciones/dashboard-bug13.json",
                "estadisticas-endpoints/correcciones/dashboard-bug12.json");
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
        // BUG-12: el año ya no cuenta como indicador del período
        compararConReferencia("periodo-normalizado", r, "estadisticas-endpoints/correcciones/periodo-normalizado-bug12.json");
    }

    // ==================== Consultas por proceso, estado, programa y periodo ====================

    @Test
    @DisplayName("/filtradas, /periodo, /proceso, /estado, /programa, /tendencias e /historial con datos reales")
    void consultas() throws Exception {
        Map<String, JsonNode> r = new LinkedHashMap<>();
        // /filtradas: cada rama (proceso, programa, estado, fechas, sin filtros)
        consultar(r, "/filtradas", "nombreProceso", "Homologacion", "idPrograma", "1");
        consultar(r, "/filtradas", "nombreProceso", "  ", "idPrograma", "2");
        consultar(r, "/filtradas", "idPrograma", "0", "estado", "APROBADA");
        consultar(r, "/filtradas", "estado", "Rechazada");
        consultar(r, "/filtradas", "fechaInicio", "2025-01-01", "fechaFin", "2025-06-30");
        consultar(r, "/filtradas", "fechaInicio", "2025-01-01");
        consultar(r, "/filtradas");
        // /periodo: periodo académico (válido e inválido), fechas y sin datos
        consultar(r, "/periodo", "periodoAcademico", " 2025-1 ");
        consultar(r, "/periodo", "periodoAcademico", "2024-2");
        consultar(r, "/periodo", "periodoAcademico", "2025-3");
        consultar(r, "/periodo", "fechaInicio", "2024-07-01", "fechaFin", "2025-06-30");
        consultar(r, "/periodo");
        for (String proceso : new String[] { "Homologacion", "Paz y Salvo", "Cursos de Verano", "Desconocido" }) {
            consultar(r, "/proceso/" + proceso);
        }
        for (String estado : new String[] { "APROBADA", "Rechazada", "Aprobado", "Desconocido" }) {
            consultar(r, "/estado/" + estado);
        }
        for (String programa : new String[] { "1", "2", "3", "99" }) {
            consultar(r, "/programa/" + programa);
        }
        consultar(r, "/programa/2/periodo", "fechaInicio", "2024-07-01", "fechaFin", "2025-12-31");
        consultar(r, "/programa/1/periodo", "fechaInicio", "2025-01-01", "fechaFin", "2025-06-30");
        consultar(r, "/proceso/Cursos de Verano/estado/ENVIADA");
        consultar(r, "/proceso/Homologacion/estado/aprobada");
        consultar(r, "/proceso/Desconocido/estado/Rechazada");
        // /tendencias: sin fechas devuelve las globales
        consultar(r, "/tendencias", "fechaInicio1", "2024-07-01", "fechaFin1", "2024-12-31",
                "fechaInicio2", "2025-01-01", "fechaFin2", "2025-06-30");
        consultar(r, "/tendencias", "fechaInicio1", "2025-01-01", "fechaFin1", "2025-06-30",
                "fechaInicio2", "2025-07-01", "fechaFin2", "2025-12-31");
        consultar(r, "/tendencias");
        consultar(r, "/historial", "anioInicio", "2024", "anioFin", "2025");
        consultar(r, "/historial", "anioInicio", "2025", "anioFin", "2025");
        // BUG-43: consultas.json fija el 200 con {error} de /periodo?periodoAcademico=2025-3; la corrección, el 400.
        // DUP-8: /periodo, /proceso/{tipo}, /estado/{estado} y /programa/{id} se retiran y responden 404.
        // EST-7 (paso 9): /filtradas, /programa/{id}/periodo, /proceso/{t}/estado/{e} y /tendencias también.
        compararConReferencia("consultas", r, "estadisticas-endpoints/correcciones/consultas-bug43.json",
                "estadisticas-endpoints/correcciones/consultas-dup8.json",
                "estadisticas-endpoints/correcciones/consultas-est7.json");
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
        // EST-7: la clave conserva la ruta con la que se grabó; se pide /reportes?formato=pdf|excel
        for (String formato : new String[] { "pdf", "excel" }) {
            for (String[] f : filtros) {
                consultarEn(r, "/export/" + formato, "/reportes?formato=" + formato, f);
            }
        }
        // BUG-12: el año ya no cuenta como indicador del período
        compararConReferencia("exportaciones", r, "estadisticas-endpoints/correcciones/exportaciones-bug18.json",
                "estadisticas-endpoints/correcciones/exportaciones-bug12.json");
    }

    // ==================== SOPORTE ====================

    /** Pide {@code ruta} con los pares parámetro/valor dados (un valor null omite el parámetro). */
    private void consultar(Map<String, JsonNode> r, String ruta, String... parametros) throws Exception {
        consultarEn(r, ruta, ruta, parametros);
    }

    /** Como {@link #consultar}, pero la clave lleva {@code rutaClave} y se pide {@code rutaPedida}. */
    private void consultarEn(Map<String, JsonNode> r, String rutaClave, String rutaPedida, String... parametros)
            throws Exception {
        MockHttpServletRequestBuilder peticion = get(BASE + rutaPedida);
        StringBuilder clave = new StringBuilder("GET " + rutaClave);
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

    /** Compara con la referencia {@code nombre}, superponiendo en orden las correcciones indicadas. */
    private static void compararConReferencia(String nombre, Map<String, JsonNode> obtenido,
            String... rutasCorreccion) throws Exception {
        Path archivo = CARPETA.resolve(nombre + ".json");
        if (Boolean.getBoolean("grabarReferencia")) {
            Files.createDirectories(CARPETA);
            Files.writeString(archivo, JSON.writeValueAsString(obtenido) + System.lineSeparator(), StandardCharsets.UTF_8);
        }
        JsonNode esperado = JSON.readTree(Files.readString(archivo, StandardCharsets.UTF_8));
        for (String rutaCorreccion : rutasCorreccion) {
            esperado = Correcciones.aplicar(esperado, rutaCorreccion);
        }
        assertThat(JSON.valueToTree(obtenido).toString()).isEqualTo(esperado.toString());
    }
}
