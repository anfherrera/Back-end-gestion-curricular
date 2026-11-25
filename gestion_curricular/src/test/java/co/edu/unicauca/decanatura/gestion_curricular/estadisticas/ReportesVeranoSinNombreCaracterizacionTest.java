package co.edu.unicauca.decanatura.gestion_curricular.estadisticas;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.AnalisisCursosVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.DemandaCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.GlobalesContadas;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.formateador.GeneradorReportesEstadisticas;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.Correcciones;

/**
 * Fija lo que escriben el PDF y el Excel de cursos de verano cuando una materia y un programa no tienen nombre,
 * en los cuatro documentos que incluyen esa sección. Si un documento no se puede generar, se registra la clase de
 * la excepción. La fecha de generación se sustituye por un marcador y las líneas se comparan ordenadas.
 * Se graba con {@code -DgrabarReferencia=true} solo si la referencia no existe.
 */
@DisplayName("Reportes de cursos de verano con una materia y un programa sin nombre")
class ReportesVeranoSinNombreCaracterizacionTest {

    private static final Path REFERENCIA =
            Path.of("src/test/resources/referencias/estadisticas/reportes-verano-sin-nombre.json");
    private static final String FECHA_GENERACION = "Fecha de generación:";
    private static final ObjectMapper JSON = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private final GeneradorReportesEstadisticas generador = new GeneradorReportesEstadisticas();

    private final GlobalesContadas estadisticas = new GlobalesContadas(10, 6, 2, 2, 0, 60.0,
            new TreeMap<>(Map.of("APROBADA", 6, "RECHAZADA", 2, "ENVIADA", 2)),
            new TreeMap<>(Map.of("Paz y Salvo", 4, "Reingreso", 6)),
            new TreeMap<>(Map.of("Ingeniería de Sistemas", 10)), new Date());

    private final AnalisisCursosVerano cursosVerano = new AnalisisCursosVerano(null, null, null,
            new AnalisisCursosVerano.Resumen(5, 2, 1, 40.0), null, null,
            List.of(new DemandaCursoVerano("Cálculo I", 3, 60.0), new DemandaCursoVerano(null, 2, 40.0)),
            List.of(new DemandaCursoVerano(null, 5, 100.0)),
            null, List.of(), null);

    @Test
    void conservaLoQueEscribenLosReportes() throws Exception {
        ObjectNode casos = JSON.createObjectNode();
        casos.set("pdf cursos verano", pdf(() -> generador.pdfCursosVerano(cursosVerano, "2025-2", 1)));
        casos.set("pdf completo", pdf(() -> generador.pdfCompleto(estadisticas, cursosVerano, "2025-2", 1)));
        casos.set("excel cursos verano", excel(() -> generador.excel(null, cursosVerano, "2025-2", 1)));
        casos.set("excel completo", excel(() -> generador.excel(estadisticas, cursosVerano, "2025-2", 1)));
        String obtenido = JSON.writeValueAsString(casos).replace("\r\n", "\n");

        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(REFERENCIA)) {
            Files.writeString(REFERENCIA, obtenido, StandardCharsets.UTF_8);
        }
        // BUG-44: la referencia fija el Excel que falla y el "null" del PDF; la corrección, la celda en blanco
        // y "Sin nombre"
        JsonNode esperado = Correcciones.aplicar(JSON.readTree(Files.readString(REFERENCIA, StandardCharsets.UTF_8)),
                "estadisticas/correcciones/reportes-verano-sin-nombre-bug44.json");
        assertThat(casos.toString()).isEqualTo(esperado.toString());
    }

    /** Líneas del PDF, ordenadas, o la clase de la excepción si no se genera. */
    private static JsonNode pdf(Callable<byte[]> generar) throws Exception {
        byte[] contenido;
        try {
            contenido = generar.call();
        } catch (RuntimeException e) {
            return excepcion(e);
        }
        List<String> lineas = new ArrayList<>();
        PdfReader lector = new PdfReader(contenido);
        try {
            for (int pagina = 1; pagina <= lector.getNumberOfPages(); pagina++) {
                lineas.addAll(Arrays.asList(PdfTextExtractor.getTextFromPage(lector, pagina).split("\n")));
            }
        } finally {
            lector.close();
        }
        return ordenadas(lineas);
    }

    /** Filas de cada hoja (celdas separadas por " | "), ordenadas, o la clase de la excepción si no se genera. */
    private static JsonNode excel(Callable<byte[]> generar) throws Exception {
        byte[] contenido;
        try {
            contenido = generar.call();
        } catch (RuntimeException e) {
            return excepcion(e);
        }
        ObjectNode hojas = JSON.createObjectNode();
        DataFormatter formato = new DataFormatter();
        try (XSSFWorkbook libro = new XSSFWorkbook(new ByteArrayInputStream(contenido))) {
            for (Sheet hoja : libro) {
                List<String> filas = new ArrayList<>();
                for (Row fila : hoja) {
                    List<String> celdas = new ArrayList<>();
                    for (Cell celda : fila) {
                        celdas.add(formato.formatCellValue(celda));
                    }
                    filas.add(String.join(" | ", celdas));
                }
                hojas.set(hoja.getSheetName(), ordenadas(filas));
            }
        }
        return hojas;
    }

    /** La excepción y, si envuelve otra, la causa más profunda. */
    private static JsonNode excepcion(Throwable e) {
        Throwable causa = e;
        while (causa.getCause() != null) {
            causa = causa.getCause();
        }
        ObjectNode nodo = JSON.createObjectNode();
        nodo.put("excepcion", e.getClass().getName());
        nodo.put("causa", causa.getClass().getName());
        return nodo;
    }

    private static JsonNode ordenadas(List<String> lineas) {
        ArrayNode salida = JSON.createArrayNode();
        lineas.stream().map(ReportesVeranoSinNombreCaracterizacionTest::sinFechaDeGeneracion).sorted()
                .forEach(salida::add);
        return salida;
    }

    private static String sinFechaDeGeneracion(String texto) {
        int i = texto.indexOf(FECHA_GENERACION);
        return i < 0 ? texto : texto.substring(0, i) + FECHA_GENERACION + " <ahora>";
    }
}
