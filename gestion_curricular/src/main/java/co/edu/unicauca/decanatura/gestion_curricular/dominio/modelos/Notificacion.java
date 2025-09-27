package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {
    
    private Integer id_notificacion;
    private String tipoSolicitud; // "CURSO_VERANO", "ECAES", "REINGRESO", "HOMOLOGACION", "PAZ_SALVO"
    private String tipoNotificacion; // "NUEVA_SOLICITUD", "APROBADO", "RECHAZADO", "PENDIENTE", "ALERTA"
    private String mensaje;
    private String titulo;
    private Date fechaCreacion;
    private Boolean leida;
    private Boolean esUrgente;
    private String accion; // "VER_SOLICITUD", "REVISAR_CURSO", "VALIDAR_PAGO", etc.
    private String urlAccion; // URL o endpoint para la acción
    
    // Relaciones
    private Usuario objUsuario; // Usuario que recibe la notificación
    private Solicitud objSolicitud; // Solicitud relacionada (opcional)
    private CursoOfertadoVerano objCurso; // Curso relacionado (opcional)
    
    // Constructor para notificaciones simples
    public Notificacion(String tipoSolicitud, String tipoNotificacion, String titulo, String mensaje, Usuario usuario) {
        this();
        this.tipoSolicitud = tipoSolicitud;
        this.tipoNotificacion = tipoNotificacion;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.objUsuario = usuario;
    }
    
    // Constructor para notificaciones con solicitud
    public Notificacion(String tipoSolicitud, String tipoNotificacion, String titulo, String mensaje, 
                       Usuario usuario, Solicitud solicitud) {
        this(tipoSolicitud, tipoNotificacion, titulo, mensaje, usuario);
        this.objSolicitud = solicitud;
    }
    
    // Constructor para notificaciones con curso
    public Notificacion(String tipoSolicitud, String tipoNotificacion, String titulo, String mensaje, 
                       Usuario usuario, CursoOfertadoVerano curso) {
        this(tipoSolicitud, tipoNotificacion, titulo, mensaje, usuario);
        this.objCurso = curso;
    }
}
