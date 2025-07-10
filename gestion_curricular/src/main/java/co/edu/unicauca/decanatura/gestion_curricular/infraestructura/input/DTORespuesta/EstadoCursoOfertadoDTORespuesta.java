package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadoCursoOfertadoDTORespuesta {
    private Integer id_estado;
    private String estado_actual;
    private Date fecha_registro_estado;

    // Si deseas incluir información del curso ofertado, puedes agregarlo así:
    private CursosOfertadosDTORespuesta objCursoOfertadoVerano;
}
