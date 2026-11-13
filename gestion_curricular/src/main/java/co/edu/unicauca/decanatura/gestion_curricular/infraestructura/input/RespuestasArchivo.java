package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;

/** Construcción común de Content-Disposition, conservando los tres contratos existentes. */
public final class RespuestasArchivo {
    private RespuestasArchivo() {}

    public static String adjunto(String nombre) {
        return "attachment; filename=\"" + nombre + "\"";
    }

    public static String adjuntoUtf8(String nombre) {
        String codificado = URLEncoder.encode(nombre, StandardCharsets.UTF_8).replace("+", "%20");
        return adjunto(nombre) + "; filename*=UTF-8''" + codificado;
    }

    /** Los reportes anteriores usan form-data con name=attachment; no se cambia a attachment. */
    public static String formulario(String nombre) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", nombre);
        return headers.getFirst(HttpHeaders.CONTENT_DISPOSITION);
    }
}
