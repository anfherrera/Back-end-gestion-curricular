package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class SolicitudHomologacionDTOPeticion extends SolicitudDTOPeticion {
        public SolicitudHomologacionDTOPeticion() {
        super(); // Llama al constructor de la clase base (SolicitudDTOPeticion)
    }

    // Constructor con parámetros para todos los campos de la clase base y de esta clase
    public SolicitudHomologacionDTOPeticion(Integer id_solicitud, String nombre_solicitud, Date fecha_registro_solicitud, String periodo_academico, Boolean esSeleccionado,
                                             EstadoSolicitudDTOPeticion estado_actual, UsuarioDTOPeticion objUsuario, List<DocumentosDTOPeticion> documentos) {
        // Llamamos al constructor de la clase base (orden: id, nombre, fecha, periodo, esSeleccionado, estado, usuario, documentos)
        super(id_solicitud, nombre_solicitud, fecha_registro_solicitud, periodo_academico, esSeleccionado, estado_actual, objUsuario, documentos);
    }
}
