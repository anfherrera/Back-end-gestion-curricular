package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import org.springframework.web.multipart.MultipartFile;

public interface GestionarArchivosCUIntPort {
    
    String saveFile(MultipartFile file, String name, String fileType);
    byte[] getFile(String filename);
}