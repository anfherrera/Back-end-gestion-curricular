package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

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

    private RolDTOPeticion rol;

    @NotBlank(message = "{Usuario.codigo.empty}")
    @Size(min = 12, max = 12, message = "{Usuario.codigo.length}")
    @Pattern(message = "{Usuario.codigo.pattern}", regexp = "^1046\\d{8}$")
    private String codigo;

    @NotBlank(message = "{Usuario.correo.empty}")
    @Size(min = 5, max = 50, message = "{Usuario.correo.length}")
    @Pattern(regexp = "^[\\w.%+-]+@unicauca\\.edu\\.co$", message = "{Usuario.correo.unicauca}")
    private String correo;

    @NotBlank(message = "{Usuario.password.empty}")
    @Size(min = 8, max = 20, message = "{Usuario.password.length}")
    private String password;

    private boolean estado_usuario;

    @NotNull(message = "{Usuario.objPrograma.notnull}")
    @Valid
    private ProgramaDTOPeticion objPrograma;
}
