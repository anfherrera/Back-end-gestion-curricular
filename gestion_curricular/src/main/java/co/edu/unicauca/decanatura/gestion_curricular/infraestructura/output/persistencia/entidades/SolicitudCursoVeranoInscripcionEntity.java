package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;


import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.Enums.CondicionSolicitudVeranoEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Solicitudes_Cursos_Verano_Inscripcion")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SolicitudCursoVeranoInscripcionEntity extends SolicitudEntity {

    @Column(nullable = false, length = 100)
    private String nombre_estudiante;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CondicionSolicitudVeranoEntity codicion_solicitud;
    @Column(nullable = false, length = 100)
    private String observacion;

    @Column(nullable = false, length = 255)
    private String ruta_recibo_pago;

    @OneToOne
    @JoinColumn(name = "idfkCurso", referencedColumnName = "idCurso", nullable = false)
    private CursoOfertadoVeranoEntity objCursoOfertado;

}
