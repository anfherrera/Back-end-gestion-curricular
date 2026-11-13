package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.formateador;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeaderFooter;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

/** Renderiza el oficio de Paz y Salvo ya completado con sus imágenes institucionales. */
final class GeneradorPdfPazYSalvo {

    private GeneradorPdfPazYSalvo() {
    }

    static byte[] desdePlantillaRellena(byte[] docx) throws IOException {
        try (var plantilla = new XWPFDocument(new ByteArrayInputStream(docx))) {
            var salida = new ByteArrayOutputStream();
            Document pdf = new Document(PageSize.LETTER, 58, 58, 42, 42);
            try {
                PdfWriter.getInstance(pdf, salida);
                pdf.open();
                for (var encabezado : plantilla.getHeaderList()) {
                    agregarImagenes(pdf, encabezado);
                }
                for (var parrafo : plantilla.getParagraphs()) {
                    pdf.add(convertirParrafo(parrafo));
                }
                for (var pie : plantilla.getFooterList()) {
                    agregarImagenes(pdf, pie);
                }
            } catch (DocumentException e) {
                throw new IOException("No se pudo generar el PDF de Paz y Salvo", e);
            } finally {
                pdf.close();
            }
            return salida.toByteArray();
        }
    }

    private static Paragraph convertirParrafo(XWPFParagraph origen) {
        String texto = origen.getText();
        boolean negrita = origen.getRuns().stream().anyMatch(run -> run.isBold() && !run.text().isBlank());
        Font fuente = new Font(Font.FontFamily.HELVETICA, 11, negrita ? Font.BOLD : Font.NORMAL);
        Paragraph parrafo = new Paragraph(texto.isBlank() ? " " : texto, fuente);
        parrafo.setLeading(texto.isBlank() ? 6 : 14);
        parrafo.setSpacingAfter(texto.isBlank() ? 0 : 4);
        parrafo.setAlignment(switch (origen.getAlignment()) {
            case CENTER -> Element.ALIGN_CENTER;
            case RIGHT -> Element.ALIGN_RIGHT;
            case BOTH -> Element.ALIGN_JUSTIFIED;
            default -> Element.ALIGN_LEFT;
        });
        return parrafo;
    }

    private static void agregarImagenes(Document pdf, XWPFHeaderFooter seccion)
            throws IOException, DocumentException {
        for (var imagenWord : seccion.getAllPictures()) {
            Image imagen = Image.getInstance(imagenWord.getData());
            imagen.scaleToFit(pdf.getPageSize().getWidth() - pdf.leftMargin() - pdf.rightMargin(), 65);
            imagen.setAlignment(Element.ALIGN_CENTER);
            imagen.setSpacingBefore(5);
            imagen.setSpacingAfter(5);
            pdf.add(imagen);
        }
    }
}
