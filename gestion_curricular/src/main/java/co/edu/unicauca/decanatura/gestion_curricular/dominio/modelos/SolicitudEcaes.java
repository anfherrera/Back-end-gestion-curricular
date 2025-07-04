package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.Date;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.TipoDocumento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SolicitudEcaes extends Solicitud {

    private TipoDocumento tipoDocumento;
    private String numero_documento;
    private Date fecha_expedicion;
    private String ruta_documento;
    //@Lob
    //@Column(name = "archivo_datos")
    //private byte[] archivo_datos; //se puede usar si se quiere almacenar el documento en la base de datos

}
