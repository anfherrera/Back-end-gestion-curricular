package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Service;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.ArchivoSubido;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarArchivosGatewayIntPort;


@Service
public class GestionarArchivosGatewayImplAdapter implements GestionarArchivosGatewayIntPort {

    private final Path rootLocation;
    
    private static String rutaRaiz = "Archivos";

    public GestionarArchivosGatewayImplAdapter() throws IOException{
        this.rootLocation = Paths.get(rutaRaiz).toAbsolutePath().normalize();
        if(!Files.exists(rootLocation)){
            Files.createDirectories(rootLocation);
        }
    }

    /**
     * Resuelve una ruta relativa dentro de la carpeta raíz de archivos.
     * Rechaza rutas absolutas o con "../" que intenten salir de la carpeta raíz.
     */
    private Path resolverSeguro(String rutaRelativa) throws IOException {
        if (rutaRelativa == null || rutaRelativa.isBlank()) {
            throw new IOException("Ruta del archivo no puede ser nula o vacía");
        }
        Path resultado;
        try {
            resultado = this.rootLocation.resolve(rutaRelativa).normalize();
        } catch (java.nio.file.InvalidPathException e) {
            throw new IOException("Ruta de archivo inválida");
        }
        if (!resultado.startsWith(this.rootLocation) || resultado.equals(this.rootLocation)) {
            throw new IOException("Ruta de archivo no permitida");
        }
        return resultado;
    }

    
    @Override
    public String saveFile(ArchivoSubido file, String name, String fileType) throws IOException {
        String extension = getFileExtension(file.nombreOriginal());
        //String filename = UUID.randomUUID().toString()+ "." + extension;
        if(name == null){
            throw new IOException("nombre nulo");
        }

        String filename = name ;

        if(fileType.equalsIgnoreCase("image") && !isValidImageFile(extension)){
            throw new IOException("Invalid image file type:");
        }
        if (fileType.equalsIgnoreCase("pdf") && !"pdf".equalsIgnoreCase(extension)) {
            throw new IOException("Invalid PDF file type:");
        }

        Path destination = resolverSeguro(filename);
        try (java.io.InputStream contenido = file.contenido()) {
            Files.copy(contenido, destination, StandardCopyOption.REPLACE_EXISTING);
        }
        return filename;
    }

    @Override
    public String saveFile(ArchivoSubido file, String name, String fileType, String tipoSolicitud, Integer idSolicitud) throws IOException {
        String extension = getFileExtension(file.nombreOriginal());
        
        if (name == null) {
            throw new IOException("nombre nulo");
        }
        
        if (tipoSolicitud == null || tipoSolicitud.trim().isEmpty()) {
            throw new IOException("tipo de solicitud no puede ser nulo o vacío");
        }
        
        if (idSolicitud == null) {
            throw new IOException("ID de solicitud no puede ser nulo");
        }

        // Validar tipo de archivo
        if (fileType.equalsIgnoreCase("image") && !isValidImageFile(extension)) {
            throw new IOException("Invalid image file type:");
        }
        if (fileType.equalsIgnoreCase("pdf") && !"pdf".equalsIgnoreCase(extension)) {
            throw new IOException("Invalid PDF file type:");
        }

        // Construir ruta organizada: tipoSolicitud/solicitud_id/nombre
        String tipoSolicitudNormalizado = tipoSolicitud.toLowerCase().replaceAll("[^a-z0-9-_]", "_");
        String subfolder = tipoSolicitudNormalizado + "/solicitud_" + idSolicitud;
        Path targetFolder = resolverSeguro(subfolder);
        
        // Crear carpeta si no existe
        if (!Files.exists(targetFolder)) {
            Files.createDirectories(targetFolder);
        }
        
        // Guardar archivo en la subcarpeta
        Path destination = resolverSeguro(subfolder + "/" + name);
        try (java.io.InputStream contenido = file.contenido()) {
            Files.copy(contenido, destination, StandardCopyOption.REPLACE_EXISTING);
        }
        
        // Retornar ruta relativa completa para guardar en BD
        String rutaRelativa = subfolder + "/" + name;
        return rutaRelativa;
    }

