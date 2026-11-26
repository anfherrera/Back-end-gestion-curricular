package co.edu.unicauca.decanatura.gestion_curricular.documentos;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.Test;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.DatosGeneracionDocumento;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.formateador.ServicioGeneracionDocumentos;

/** Al llenar los campos, el resto del párrafo conserva el formato de la plantilla. */
class FormatoPlantillasTest {

    private final ServicioGeneracionDocumentos servicio = new ServicioGeneracionDocumentos();

    @Test
    void reingresoConservaNegrillaYLetraDeLaPlantilla() throws Exception {
        try (XWPFDocument plantilla = new XWPFDocument(
                getClass().getResourceAsStream("/templates/resolucion-reingreso.docx"));
             XWPFDocument generado = generar("RESOLUCION_REINGRESO")) {
            XWPFParagraph original = parrafo(plantilla, "ARTÍCULO PRIMERO.");
            XWPFParagraph articulo = parrafo(generado, "ARTÍCULO PRIMERO.");

            // El rótulo sigue en negrilla y el texto que rodea los campos, con la letra de la plantilla
            assertThat(run(articulo, "ARTÍCULO PRIMERO.").isBold()).isTrue();
            assertThat(run(articulo, " - Aceptar el reingreso").getFontFamily())
                    .isEqualTo(run(original, " - Aceptar el reingreso").getFontFamily());
            assertThat(run(articulo, " - Aceptar el reingreso").isBold()).isFalse();
            // Los datos del estudiante van en negrilla y el periodo queda reemplazado
            assertThat(run(articulo, "Ana Pérez").isBold()).isTrue();
            assertThat(articulo.getText()).contains("para el periodo 2025-2.").doesNotContain("[");
        }
    }

    @Test
    void firmaLlevaElDiaEnLetras() throws Exception {
        try (XWPFDocument generado = generar("RESOLUCION_REINGRESO")) {
            assertThat(parrafo(generado, "Para constancia").getText())
                    .matches("Para constancia se firma en Popayán, a los [a-zéú ]+ \\(\\d{1,2}\\) días del mes de .*");
        }
    }

    @Test
    void pazYSalvoDestacaTituloYDirectorDelTrabajoDeGrado() throws Exception {
        try (XWPFDocument generado = generar("PAZ_SALVO")) {
            XWPFParagraph trabajo = parrafo(generado, "El trabajo de grado titulado");
            assertThat(run(trabajo, "\"Título de prueba\"").isBold()).isTrue();
            assertThat(run(trabajo, "Director de prueba").isBold()).isTrue();
            assertThat(run(trabajo, "El trabajo de grado titulado").isBold()).isFalse();
        }
    }

    private XWPFDocument generar(String tipo) throws Exception {
        Map<String, Object> solicitud = new HashMap<>();
        solicitud.put("nombreEstudiante", "Ana Pérez");
        solicitud.put("codigoEstudiante", "1046");
        solicitud.put("programa", "Ingeniería de Sistemas");
        solicitud.put("fechaSolicitud", "2025-02-03");
        solicitud.put("cedulaEstudiante", "123456789");
        solicitud.put("tituloTrabajoGrado", "Título de prueba");
        solicitud.put("directorTrabajoGrado", "Director de prueba");
        solicitud.put("periodoAcademico", "2025-2");
        byte[] docx = servicio.generarDocumento(new DatosGeneracionDocumento(1, tipo,
                Map.of("numeroDocumento", "REF-2025", "fechaDocumento", "2025-06-12"), solicitud, null, null));
        return new XWPFDocument(new ByteArrayInputStream(docx));
    }

    private static XWPFParagraph parrafo(XWPFDocument documento, String inicio) {
        List<XWPFParagraph> encontrados = documento.getParagraphs().stream()
                .filter(p -> p.getText().startsWith(inicio)).toList();
        assertThat(encontrados).as("párrafo que empieza por %s", inicio).hasSize(1);
        return encontrados.get(0);
    }

    /** El run cuyo texto empieza por {@code inicio}. */
    private static XWPFRun run(XWPFParagraph parrafo, String inicio) {
        return parrafo.getRuns().stream().filter(r -> r.text().startsWith(inicio)).findFirst()
                .orElseThrow(() -> new AssertionError("no hay run que empiece por: " + inicio));
    }
}
