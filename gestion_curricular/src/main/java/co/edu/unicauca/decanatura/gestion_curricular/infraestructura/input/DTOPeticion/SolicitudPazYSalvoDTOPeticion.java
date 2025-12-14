package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;
import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class SolicitudPazYSalvoDTOPeticion extends SolicitudDTOPeticion {
    
    @Size(max = 500, message = "El título del trabajo de grado no puede exceder 500 caracteres")
    private String titulo_trabajo_grado; // Título del trabajo de grado sustentado
    
    @Size(max = 200, message = "El director del trabajo de grado no puede exceder 200 caracteres")
    private String director_trabajo_grado; // Director del trabajo de grado (ej: "PhD. Carlos Alberto Cobos Lozada")
    
    public SolicitudPazYSalvoDTOPeticion() {
        super(); // Llama al constructor de la clase base (SolicitudDTOPeticion)
    }

    // Constructor con parámetros para todos los campos de la clase base y de esta clase
    public SolicitudPazYSalvoDTOPeticion(Integer id_solicitud, String nombre_solicitud, Date fecha_registro_solicitud, String periodo_academico,
                                             EstadoSolicitudDTOPeticion estado_actual, UsuarioDTOPeticion objUsuario, List<DocumentosDTOPeticion> documentos) {
        // Llamamos al constructor de la clase base (orden: id, nombre, fecha, periodo, estado, usuario, documentos)
        super(id_solicitud, nombre_solicitud, fecha_registro_solicitud, periodo_academico, estado_actual, objUsuario, documentos);
    }  
}
