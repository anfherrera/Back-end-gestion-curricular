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
    public String saveFile(MultipartFile file, String name, String fileType, String tipoSolicitud, Integer idSolicitud) {
        String filename = null;
        if (file == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("el archivo no puede ser nulo");
            return null;
        }
        if (name == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("el nombre no puede ser nulo");
            return null;
        }
        if (fileType == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("el tipo de archivo no puede ser nulo");
            return null;
        }
        if (tipoSolicitud == null || tipoSolicitud.trim().isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("el tipo de solicitud no puede ser nulo o vacío");
            return null;
        }
        if (idSolicitud == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("el ID de solicitud no puede ser nulo");
            return null;
        }
        try {
            filename = this.objGestionarArchivos.saveFile(file, name, fileType, tipoSolicitud, idSolicitud);
        } catch (Exception e) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio(e.getMessage());
        }
        return filename;
    }

    @Override
    public byte[] getFile(String filename) {
        byte[] archivos = null;
        if (filename == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("el nombre no puede ser nulo");
            return null;
        }
        try {
            archivos = this.objGestionarArchivos.getFile(filename);
        } catch (Exception e) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio(e.getMessage());
        }
        return archivos;
    }

    @Override
    public byte[] getFileByPath(String relativePath) {
        byte[] archivos = null;
        if (relativePath == null || relativePath.trim().isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("la ruta del archivo no puede ser nula o vacía");
            return null;
        }
        try {
            archivos = this.objGestionarArchivos.getFileByPath(relativePath);
        } catch (Exception e) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio(e.getMessage());
        }
        return archivos;
    }

    @Override
    public String moverArchivoAOrganizado(String rutaActual, String nombreArchivo, String tipoSolicitud, Integer idSolicitud) {
        if (rutaActual == null || rutaActual.trim().isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("la ruta actual del archivo no puede ser nula o vacía");
            return null;
        }
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("el nombre del archivo no puede ser nulo o vacío");
            return null;
        }
        if (tipoSolicitud == null || tipoSolicitud.trim().isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("el tipo de solicitud no puede ser nulo o vacío");
            return null;
        }
        if (idSolicitud == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("el ID de solicitud no puede ser nulo");
            return null;
        }
        try {
            return this.objGestionarArchivos.moverArchivoAOrganizado(rutaActual, nombreArchivo, tipoSolicitud, idSolicitud);
        } catch (Exception e) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio(e.getMessage());
            return null;
        }
    }
    
}
