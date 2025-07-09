package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MateriaDTOPeticion {
    private Integer id_materia;

    @NotBlank(message = "{Materia.codigo.empty}")
    @Size(min = 3, max = 10, message = "{Materia.codigo.length}")
    private String codigo;

    @NotBlank(message = "{Materia.nombre.empty}")
    @Size(min = 3, max = 50, message = "{Materia.nombre.length}")
    private String nombre;

    @NotNull(message = "{Materia.creditos.empty}")
    @Min(value = 1, message = "{Materia.creditos.min}")
    @Max(value = 10, message = "{Materia.creditos.max}")
    private Integer creditos;
}
