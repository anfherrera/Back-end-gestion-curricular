package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;
import java.util.List;
public record ListadoEstudiantes(List<EstudianteCurso> estudiantes, int totalPreinscritos, int totalInscritos) {}
