package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class CreateCursoDTO {
    
    @NotBlank(message = "El nombre del curso es obligatorio")
    private String nombre_curso;
    
    @NotBlank(message = "El código del curso es obligatorio")
    private String codigo_curso;
    
    private String descripcion;
    
    @NotBlank(message = "La fecha de inicio es obligatoria")
    private String fecha_inicio; // ISO string
    
    @NotBlank(message = "La fecha de fin es obligatoria")
    private String fecha_fin; // ISO string
    
    @NotNull(message = "El cupo máximo es obligatorio")
    @Min(value = 1, message = "El cupo máximo debe ser mayor a 0")
    @Max(value = 100, message = "El cupo máximo no puede ser mayor a 100")
    private Integer cupo_maximo;
    
    @NotNull(message = "El cupo estimado es obligatorio")
    @Min(value = 1, message = "El cupo estimado debe ser mayor a 0")
    @Max(value = 100, message = "El cupo estimado no puede ser mayor a 100")
    private Integer cupo_estimado;
    
    private String espacio_asignado;
    
    @NotBlank(message = "El estado es obligatorio")
    private String estado; // 'Abierto', 'Publicado', 'Preinscripcion', 'Inscripcion', 'Cerrado'
    
    @NotNull(message = "El ID de la materia es obligatorio")
    private Long id_materia;
    
    @NotNull(message = "El ID del docente es obligatorio")
    private Long id_docente;
}
