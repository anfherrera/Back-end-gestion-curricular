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
@Table(name = "Documentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDocumento")
    private Integer id_documento;
    /** Longitud suficiente para nombres largos (ej. Autorización para publicar y permitir la consulta y uso de obras en el Repositorio Institucional.pdf) */
    @Column(nullable = true, length = 255)
    private String nombre;
    /** Ruta completa puede ser pazysalvo/solicitud_X/nombre_largo.pdf */
    @Column(nullable = true, length = 500)
    private String ruta_documento;
    @Column(nullable = true)
    private Date fecha_documento;
    @Column(columnDefinition = "TINYINT", length = 1)
    private boolean esValido;
    /** Historial de comentarios (concatenados, multilínea); longitud ampliada para varios comentarios con fecha y usuario */
    @Column(length = 2000)
    private String comentario; // comentario de funcionario o coordinador

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSolicitud")
    private SolicitudEntity objSolicitud;
    
}
