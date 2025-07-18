package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;
import java.util.Date;
import java.util.List;


import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class SolicitudPazYSalvoDTOPeticion extends SolicitudDTOPeticion {
    public SolicitudPazYSalvoDTOPeticion() {
        super(); // Llama al constructor de la clase base (SolicitudDTOPeticion)
    }

    // Constructor con parámetros para todos los campos de la clase base y de esta clase
    public SolicitudPazYSalvoDTOPeticion(Integer id_solicitud, String nombre_solicitud, Date fecha_registro_solicitud, boolean esSeleccionado,
                                             EstadoSolicitudDTOPeticion estado_actual, UsuarioDTOPeticion objUsuario, List<DocumentosDTOPeticion> documentos) {
        // Llamamos al constructor de la clase base
        super(id_solicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, estado_actual, objUsuario, documentos);
    }  
}
