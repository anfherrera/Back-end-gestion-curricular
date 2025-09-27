package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionDTORespuesta {
    
    private Integer id_notificacion;
    private String tipoSolicitud;
    private String tipoNotificacion;
    private String titulo;
    private String mensaje;
    private Date fechaCreacion;
    private Boolean leida;
    private Boolean esUrgente;
    private String accion;
    private String urlAccion;
    
    // Información del usuario
    private Integer idUsuario;
    private String nombreUsuario;
    private String emailUsuario;
    
    // Información de la solicitud relacionada
    private Integer idSolicitud;
    private String nombreSolicitud;
    
    // Información del curso relacionado
    private Integer idCurso;
    private String nombreCurso;
    private String nombreMateria;
}
