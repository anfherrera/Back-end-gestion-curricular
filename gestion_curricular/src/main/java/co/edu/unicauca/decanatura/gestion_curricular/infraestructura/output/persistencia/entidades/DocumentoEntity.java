package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;

import java.util.Date;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.Enums.TipoDocumentoSolicitudPazYSalvoEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "Documentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDocumento")
    private Integer id_documento;
    @Column(nullable = false, length = 100)
    private String nombre;
    @Column(nullable = false, length = 100)
    private String ruta_documento;
    @Column(nullable = false)
    private Date fecha_documento;
    @Column(columnDefinition = "TINYINT", length = 1)
    private boolean esValido;
    @Column(length = 100)
    private String comentario; // comentario de funcionario o coordinador

    @Enumerated(EnumType.STRING)
    private TipoDocumentoSolicitudPazYSalvoEntity tipoDocumentoSolicitudPazYSalvo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSolicitud")
    private SolicitudEntity objSolicitud;
    
}
