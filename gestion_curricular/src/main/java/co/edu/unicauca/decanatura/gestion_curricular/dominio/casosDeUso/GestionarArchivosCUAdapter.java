package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarArchivosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.ReglaNegocioExcepcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.ArchivoSubido;

public class GestionarArchivosCUAdapter implements GestionarArchivosCUIntPort {

    private final GestionarArchivosGatewayIntPort gestionarArchivos;

    public GestionarArchivosCUAdapter (GestionarArchivosGatewayIntPort gestionarArchivos){
        this.gestionarArchivos = gestionarArchivos;
    }
    

    @Override
    public String saveFile(ArchivoSubido file, String name, String fileType) {
        String filename = null;
        if(file == null){
            throw new ReglaNegocioExcepcion("el archivo no puede ser nulo");
        }
        if(name == null){
            throw new ReglaNegocioExcepcion("el nombre no puede ser nulo");
        }
        if(fileType == null){
            throw new ReglaNegocioExcepcion("el tipo de archivo no puede ser nulo");
        }
        try {
            filename = this.gestionarArchivos.saveFile(file, name, fileType);
        } catch (Exception e) {
            throw new ReglaNegocioExcepcion(e.getMessage());
        }

        return filename;
    }

    @Override
    public String saveFile(ArchivoSubido file, String name, String fileType, String tipoSolicitud, Integer idSolicitud) {
        String filename = null;
        if (file == null) {
            throw new ReglaNegocioExcepcion("el archivo no puede ser nulo");
        }
        if (name == null) {
            throw new ReglaNegocioExcepcion("el nombre no puede ser nulo");
        }
        if (fileType == null) {
            throw new ReglaNegocioExcepcion("el tipo de archivo no puede ser nulo");
        }
        if (tipoSolicitud == null || tipoSolicitud.trim().isEmpty()) {
            throw new ReglaNegocioExcepcion("el tipo de solicitud no puede ser nulo o vacío");
        }
        if (idSolicitud == null) {
            throw new ReglaNegocioExcepcion("el ID de solicitud no puede ser nulo");
        }
        try {
            filename = this.gestionarArchivos.saveFile(file, name, fileType, tipoSolicitud, idSolicitud);
        } catch (Exception e) {
            throw new ReglaNegocioExcepcion(e.getMessage());
        }
        return filename;
    }

    @Override
    public byte[] getFile(String filename) {
        byte[] archivos = null;
        if (filename == null) {
            throw new ReglaNegocioExcepcion("el nombre no puede ser nulo");
        }
        try {
            archivos = this.gestionarArchivos.getFile(filename);
        } catch (Exception e) {
            throw new ReglaNegocioExcepcion(e.getMessage());
        }
        return archivos;
    }

    @Override
    public byte[] getFileByPath(String relativePath) {
        byte[] archivos = null;
        if (relativePath == null || relativePath.trim().isEmpty()) {
            throw new ReglaNegocioExcepcion("la ruta del archivo no puede ser nula o vacía");
        }
        try {
            archivos = this.gestionarArchivos.getFileByPath(relativePath);
        } catch (Exception e) {
            throw new ReglaNegocioExcepcion(e.getMessage());
        }
        return archivos;
    }

    @Override
    public String moverArchivoAOrganizado(String rutaActual, String nombreArchivo, String tipoSolicitud, Integer idSolicitud) {
        if (rutaActual == null || rutaActual.trim().isEmpty()) {
            throw new ReglaNegocioExcepcion("la ruta actual del archivo no puede ser nula o vacía");
        }
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            throw new ReglaNegocioExcepcion("el nombre del archivo no puede ser nulo o vacío");
        }
        if (tipoSolicitud == null || tipoSolicitud.trim().isEmpty()) {
            throw new ReglaNegocioExcepcion("el tipo de solicitud no puede ser nulo o vacío");
        }
        if (idSolicitud == null) {
            throw new ReglaNegocioExcepcion("el ID de solicitud no puede ser nulo");
        }
        try {
            return this.gestionarArchivos.moverArchivoAOrganizado(rutaActual, nombreArchivo, tipoSolicitud, idSolicitud);
        } catch (Exception e) {
            throw new ReglaNegocioExcepcion(e.getMessage());
        }
    }
    
    @Override
    public java.nio.file.Path getFileAsPath(String filename) {
        if (filename == null) {
            throw new ReglaNegocioExcepcion("el nombre no puede ser nulo");
        }
        try {
            return this.gestionarArchivos.getFileAsPath(filename);
        } catch (Exception e) {
            throw new ReglaNegocioExcepcion(e.getMessage());
        }
    }
    
    @Override
    public java.nio.file.Path getFileByPathAsPath(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            throw new ReglaNegocioExcepcion("la ruta del archivo no puede ser nula o vacía");
        }
        try {
            return this.gestionarArchivos.getFileByPathAsPath(relativePath);
        } catch (Exception e) {
            throw new ReglaNegocioExcepcion(e.getMessage());
        }
    }
    
}
