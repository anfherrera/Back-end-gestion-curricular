package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class UpdateCursoDTO {
    
    private String nombre_curso;
    private String codigo_curso;
    private String descripcion;
    private String fecha_inicio; // ISO string
    private String fecha_fin; // ISO string
    
    @Min(value = 1, message = "El cupo máximo debe ser mayor a 0")
    @Max(value = 100, message = "El cupo máximo no puede ser mayor a 100")
    private Integer cupo_maximo;
    
    @Min(value = 1, message = "El cupo estimado debe ser mayor a 0")
    @Max(value = 100, message = "El cupo estimado no puede ser mayor a 100")
    private Integer cupo_estimado;
    
    private String espacio_asignado; // Deprecated: usar id_salon
    private Integer id_salon; // ID del salón seleccionado
    private String estado; // 'Abierto', 'Publicado', 'Preinscripcion', 'Inscripcion', 'Cerrado'
    private Long id_materia;
    private Long id_docente;
}
