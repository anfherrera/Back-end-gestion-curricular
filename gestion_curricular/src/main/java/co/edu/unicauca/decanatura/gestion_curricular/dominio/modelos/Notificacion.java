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
    private String tipoSolicitud; // ECAES, REINGRESO, HOMOLOGACION, PAZ_Y_SALVO, CURSO_VERANO_PREINSCRIPCION, CURSO_VERANO_INSCRIPCION
    private String tipoNotificacion; // APROBADA, RECHAZADA, ENVIADA, CAMBIO_ESTADO, NUEVA_SOLICITUD, etc.
    private String titulo;
    private String mensaje;
    private Date fechaCreacion;
    private Boolean leida = false;
    private Boolean esUrgente = false;
    private String accion; // Descripción de la acción relacionada
    private String urlAccion; // URL para la acción relacionada
    
    // Relaciones
    private Usuario objUsuario;
    private Solicitud objSolicitud;
    private CursoOfertadoVerano objCurso;
}


