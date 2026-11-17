package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarArchivosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.ArchivoSubido;

public class GestionarArchivosCUAdapter implements GestionarArchivosCUIntPort {

    private final GestionarArchivosGatewayIntPort gestionarArchivos;
    private final FormateadorResultadosIntPort formateadorResultados;

    public GestionarArchivosCUAdapter (GestionarArchivosGatewayIntPort gestionarArchivos, FormateadorResultadosIntPort formateadorResultados){
        this.gestionarArchivos = gestionarArchivos;
        this.formateadorResultados = formateadorResultados;
    }
    

    @Override
    public String saveFile(ArchivoSubido file, String name, String fileType) {
        String filename = null;
        if(file == null){
            this.formateadorResultados.retornarRespuestaErrorReglaDeNegocio("el archivo no puede ser nulo");
        }
        if(name == null){
            this.formateadorResultados.retornarRespuestaErrorReglaDeNegocio("el nombre no puede ser nulo");
        }
        if(fileType == null){
            this.formateadorResultados.retornarRespuestaErrorReglaDeNegocio("el tipo de archivo no puede ser nulo");
        }
        try {
            filename = this.gestionarArchivos.saveFile(file, name, fileType);
        } catch (Exception e) {
            this.formateadorResultados.retornarRespuestaErrorReglaDeNegocio(e.getMessage());
        }

        return filename;
    }

    @Override
    public String saveFile(ArchivoSubido file, String name, String fileType, String tipoSolicitud, Integer idSolicitud) {
        String filename = null;
        if (file == null) {
            this.formateadorResultados.retornarRespuestaErrorReglaDeNegocio("el archivo no puede ser nulo");
            return null;
        }
        if (name == null) {
            this.formateadorResultados.retornarRespuestaErrorReglaDeNegocio("el nombre no puede ser nulo");
            return null;
        }
        if (fileType == null) {
            this.formateadorResultados.retornarRespuestaErrorReglaDeNegocio("el tipo de archivo no puede ser nulo");
            return null;
        }
        if (tipoSolicitud == null || tipoSolicitud.trim().isEmpty()) {
            this.formateadorResultados.retornarRespuestaErrorReglaDeNegocio("el tipo de solicitud no puede ser nulo o vacío");
            return null;
        }
        if (idSolicitud == null) {
            this.formateadorResultados.retornarRespuestaErrorReglaDeNegocio("el ID de solicitud no puede ser nulo");
            return null;
        }
        try {
            filename = this.gestionarArchivos.saveFile(file, name, fileType, tipoSolicitud, idSolicitud);
        } catch (Exception e) {
            this.formateadorResultados.retornarRespuestaErrorReglaDeNegocio(e.getMessage());
        }
        return filename;
    }

    @Override
    public byte[] getFile(String filename) {
        byte[] archivos = null;
        if (filename == null) {
            this.formateadorResultados.retornarRespuestaErrorReglaDeNegocio("el nombre no puede ser nulo");
            return null;
        }
        try {
            archivos = this.gestionarArchivos.getFile(filename);
        } catch (Exception e) {
            this.formateadorResultados.retornarRespuestaErrorReglaDeNegocio(e.getMessage());
        }
        return archivos;
    }

    @Override
    public byte[] getFileByPath(String relativePath) {
        byte[] archivos = null;
        if (relativePath == null || relativePath.trim().isEmpty()) {
            this.formateadorResultados.retornarRespuestaErrorReglaDeNegocio("la ruta del archivo no puede ser nula o vacía");
            return null;
        }
        try {
            archivos = this.gestionarArchivos.getFileByPath(relativePath);
        } catch (Exception e) {
            this.formateadorResultados.retornarRespuestaErrorReglaDeNegocio(e.getMessage());
        }
        return archivos;
    }

    @Override
    public String moverArchivoAOrganizado(String rutaActual, String nombreArchivo, String tipoSolicitud, Integer idSolicitud) {
        if (rutaActual == null || rutaActual.trim().isEmpty()) {
            this.formateadorResultados.retornarRespuestaErrorReglaDeNegocio("la ruta actual del archivo no puede ser nula o vacía");
            return null;
        }
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            this.formateadorResultados.retornarRespuestaErrorReglaDeNegocio("el nombre del archivo no puede ser nulo o vacío");
            return null;
        }
        if (tipoSolicitud == null || tipoSolicitud.trim().isEmpty()) {
            this.formateadorResultados.retornarRespuestaErrorReglaDeNegocio("el tipo de solicitud no puede ser nulo o vacío");
            return null;
        }
        if (idSolicitud == null) {
            this.formateadorResultados.retornarRespuestaErrorReglaDeNegocio("el ID de solicitud no puede ser nulo");
            return null;
        }
        try {
            return this.gestionarArchivos.moverArchivoAOrganizado(rutaActual, nombreArchivo, tipoSolicitud, idSolicitud);
        } catch (Exception e) {
            this.formateadorResultados.retornarRespuestaErrorReglaDeNegocio(e.getMessage());
            return null;
        }
    }
    
    @Override
    public java.nio.file.Path getFileAsPath(String filename) {
        if (filename == null) {
            this.formateadorResultados.retornarRespuestaErrorReglaDeNegocio("el nombre no puede ser nulo");
            return null;
        }
        try {
            return this.gestionarArchivos.getFileAsPath(filename);
        } catch (Exception e) {
            this.formateadorResultados.retornarRespuestaErrorReglaDeNegocio(e.getMessage());
            return null;
        }
    }
    
    @Override
    public java.nio.file.Path getFileByPathAsPath(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            this.formateadorResultados.retornarRespuestaErrorReglaDeNegocio("la ruta del archivo no puede ser nula o vacía");
            return null;
        }
        try {
            return this.gestionarArchivos.getFileByPathAsPath(relativePath);
        } catch (Exception e) {
            this.formateadorResultados.retornarRespuestaErrorReglaDeNegocio(e.getMessage());
            return null;
        }
    }
    
}
