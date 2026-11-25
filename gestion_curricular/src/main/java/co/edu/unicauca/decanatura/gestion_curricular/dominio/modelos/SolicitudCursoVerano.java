package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.CondicionSolicitudVerano;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SolicitudCursoVerano extends Solicitud {

    private String nombreEstudiante;
    private CursoOfertadoVerano objCursoOfertado;
    private CondicionSolicitudVerano condicionSolicitud;
    private String observacion;

}
