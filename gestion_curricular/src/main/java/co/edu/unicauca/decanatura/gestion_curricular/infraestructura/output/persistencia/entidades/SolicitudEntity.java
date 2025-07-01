package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "Solicitudes")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@AllArgsConstructor
public class SolicitudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSolicitud")
    private Integer id_solicitud;
    @Column(nullable = false, length = 100)
    private String nombre_solicitud;
    @Column(nullable = false)
    private Date fecha_registro_solicitud;

    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE, CascadeType.PERSIST }, mappedBy = "objSolicitud")
    private EstadoSolicitudEntity objEstadoSolicitud;

    @ManyToMany(mappedBy = "solicitudes")
    private Set<UsuarioEntity> usuarios;

    public SolicitudEntity() {
        this.usuarios = new HashSet<UsuarioEntity>();
    }

}
