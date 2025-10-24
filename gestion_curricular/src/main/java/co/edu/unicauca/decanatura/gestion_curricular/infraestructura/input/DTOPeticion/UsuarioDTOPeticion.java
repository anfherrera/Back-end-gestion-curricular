package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDTOPeticion {

    private Integer id_usuario;

    @NotBlank(message = "{Usuario.nombre_completo.empty}")
    @Size(min = 3, max = 100, message = "{Usuario.nombre_completo.length}")
    private String nombre_completo;

    @NotNull(message = "{Usuario.rol.notnull}")
    @Min(value = 1, message = "{Usuario.rol.min}")
    private Integer id_rol;

    @NotBlank(message = "{Usuario.codigo.empty}")
    @Size(min = 3, max = 50, message = "{Usuario.codigo.length}")
    private String codigo;

    @NotBlank(message = "{Usuario.correo.empty}")
    @Size(min = 5, max = 50, message = "{Usuario.correo.length}")
    @Pattern(regexp = "^[\\w.%+-]+@unicauca\\.edu\\.co$", message = "{Usuario.correo.unicauca}")
    private String correo;

    private String password;

    private boolean estado_usuario;

    @NotNull(message = "{Usuario.objPrograma.notnull}")
    @Min(value = 1, message = "{Usuario.programa.min}")
    private Integer id_programa;
}
