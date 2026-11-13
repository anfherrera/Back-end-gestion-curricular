package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

/**
 * Valida que un archivo subido sea realmente un PDF.
 *
 * El nombre y el Content-Type los decide el cliente, así que no bastan: además se revisa
 * que el contenido empiece con la firma {@code %PDF-} (la especificación permite que aparezca
 * dentro del primer kilobyte).
 */
public final class ValidadorArchivoPdf {

    public static final long MAXIMO_10MB = 10L * 1024 * 1024;
    public static final long MAXIMO_15MB = 15L * 1024 * 1024;

    private static final byte[] FIRMA_PDF = "%PDF-".getBytes(StandardCharsets.US_ASCII);
    private static final int BYTES_A_REVISAR = 1024;

    /** Motivo por el que se rechaza el archivo, con el código HTTP a devolver. */
    public record Rechazo(HttpStatus estado, String mensaje) {}

    private ValidadorArchivoPdf() {}

    /**
     * @return null si el archivo es un PDF válido dentro del tamaño máximo; si no, el motivo del rechazo.
     */
    public static Rechazo validar(MultipartFile archivo, long tamanoMaximo) {
        if (archivo == null || archivo.isEmpty()) {
            return new Rechazo(HttpStatus.BAD_REQUEST, "No se proporcionó archivo o está vacío");
        }
        String nombre = archivo.getOriginalFilename();
        if (nombre == null || nombre.isBlank()) {
            return new Rechazo(HttpStatus.BAD_REQUEST, "Nombre de archivo no válido");
        }
        if (archivo.getSize() > tamanoMaximo) {
            return new Rechazo(HttpStatus.PAYLOAD_TOO_LARGE,
                    "Archivo demasiado grande. Máximo " + (tamanoMaximo / (1024 * 1024)) + "MB");
        }
        if (!nombre.toLowerCase().endsWith(".pdf") || !tieneFirmaPdf(archivo)) {
            return new Rechazo(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Solo se permiten archivos PDF. El archivo no es un PDF válido.");
        }
        return null;
    }

    static boolean tieneFirmaPdf(MultipartFile archivo) {
        byte[] inicio = new byte[BYTES_A_REVISAR];
        int leidos;
        try (InputStream in = archivo.getInputStream()) {
            leidos = in.readNBytes(inicio, 0, BYTES_A_REVISAR);
        } catch (IOException e) {
            return false;
        }
        for (int i = 0; i + FIRMA_PDF.length <= leidos; i++) {
            boolean coincide = true;
            for (int j = 0; j < FIRMA_PDF.length; j++) {
                if (inicio[i + j] != FIRMA_PDF[j]) {
                    coincide = false;
                    break;
                }
            }
            if (coincide) {
                return true;
            }
        }
        return false;
    }
}
