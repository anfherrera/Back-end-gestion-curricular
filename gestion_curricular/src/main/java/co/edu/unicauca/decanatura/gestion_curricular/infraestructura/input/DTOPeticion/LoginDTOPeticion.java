package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTOPeticion {
    @NotBlank(message = "{Login.correo.empty}")
    @Size(min = 5, max = 30, message = "{Login.correo.length}")
    private String correo;
    @NotBlank(message = "{Login.password.empty}")
    @Size(min = 5, max = 20, message = "{Login.password.length}")
    private String password;
}
