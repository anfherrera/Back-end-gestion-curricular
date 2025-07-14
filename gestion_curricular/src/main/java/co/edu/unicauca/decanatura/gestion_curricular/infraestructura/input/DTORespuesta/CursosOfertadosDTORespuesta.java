package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CursosOfertadosDTORespuesta {
    
    private Integer id_curso;

    private MateriaDTORespuesta objMateria;

    private DocenteDTORespuesta objDocente;

    private String grupo;

    private Integer cupo_estimado;

    private String salon;

    private List<EstadoCursoOfertadoDTORespuesta> estadosCursoOfertados;

    private Set<UsuarioDTORespuesta> estudiantesInscritos;
}
