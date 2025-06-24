package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.CondicionSolicitudVerano;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SolicitudCursoVerano extends Solicitud {

    private String nombre_estudiante;
    private CursoOfertadoVerano objCursoOfertado;
    private CondicionSolicitudVerano codicion_solicitud;
    private String observacion;

}