    @Override
    public byte[] getFile(String filename) throws IOException {
        // Primero intentar buscar en la raíz (retrocompatibilidad)
        Path filePath = resolverSeguro(filename);
        if (Files.exists(filePath)) {
            return Files.readAllBytes(filePath);
        }
        
        // Si no existe en la raíz, intentar como ruta relativa completa
        return getFileByPath(filename);
    }

    @Override
    public byte[] getFileByPath(String relativePath) throws IOException {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            throw new IOException("Ruta del archivo no puede ser nula o vacía");
        }
        
        Path filePath = resolverSeguro(relativePath);
        if (!Files.exists(filePath)) {
            throw new IOException("Archivo no encontrado en la ruta: " + relativePath);
        }
        return Files.readAllBytes(filePath);
    }

    @Override
    public String moverArchivoAOrganizado(String rutaActual, String nombreArchivo, String tipoSolicitud, Integer idSolicitud) throws IOException {
        if (rutaActual == null || rutaActual.trim().isEmpty()) {
            throw new IOException("Ruta actual del archivo no puede ser nula o vacía");
        }
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            throw new IOException("Nombre del archivo no puede ser nulo o vacío");
        }
        if (tipoSolicitud == null || tipoSolicitud.trim().isEmpty()) {
            throw new IOException("Tipo de solicitud no puede ser nulo o vacío");
        }
        if (idSolicitud == null) {
            throw new IOException("ID de solicitud no puede ser nulo");
        }
        
        // Si la ruta actual ya está organizada, no mover
        if (rutaActual.contains("/")) {
            // Ya está organizado, retornar la ruta actual
            return rutaActual;
        }
        
        // Construir ruta destino organizada (para uso en movimiento o fallback)
        String tipoSolicitudNormalizado = tipoSolicitud.toLowerCase().replaceAll("[^a-z0-9-_]", "_");
        String subfolder = tipoSolicitudNormalizado + "/solicitud_" + idSolicitud;
        Path targetFolder = resolverSeguro(subfolder);
        Path archivoDestino = resolverSeguro(subfolder + "/" + nombreArchivo);

        // Buscar el archivo en la raíz
        Path archivoOrigen = resolverSeguro(rutaActual);
        if (Files.exists(archivoOrigen)) {
            // Crear carpeta destino si no existe y mover
            if (!Files.exists(targetFolder)) {
                Files.createDirectories(targetFolder);
            }
            Files.move(archivoOrigen, archivoDestino, StandardCopyOption.REPLACE_EXISTING);
            return subfolder + "/" + nombreArchivo;
        }

        // Si no está en raíz: puede estar ya en carpeta organizada por un intento anterior (ej. creación ECAES falló tras mover y hubo rollback)
        if (Files.exists(archivoDestino)) {
            return subfolder + "/" + nombreArchivo;
        }

        throw new IOException("Archivo no encontrado en la ruta: " + rutaActual);
    }

    @Override
    public Path getFileAsPath(String filename) throws IOException {
        // Primero intentar buscar en la raíz (retrocompatibilidad)
        Path filePath = resolverSeguro(filename);
        if (Files.exists(filePath)) {
            return filePath;
        }
        
        // Si no existe en la raíz, intentar como ruta relativa completa
        return getFileByPathAsPath(filename);
    }
    
    @Override
    public Path getFileByPathAsPath(String relativePath) throws IOException {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            throw new IOException("Ruta del archivo no puede ser nula o vacía");
        }
        
        Path filePath = resolverSeguro(relativePath);
        if (!Files.exists(filePath)) {
            throw new IOException("Archivo no encontrado en la ruta: " + relativePath);
        }
        return filePath;
    }
    

    private static String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private static boolean isValidImageFile(String extension) {
        return "jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension);
    }
}
