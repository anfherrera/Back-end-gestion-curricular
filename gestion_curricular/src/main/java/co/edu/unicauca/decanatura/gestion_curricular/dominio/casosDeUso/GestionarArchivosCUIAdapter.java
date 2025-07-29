package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import org.springframework.web.multipart.MultipartFile;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarArchivosGatewayIntPort;

public class GestionarArchivosCUIAdapter implements GestionarArchivosCUIntPort {

    private final GestionarArchivosGatewayIntPort objGestionarArchivos;
    private final FormateadorResultadosIntPort objFormateadorResultados;

    public GestionarArchivosCUIAdapter (GestionarArchivosGatewayIntPort objGestionarArchivos, FormateadorResultadosIntPort objFormateadorResultados){
        this.objGestionarArchivos = objGestionarArchivos;
        this.objFormateadorResultados = objFormateadorResultados;
    }
    

    @Override
    public String saveFile(MultipartFile file, String name, String fileType) {
        String filename = null;
        if(file == null){
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("el archivo no puede ser nulo");
        }
        if(name == null){
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("el nombre no puede ser nulo");
        }
        if(fileType == null){
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("el tipo de archivo no puede ser nulo");
        }
        try {
            filename = this.objGestionarArchivos.saveFile(file, name, fileType);
        } catch (Exception e) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio(e.getMessage());
        }

        return filename;
    }

    @Override
    public byte[] getFile(String filename) {
        byte [] archivos = null;
        if(filename == null){
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("el nombre no puede ser nulo");
        }
        try {
            archivos = this.objGestionarArchivos.getFile(filename);
            
        } catch (Exception e) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio(e.getMessage());
        }
        return archivos;
    }
    
}
