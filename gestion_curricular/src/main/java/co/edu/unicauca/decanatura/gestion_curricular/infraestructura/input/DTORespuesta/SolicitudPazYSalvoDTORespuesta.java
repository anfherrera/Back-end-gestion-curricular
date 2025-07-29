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
    public SolicitudPazYSalvoDTORespuesta (Integer id_solicitud, String nombre_solicitud, Date fecha_registro_solicitud, Boolean esSeleccionado,
                                             List<EstadoSolicitudDTORespuesta> estado_actual, UsuarioDTORespuesta objUsuario, List<DocumentosDTORespuesta > documentos) {
        // Llamamos al constructor de la clase base
        super(id_solicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, estado_actual, objUsuario, documentos);
    }  
}
