package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.TipoDocumento;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SolicitudEcaes extends Solicitud {

    private TipoDocumento tipoDocumento;
    private String numero_documento;
    private Date fecha_expedicion;
    private Date fecha_nacimiento;

}
