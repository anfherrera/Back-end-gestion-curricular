package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProgramaDTORespuesta {
    private Integer id_programa;
    private String codigo;
    private String nombre_programa;
}
