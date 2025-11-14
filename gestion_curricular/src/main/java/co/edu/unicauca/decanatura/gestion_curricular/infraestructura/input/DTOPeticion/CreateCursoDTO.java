package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class CreateCursoDTO {
    
    /**
     * ID de la materia (obligatorio)
     * El nombre y código del curso se obtienen automáticamente de la materia
     */
    @NotNull(message = "El ID de la materia es obligatorio")
    private Long id_materia;
    
    /**
     * ID del docente (obligatorio)
     */
    @NotNull(message = "El ID del docente es obligatorio")
    private Long id_docente;
    
    /**
     * Cupo estimado del curso (obligatorio)
     * Valor entre 1 y 100
     */
    @NotNull(message = "El cupo estimado es obligatorio")
    @Min(value = 1, message = "El cupo estimado debe ser mayor a 0")
    @Max(value = 100, message = "El cupo estimado no puede ser mayor a 100")
    private Integer cupo_estimado;
    
    /**
     * Fecha de inicio del curso (obligatorio)
     * Formato ISO 8601: "yyyy-MM-dd'T'HH:mm:ss'Z'" o "yyyy-MM-dd"
     */
    @NotBlank(message = "La fecha de inicio es obligatoria")
    private String fecha_inicio;
    
    /**
     * Fecha de fin del curso (obligatorio)
     * Formato ISO 8601: "yyyy-MM-dd'T'HH:mm:ss'Z'" o "yyyy-MM-dd"
     */
    @NotBlank(message = "La fecha de fin es obligatoria")
    private String fecha_fin;
    
    /**
     * Período académico (obligatorio)
     * Formato: "YYYY-P" (ej: "2025-1", "2025-2")
     * La funcionaria debe seleccionar el período académico
     */
    @NotBlank(message = "El período académico es obligatorio")
    private String periodoAcademico;
    
    /**
     * Espacio asignado (salón) - Opcional
     * Si no se proporciona, se asigna "Aula 101" por defecto
     */
    private String espacio_asignado;
    
    /**
     * Estado inicial del curso - Opcional
     * Valores válidos: 'Borrador', 'Abierto', 'Publicado', 'Preinscripcion', 'Inscripcion', 'Cerrado'
     * Si no se proporciona, se asigna "Abierto" por defecto
     */
    private String estado; // Por defecto: "Abierto"
}
