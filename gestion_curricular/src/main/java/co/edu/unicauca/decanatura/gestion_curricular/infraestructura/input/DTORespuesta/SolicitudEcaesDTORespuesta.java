package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SolicitudEcaesDTORespuesta extends SolicitudDTORespuesta  {

    private String tipoDocumento;
    private String numero_documento;
    private Date fecha_expedicion;
    private Date fecha_nacimiento;
}
