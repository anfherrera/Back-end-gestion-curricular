package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Estadisticas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEstadistica")
    private Integer id_estadistica;
    @Column(nullable = false, length = 100)
    private String nombre;
    @Column(nullable = false)
    private Date periodo_academico; //Solo usar año con uno 1 y 2 correspondientes a los 6 meses
    private Integer total_solicitudes;
    private Integer total_aprobadas;
    private Integer total_rechazadas;

    @OneToOne
    @JoinColumn(name = "idfkSolicitud", referencedColumnName = "idSolicitud", nullable = false)
    private SolicitudEntity objSolicitud;
    @OneToOne
    @JoinColumn(name = "idfkPrograma", referencedColumnName = "idPrograma", nullable = false)
    private ProgramaEntity objPrograma;

    
}
