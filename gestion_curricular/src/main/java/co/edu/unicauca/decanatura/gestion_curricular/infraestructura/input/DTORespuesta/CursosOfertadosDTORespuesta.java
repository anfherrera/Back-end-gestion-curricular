package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CursosOfertadosDTORespuesta {
    
    private Integer id_curso;
    
    // Campo adicional para compatibilidad con pruebas de aceptación
    private Integer idCurso;
    
    // Campos que espera el frontend
    private String codigo_curso;
    private String nombre_curso;
    private String descripcion;
    private String fecha_inicio;
    private String fecha_fin;
    private Integer cupo_maximo;
    private Integer cupo_disponible;
    private String espacio_asignado;
    private String estado;
    private String periodo; // Período académico calculado (ej: "2025-1", "2025-2")

    private MateriaDTORespuesta objMateria;

    private DocenteDTORespuesta objDocente;

    private String grupo;

    private Integer cupo_estimado;

    private String salon; // Número del salón (ej: "221")

    // Información completa del salón (id_salon, edificio, etc.)
    private Integer id_salon; // ID del salón en la base de datos
    private Map<String, Object> salonInfo; // Objeto con información completa del salón

    private List<EstadoCursoOfertadoDTORespuesta> estadosCursoOfertados;

    private List<UsuarioDTORespuesta> estudiantesInscritos;
}
