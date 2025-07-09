package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProgramaDTOPeticion {

    private Integer id_programa;

    @NotBlank(message = "{Programa.codigo.empty}")
    @Size(min = 3, max = 10, message = "{Programa.codigo.length}")
    private String codigo;

    @NotBlank(message = "{Programa.nombre.empty}")
    @Size(min = 5, max = 100, message = "{Programa.nombre.length}")
    private String nombre_programa;
}
