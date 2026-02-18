package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarArchivosGatewayIntPort;


@Service
public class GestionarArchivosGatewayImplAdapter implements GestionarArchivosGatewayIntPort {

    private final Path rootLocation;
    
    private static String rutaRaiz = "Archivos";

    public GestionarArchivosGatewayImplAdapter() throws IOException{
        this.rootLocation = Paths.get(rutaRaiz);
        if(!Files.exists(rootLocation)){
            Files.createDirectories(rootLocation);
        }
    }

    
    @Override
    public String saveFile(MultipartFile file, String name, String fileType) throws IOException {
        String extension = getFileExtension(file.getOriginalFilename());
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

        Path destination = this.rootLocation.resolve(filename);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    @Override
    public String saveFile(MultipartFile file, String name, String fileType, String tipoSolicitud, Integer idSolicitud) throws IOException {
        String extension = getFileExtension(file.getOriginalFilename());
        
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
        Path targetFolder = this.rootLocation.resolve(subfolder);
        
        // Crear carpeta si no existe
        if (!Files.exists(targetFolder)) {
            Files.createDirectories(targetFolder);
        }
        
        // Guardar archivo en la subcarpeta
        Path destination = targetFolder.resolve(name);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        
        // Retornar ruta relativa completa para guardar en BD
        String rutaRelativa = subfolder + "/" + name;
        return rutaRelativa;
    }

    @Override
    public byte[] getFile(String filename) throws IOException {
        // Primero intentar buscar en la raíz (retrocompatibilidad)
        Path filePath = this.rootLocation.resolve(filename);
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
        
        Path filePath = this.rootLocation.resolve(relativePath);
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
        Path targetFolder = this.rootLocation.resolve(subfolder);
        Path archivoDestino = targetFolder.resolve(nombreArchivo);

        // Buscar el archivo en la raíz
        Path archivoOrigen = this.rootLocation.resolve(rutaActual);
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
    public String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".")+1);
    }

    @Override
    public Boolean isValidImageFile(String extension) {
        return "jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension);
    }
    
    @Override
    public Path getFileAsPath(String filename) throws IOException {
        // Primero intentar buscar en la raíz (retrocompatibilidad)
        Path filePath = this.rootLocation.resolve(filename);
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
        
        Path filePath = this.rootLocation.resolve(relativePath);
        if (!Files.exists(filePath)) {
            throw new IOException("Archivo no encontrado en la ruta: " + relativePath);
        }
        return filePath;
    }
    
}
