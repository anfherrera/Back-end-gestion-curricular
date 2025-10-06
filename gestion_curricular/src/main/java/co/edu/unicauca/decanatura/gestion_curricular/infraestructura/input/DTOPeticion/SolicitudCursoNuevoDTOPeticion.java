package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudCursoNuevoDTOPeticion {

    @NotBlank(message = "El nombre completo no puede estar vacío")
    @Size(min = 3, max = 100, message = "El nombre completo debe tener entre 3 y 100 caracteres")
    private String nombreCompleto;

    @NotBlank(message = "El código no puede estar vacío")
    @Size(min = 6, max = 20, message = "El código debe tener entre 6 y 20 caracteres")
    private String codigo;

    @NotBlank(message = "El curso no puede estar vacío")
    @Size(min = 3, max = 100, message = "El nombre del curso debe tener entre 3 y 100 caracteres")
    private String curso;

    @NotBlank(message = "La condición no puede estar vacía")
    @Size(min = 3, max = 50, message = "La condición debe tener entre 3 y 50 caracteres")
    private String condicion;

    @NotNull(message = "El ID del usuario no puede ser nulo")
    @Min(value = 1, message = "El ID del usuario debe ser mayor a 0")
    private Integer idUsuario;
}
