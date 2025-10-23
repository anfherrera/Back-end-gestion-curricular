package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocenteDTOPeticion {
    private Integer id_docente;

    @NotBlank(message = "{Docente.codigo.empty}")
    @Size(min = 3, max = 12, message = "{Docente.codigo.length}")
    private String codigo_docente;

    @NotBlank(message = "{Docente.nombre.empty}")
    @Size(min = 3, max = 100, message = "{Docente.nombre.length}")
    private String nombre_docente;
}
