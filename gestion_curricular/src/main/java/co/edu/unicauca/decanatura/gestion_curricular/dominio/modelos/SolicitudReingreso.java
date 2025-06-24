package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SolicitudReingreso extends Solicitud {

    private String ruta_PM_FO_4_FOR_17;
    private String ruta_pazysalvo_academico;
    private String ruta_pazysalvo_financiero;
    
}
