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
    private String numeroDocumento;
    private Date fechaExpedicion;
    private Date fechaNacimiento;

}
