package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.GrupoCursoVerano;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CursoOfertadoVerano {
    private Integer id_curso;
    private Materia objMateria;
    private Docente objDocente;
    private GrupoCursoVerano grupo;
    private Integer cupo_estimado; // Valor mínimo para abrir un curso
    private String salon;
    
}
