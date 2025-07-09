package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
    //cambio: cambio de relacion one to one a one to many
    @OneToMany( mappedBy = "objSolicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EstadoSolicitudEntity> objEstadoSolicitud = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario")
    private UsuarioEntity objUsuario;

    @OneToMany(mappedBy = "objSolicitud", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    private List<DocumentoEntity> documentos;

    public SolicitudEntity() {
        this.documentos = new ArrayList<DocumentoEntity>();
    }

}
