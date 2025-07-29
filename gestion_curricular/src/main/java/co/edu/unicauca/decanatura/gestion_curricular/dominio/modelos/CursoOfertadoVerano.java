package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;


import java.util.ArrayList;
import java.util.List;

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
    
    
    private List<EstadoCursoOfertado> estadosCursoOfertados; // Estado del curso ofertado
    private List<Usuario> estudiantesInscritos; // Lista de estudiantes inscritos en el curso

    private List<Solicitud> solicitudes;
    
    public CursoOfertadoVerano() {
        this.estudiantesInscritos = new ArrayList<Usuario>();
        this.estadosCursoOfertados = new ArrayList<EstadoCursoOfertado>();
        this.solicitudes = new ArrayList<Solicitud>();
    }

    
}
