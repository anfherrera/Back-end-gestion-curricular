package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MateriaDTORespuesta {
    private Integer id_materia;
    private String codigo;
    private String nombre; 
    private Integer creditos;
}
