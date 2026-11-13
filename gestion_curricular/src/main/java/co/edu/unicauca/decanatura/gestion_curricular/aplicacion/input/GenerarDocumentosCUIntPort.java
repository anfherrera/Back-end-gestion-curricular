package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.io.IOException;
import java.util.*;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.PlantillaDocumento;

public interface GenerarDocumentosCUIntPort {
    byte[] generarDocumento(int idSolicitud, String tipo, Map<String, Object> documento, Map<String, Object> solicitud) throws IOException;
    List<PlantillaDocumento> getTemplates(String proceso);
    record DocumentoGenerado(String nombreArchivo, byte[] contenido) {}
    Optional<DocumentoGenerado> generarPazYSalvo(Integer id, String cedula, String titulo, String director,
            boolean porDefecto, boolean pdf, String numero, String fecha, String observaciones) throws IOException;
}
