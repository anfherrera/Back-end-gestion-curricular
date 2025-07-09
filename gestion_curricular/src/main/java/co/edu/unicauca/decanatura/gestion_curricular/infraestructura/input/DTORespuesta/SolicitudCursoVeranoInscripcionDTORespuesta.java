package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SolicitudCursoVeranoInscripcionDTORespuesta extends SolicitudDTORespuesta {
    private String nombre_estudiante;
    private CursosOfertadosDTORespuesta objCursoOfertado;
    private String codicion_solicitud;
    private String observacion;
}
