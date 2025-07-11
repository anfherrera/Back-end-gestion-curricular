package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FechaEcaes {
    private Date inscripcion_est_by_facultad;
    private Date registro_recaudo_ordinario;
    private Date registro_recaudo_extraordinario;
    private Date citacion;
    private Date aplicacion;
    private Date resultados_individuales;

}
