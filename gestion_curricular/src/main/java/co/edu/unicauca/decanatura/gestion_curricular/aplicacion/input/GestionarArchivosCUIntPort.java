package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.nio.file.Path;

import org.springframework.web.multipart.MultipartFile;

public interface GestionarArchivosCUIntPort {
    
    /**
     * Guarda un archivo en la carpeta raíz (método original para retrocompatibilidad)
     */
    String saveFile(MultipartFile file, String name, String fileType);
    
    /**
     * Guarda un archivo organizado en subcarpetas por tipo de solicitud e ID
     * @param file Archivo a guardar
     * @param name Nombre del archivo
     * @param fileType Tipo de archivo (pdf, image, etc.)
     * @param tipoSolicitud Tipo de solicitud (pazysalvo, reingreso, curso-verano, etc.)
     * @param idSolicitud ID de la solicitud
     * @return Ruta relativa completa del archivo (ej: "pazysalvo/solicitud_123/archivo.pdf")
     */
    String saveFile(MultipartFile file, String name, String fileType, String tipoSolicitud, Integer idSolicitud);
    
    /**
     * Obtiene un archivo por nombre o ruta (método original para retrocompatibilidad)
     */
    byte[] getFile(String filename);
    
    /**
     * Obtiene un archivo por ruta relativa completa (incluye subcarpetas)
     * @param relativePath Ruta relativa completa (ej: "pazysalvo/solicitud_123/archivo.pdf")
     * @return Contenido del archivo en bytes
     */
    byte[] getFileByPath(String relativePath);
    
    /**
     * Obtiene la ruta (Path) de un archivo por nombre para streaming eficiente
     * @param filename Nombre del archivo o ruta relativa
     * @return Path del archivo para streaming
     */
    Path getFileAsPath(String filename);
    
    /**
     * Obtiene la ruta (Path) de un archivo por ruta relativa completa para streaming eficiente
     * @param relativePath Ruta relativa completa (ej: "pazysalvo/solicitud_123/archivo.pdf")
     * @return Path del archivo para streaming
     */
    Path getFileByPathAsPath(String relativePath);
    
    /**
     * Mueve un archivo desde su ubicación actual a una carpeta organizada
     * @param rutaActual Ruta actual del archivo (puede ser solo el nombre si está en raíz)
     * @param nombreArchivo Nombre del archivo
     * @param tipoSolicitud Tipo de solicitud (pazysalvo, reingreso, curso-verano, etc.)
     * @param idSolicitud ID de la solicitud
     * @return Nueva ruta relativa completa del archivo
     */
    String moverArchivoAOrganizado(String rutaActual, String nombreArchivo, String tipoSolicitud, Integer idSolicitud);
}
