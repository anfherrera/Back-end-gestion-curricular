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

@Entity
@Table(name = "Estados")
@Data
@AllArgsConstructor
public class EstadoSolicitudEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEstado")
    private Integer id_estado;
    @Column(nullable = false, length = 100)
    private String estado_actual;
    @Column(nullable = false)
    private Date fecha_registro_estado;

    @OneToOne
    @JoinColumn(name = "idfkSolicitud", referencedColumnName = "idSolicitud", nullable = false)
    private SolicitudEntity objSolicitud;

    public EstadoSolicitudEntity(){
        this.estado_actual = "Enviado";
    }
    
}
