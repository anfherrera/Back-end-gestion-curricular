package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Notificaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idNotificacion")
    private Integer id_notificacion;

    @Column(nullable = false, length = 50)
    private String tipoSolicitud; // ECAES, REINGRESO, HOMOLOGACION, PAZ_Y_SALVO, CURSO_VERANO_PREINSCRIPCION, CURSO_VERANO_INSCRIPCION

    @Column(nullable = false, length = 50)
    private String tipoNotificacion; // APROBADA, RECHAZADA, ENVIADA, CAMBIO_ESTADO, NUEVA_SOLICITUD, etc.

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String mensaje;

    @Column(nullable = false)
    private Date fechaCreacion;

    @Column(nullable = false, columnDefinition = "TINYINT", length = 1)
    private Boolean leida = false;

    @Column(nullable = false, columnDefinition = "TINYINT", length = 1)
    private Boolean esUrgente = false;

    @Column(length = 100)
    private String accion; // Descripción de la acción relacionada

    @Column(length = 500)
    private String urlAccion; // URL para la acción relacionada

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario", nullable = false)
    private UsuarioEntity objUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSolicitud", nullable = true)
    private SolicitudEntity objSolicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCurso", nullable = true)
    private CursoOfertadoVeranoEntity objCurso;
}


