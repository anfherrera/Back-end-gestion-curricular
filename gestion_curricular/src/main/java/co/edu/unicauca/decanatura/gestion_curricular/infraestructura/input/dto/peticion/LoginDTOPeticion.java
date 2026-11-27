package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTOPeticion {
    /** Mismo límite que el correo al crear el usuario. */
    @NotBlank(message = "{Login.correo.empty}")
    @Size(min = 5, max = 50, message = "{Login.correo.length}")
    private String correo;
    @NotBlank(message = "{Login.password.empty}")
    private String password;
}
