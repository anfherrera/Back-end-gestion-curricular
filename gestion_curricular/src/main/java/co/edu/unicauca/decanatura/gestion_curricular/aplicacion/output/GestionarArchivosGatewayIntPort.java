package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.web.multipart.MultipartFile;

public interface GestionarArchivosGatewayIntPort {

    /**
     * Guarda un archivo en la carpeta raíz (método original para retrocompatibilidad)
     */
    String saveFile(MultipartFile file, String name, String fileType) throws IOException;
    
    /**
     * Guarda un archivo organizado en subcarpetas por tipo de solicitud e ID
     * @param file Archivo a guardar
     * @param name Nombre del archivo
     * @param fileType Tipo de archivo (pdf, image, etc.)
     * @param tipoSolicitud Tipo de solicitud (pazysalvo, reingreso, curso-verano, etc.)
     * @param idSolicitud ID de la solicitud
     * @return Ruta relativa completa del archivo (ej: "pazysalvo/solicitud_123/archivo.pdf")
     * @throws IOException
     */
    String saveFile(MultipartFile file, String name, String fileType, String tipoSolicitud, Integer idSolicitud) throws IOException;
    
    /**
     * Obtiene un archivo por nombre (método original para retrocompatibilidad)
     * Busca primero en la raíz, luego intenta con rutas relativas
     */
    byte[] getFile(String filename) throws IOException;
    
    /**
     * Obtiene un archivo por ruta relativa completa (incluye subcarpetas)
     * @param relativePath Ruta relativa completa (ej: "pazysalvo/solicitud_123/archivo.pdf")
     * @return Contenido del archivo en bytes
     * @throws IOException
     */
    byte[] getFileByPath(String relativePath) throws IOException;
    
    /**
     * Obtiene un archivo por nombre como Path para streaming eficiente
     * Busca primero en la raíz, luego intenta con rutas relativas
     * @param filename Nombre del archivo o ruta relativa
     * @return Path del archivo para streaming
     * @throws IOException Si el archivo no existe
     */
    Path getFileAsPath(String filename) throws IOException;
    
    /**
     * Obtiene un archivo por ruta relativa completa como Path para streaming eficiente
     * @param relativePath Ruta relativa completa (ej: "pazysalvo/solicitud_123/archivo.pdf")
     * @return Path del archivo para streaming
     * @throws IOException Si el archivo no existe
     */
    Path getFileByPathAsPath(String relativePath) throws IOException;
    
    /**
     * Mueve un archivo desde su ubicación actual a una carpeta organizada
     * @param rutaActual Ruta actual del archivo (puede ser solo el nombre si está en raíz)
     * @param nombreArchivo Nombre del archivo
     * @param tipoSolicitud Tipo de solicitud (pazysalvo, reingreso, curso-verano, etc.)
     * @param idSolicitud ID de la solicitud
     * @return Nueva ruta relativa completa del archivo
     * @throws IOException
     */
    String moverArchivoAOrganizado(String rutaActual, String nombreArchivo, String tipoSolicitud, Integer idSolicitud) throws IOException;
    
    String getFileExtension(String filename);
    Boolean isValidImageFile(String extension);
    
}
