package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.http.ContentDisposition;

/** Construcción común de Content-Disposition para las descargas. */
public final class RespuestasArchivo {
    private RespuestasArchivo() {}

    public static String adjunto(String nombre) {
        return "attachment; filename=\"" + nombre + "\"";
    }

    public static String adjuntoUtf8(String nombre) {
        String codificado = URLEncoder.encode(nombre, StandardCharsets.UTF_8).replace("+", "%20");
        return adjunto(nombre) + "; filename*=UTF-8''" + codificado;
    }

    /** Como {@link #adjunto}, pero el nombre lo escapa {@link ContentDisposition}. */
    public static String adjuntoEscapado(String nombre) {
        return ContentDisposition.attachment().filename(nombre).build().toString();
    }
}
