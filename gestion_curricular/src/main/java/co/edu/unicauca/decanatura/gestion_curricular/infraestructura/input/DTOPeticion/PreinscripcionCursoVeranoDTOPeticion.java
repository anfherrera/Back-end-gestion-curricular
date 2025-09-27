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
public class PreinscripcionCursoVeranoDTOPeticion {

    @NotNull(message = "El ID del usuario no puede ser nulo")
    @Min(value = 1, message = "El ID del usuario debe ser mayor a 0")
    private Integer idUsuario;

    @NotNull(message = "El ID del curso no puede ser nulo")
    @Min(value = 1, message = "El ID del curso debe ser mayor a 0")
    private Integer idCurso;

    @NotBlank(message = "El nombre de la solicitud no puede estar vacío")
    @Size(min = 3, max = 100, message = "El nombre de la solicitud debe tener entre 3 y 100 caracteres")
    private String nombreSolicitud;
}
