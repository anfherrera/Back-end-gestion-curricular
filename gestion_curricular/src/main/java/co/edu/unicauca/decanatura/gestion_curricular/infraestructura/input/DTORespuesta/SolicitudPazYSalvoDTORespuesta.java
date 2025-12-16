package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class SolicitudPazYSalvoDTORespuesta extends SolicitudDTORespuesta  {
    public SolicitudPazYSalvoDTORespuesta () {
        super(); // Llama al constructor de la clase base (SolicitudDTORespuesta )
    }

    // Constructor con parámetros para todos los campos de la clase base y de esta clase
    public SolicitudPazYSalvoDTORespuesta (Integer id_solicitud, String nombre_solicitud, String periodo_academico, Date fecha_registro_solicitud,
                                             List<EstadoSolicitudDTORespuesta> estadosSolicitud, UsuarioDTORespuesta objUsuario, List<DocumentosDTORespuesta > documentos,
                                             String categoria, String tipo_solicitud) {
        // Llamamos al constructor de la clase base (orden: id, nombre, periodo, fecha, estados, usuario, documentos, categoria, tipo_solicitud)
        super(id_solicitud, nombre_solicitud, periodo_academico, fecha_registro_solicitud, estadosSolicitud, objUsuario, documentos, categoria, tipo_solicitud);
    }  
}
