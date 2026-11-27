package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Inicio de sesión con Google: el ID token ({@code credential}) que entrega Google Identity Services. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginGoogleDTOPeticion {
    @NotBlank(message = "{LoginGoogle.credencial.empty}")
    private String credencial;
}
