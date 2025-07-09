package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocenteDTORespuesta {
    private Integer id_docente;
    private String codigo_docente;
    private String nombre_docente;
}
