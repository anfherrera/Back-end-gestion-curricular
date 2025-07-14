package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RolDTOPeticion {

    private Integer id_rol;

    @NotBlank(message = "{Rol.nombre.empty}")
    @Size(min = 3, max = 30, message = "{Rol.nombre.length}")
    private String nombre;

    
}
