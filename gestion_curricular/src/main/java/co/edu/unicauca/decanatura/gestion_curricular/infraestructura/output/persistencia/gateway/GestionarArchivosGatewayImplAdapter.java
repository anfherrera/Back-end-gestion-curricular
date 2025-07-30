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

        Files.copy(file.getInputStream(), this.rootLocation.resolve(filename));
        return filename;    
        // Path destination = this.rootLocation.resolve(filename);
        // Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        // return destination.toAbsolutePath().toString();
    }

    @Override
    public byte[] getFile(String filename) throws IOException {
        Path filPath = this.rootLocation.resolve(filename);
        return Files.readAllBytes(filPath);
    }

    @Override
    public String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".")+1);
    }

    @Override
    public Boolean isValidImageFile(String extension) {
        return "jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension);
    }
    
}
