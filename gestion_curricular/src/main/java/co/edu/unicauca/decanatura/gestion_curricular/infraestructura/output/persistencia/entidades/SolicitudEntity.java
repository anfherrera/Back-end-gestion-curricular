package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "Solicitudes")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"estadosSolicitud", "objUsuario", "documentos", "objCursoOfertadoVerano"})
public class SolicitudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSolicitud")
    private Integer id_solicitud;
    @Column(nullable = false, length = 100)
    private String nombre_solicitud;

    @Column(nullable = true, length = 10)
    private String periodo_academico; // Período académico de la solicitud (ej: "2024-2")

    @Column(nullable = true)
    private Date fecha_ceremonia; // Fecha de la ceremonia de graduación (para filtrar por ceremonias: marzo, junio, diciembre, etc.)

    @Column(nullable = false)
    private Date fecha_registro_solicitud;

    private Boolean esSeleccionado;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "objSolicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EstadoSolicitudEntity> estadosSolicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario")
    private UsuarioEntity objUsuario;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "objSolicitud", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    private List<DocumentoEntity> documentos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCurso")
    private CursoOfertadoVeranoEntity objCursoOfertadoVerano;

    public SolicitudEntity() {
        this.documentos = new ArrayList<DocumentoEntity>();
        this.estadosSolicitud = new ArrayList<EstadoSolicitudEntity>();
    }

}
