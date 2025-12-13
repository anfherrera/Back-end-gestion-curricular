package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class SolicitudReingresoDTORespuesta extends SolicitudDTORespuesta{
        public SolicitudReingresoDTORespuesta() {
        super(); // Llama al constructor de la clase base (SolicitudDTORespuesta)
    }

    // Constructor con parámetros para todos los campos de la clase base y de esta clase
    public SolicitudReingresoDTORespuesta(Integer id_solicitud, String nombre_solicitud, String periodo_academico, Date fecha_registro_solicitud, Boolean esSeleccionado,
                                             List<EstadoSolicitudDTORespuesta> estado_actual, UsuarioDTORespuesta objUsuario, List<DocumentosDTORespuesta> documentos) {
        // Llamamos al constructor de la clase base (orden: id, nombre, periodo, fecha, esSeleccionado, estados, usuario, documentos)
        super(id_solicitud, nombre_solicitud, periodo_academico, fecha_registro_solicitud, esSeleccionado, estado_actual, objUsuario, documentos);
    }  
}
