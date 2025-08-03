package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface GestionarArchivosGatewayIntPort {

    String saveFile(MultipartFile file, String name, String fileType) throws IOException;
    byte[] getFile(String filename) throws IOException;
    String getFileExtension(String filename);
    Boolean isValidImageFile(String extension);
    
}