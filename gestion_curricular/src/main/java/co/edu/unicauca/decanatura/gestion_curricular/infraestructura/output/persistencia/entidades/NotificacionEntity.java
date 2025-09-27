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
    private String tipoSolicitud; // "CURSO_VERANO", "ECAES", "REINGRESO", etc.
    
    @Column(nullable = false, length = 50)
    private String tipoNotificacion; // "NUEVA_SOLICITUD", "APROBADO", "RECHAZADO", etc.
    
    @Column(nullable = false, length = 200)
    private String titulo;
    
    @Column(nullable = false, length = 500)
    private String mensaje;
    
    @Column(nullable = false)
    private Date fechaCreacion;
    
    @Column(nullable = false)
    private Boolean leida;
    
    @Column(nullable = false)
    private Boolean esUrgente;
    
    @Column(length = 100)
    private String accion; // "VER_SOLICITUD", "REVISAR_CURSO", etc.
    
    @Column(length = 200)
    private String urlAccion; // URL o endpoint para la acción
    
    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idfkUsuario", referencedColumnName = "idUsuario", nullable = false)
    private UsuarioEntity objUsuario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idfkSolicitud", referencedColumnName = "idSolicitud", nullable = true)
    private SolicitudEntity objSolicitud;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idfkCurso", referencedColumnName = "idCurso", nullable = true)
    private CursoOfertadoVeranoEntity objCurso;
    
}
