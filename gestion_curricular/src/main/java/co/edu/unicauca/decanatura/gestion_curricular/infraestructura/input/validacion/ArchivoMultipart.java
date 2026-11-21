package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.validacion;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.ArchivoSubido;

/** Adapta el {@link MultipartFile} de Spring Web al {@link ArchivoSubido} del dominio. */
public final class ArchivoMultipart implements ArchivoSubido {

    private final MultipartFile archivo;

    private ArchivoMultipart(MultipartFile archivo) {
        this.archivo = archivo;
    }

    /** null si {@code archivo} es null (el caso de uso responde con su propio error). */
    public static ArchivoSubido de(MultipartFile archivo) {
        return archivo != null ? new ArchivoMultipart(archivo) : null;
    }

    @Override
    public String nombreOriginal() {
        return archivo.getOriginalFilename();
    }

    @Override
    public InputStream contenido() throws IOException {
        return archivo.getInputStream();
    }
}
