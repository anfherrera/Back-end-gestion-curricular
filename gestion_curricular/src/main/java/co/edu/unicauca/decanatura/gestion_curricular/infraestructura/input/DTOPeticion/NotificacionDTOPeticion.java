package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import java.util.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionDTOPeticion {
    
    private Integer id_notificacion;
    
    @NotBlank(message = "El tipo de solicitud es obligatorio")
    private String tipoSolicitud;
    
    @NotBlank(message = "El tipo de notificación es obligatorio")
    private String tipoNotificacion;
    
    @NotBlank(message = "El título es obligatorio")
    private String titulo;
    
    @NotBlank(message = "El mensaje es obligatorio")
    private String mensaje;
    
    private Date fechaCreacion;
    private Boolean leida;
    private Boolean esUrgente;
    private String accion;
    private String urlAccion;
    
    // Relaciones
    @NotNull(message = "El usuario es obligatorio")
    private Integer idUsuario;
    
    private Integer idSolicitud;
    private Integer idCurso;
}
