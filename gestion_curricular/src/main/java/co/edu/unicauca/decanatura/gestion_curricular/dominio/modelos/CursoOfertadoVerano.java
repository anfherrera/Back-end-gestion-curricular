package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;


import java.util.HashSet;
import java.util.Set;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.GrupoCursoVerano;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CursoOfertadoVerano {
    private Integer id_curso;
    private Materia objMateria;
    private Docente objDocente;
    private GrupoCursoVerano grupo;
    private Integer cupo_estimado; // Valor mínimo para abrir un curso
    private String salon;
    

    private EstadoCursoOfertado objEstadoCursoOfertado; // Estado del curso ofertado
    private Set<Usuario> estudiantesInscritos; // Lista de estudiantes inscritos en el curso

    public CursoOfertadoVerano() {
        this.estudiantesInscritos = new HashSet<Usuario>();
    }

    
}
