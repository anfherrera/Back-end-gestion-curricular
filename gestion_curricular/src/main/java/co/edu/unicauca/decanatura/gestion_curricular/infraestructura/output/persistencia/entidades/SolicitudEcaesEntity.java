package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;

import java.util.Date;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.Enums.TipoDocumentoEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Solicitudes_Ecaes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SolicitudEcaesEntity extends SolicitudEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDocumentoEntity tipoDocumento;
    @Column(nullable = false, length = 100)
    private String numero_documento;
    @Column(nullable = false)
    private Date fecha_expedicion;
    @Column(nullable = false, length = 100)
    private String ruta_documento;
    // @Lob
    // @Column(name = "archivo_datos")
     //private byte[] archivo_datos; //se puede usar si se quiere almacenar el documento en la base de datos


}
