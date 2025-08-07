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

@Entity
@Table(name = "EstadosSolicitudes")
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idfkSolicitud", referencedColumnName = "idSolicitud", nullable = true)
    private SolicitudEntity objSolicitud;

    public EstadoSolicitudEntity(){
        this.estado_actual = "Enviado";
        
    }
    
}
