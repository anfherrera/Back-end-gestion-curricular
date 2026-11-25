package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion.ValidadorArchivoPdf;

class ValidadorArchivoPdfTest {

    private static MockMultipartFile archivo(String nombre, String tipo, byte[] contenido) {
        return new MockMultipartFile("file", nombre, tipo, contenido);
    }

    @Test
    @DisplayName("Acepta un PDF real")
    void aceptaPdfReal() {
        byte[] pdf = "%PDF-1.7\n1 0 obj\n<<>>\nendobj\n%%EOF".getBytes(StandardCharsets.US_ASCII);
        assertNull(ValidadorArchivoPdf.validar(archivo("doc.pdf", "application/pdf", pdf), ValidadorArchivoPdf.MAXIMO_10MB));
    }

    @Test
    @DisplayName("Acepta la firma %PDF- dentro del primer kilobyte")
    void aceptaFirmaDespuesDeBytesBasura() {
        byte[] pdf = ("\n\n   %PDF-1.4 resto").getBytes(StandardCharsets.US_ASCII);
        assertNull(ValidadorArchivoPdf.validar(archivo("doc.pdf", "application/pdf", pdf), ValidadorArchivoPdf.MAXIMO_10MB));
    }

    @Test
    @DisplayName("Rechaza un archivo renombrado a .pdf que no es PDF")
    void rechazaArchivoRenombrado() {
        byte[] exe = new byte[] { 'M', 'Z', 0, 0, 1, 2, 3 };
        ValidadorArchivoPdf.Rechazo r = ValidadorArchivoPdf.validar(
                archivo("factura.pdf", "application/pdf", exe), ValidadorArchivoPdf.MAXIMO_10MB);
        assertNotNull(r);
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, r.estado());
    }

    @Test
    @DisplayName("Rechaza un PDF real con extensión distinta")
    void rechazaExtensionDistinta() {
        byte[] pdf = "%PDF-1.7".getBytes(StandardCharsets.US_ASCII);
        ValidadorArchivoPdf.Rechazo r = ValidadorArchivoPdf.validar(
                archivo("doc.exe", "application/pdf", pdf), ValidadorArchivoPdf.MAXIMO_10MB);
        assertNotNull(r);
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, r.estado());
    }

    @Test
    @DisplayName("Rechaza archivo vacío con 400")
    void rechazaVacio() {
        ValidadorArchivoPdf.Rechazo r = ValidadorArchivoPdf.validar(
                archivo("doc.pdf", "application/pdf", new byte[0]), ValidadorArchivoPdf.MAXIMO_10MB);
        assertNotNull(r);
        assertEquals(HttpStatus.BAD_REQUEST, r.estado());
    }

    @Test
    @DisplayName("Rechaza archivo que supera el tamaño máximo con 413")
    void rechazaDemasiadoGrande() {
        byte[] pdf = "%PDF-1.7 contenido".getBytes(StandardCharsets.US_ASCII);
        ValidadorArchivoPdf.Rechazo r = ValidadorArchivoPdf.validar(archivo("doc.pdf", "application/pdf", pdf), 5);
        assertNotNull(r);
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, r.estado());
    }
}
