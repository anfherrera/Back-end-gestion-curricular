package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.io.IOException;
import java.io.InputStream;

/**
 * Archivo que envía un usuario, sin depender de cómo llegó (multipart HTTP, pruebas...).
 * Los puertos de archivos lo reciben en lugar de {@code MultipartFile} de Spring Web.
 */
public interface ArchivoSubido {

    /** Nombre con el que el usuario envió el archivo (puede ser null). */
    String nombreOriginal();

    /** Contenido del archivo. Quien lo abre debe cerrarlo. */
    InputStream contenido() throws IOException;
}
