package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CursoDisponibleDTORespuesta {
    
    private Integer id_curso;
    private String nombre_curso;
    private String codigo_curso;
    private Integer creditos;
    private String descripcion;
}
