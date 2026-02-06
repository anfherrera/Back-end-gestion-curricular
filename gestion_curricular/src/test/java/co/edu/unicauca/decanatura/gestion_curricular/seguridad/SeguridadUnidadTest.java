package co.edu.unicauca.decanatura.gestion_curricular.seguridad;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas unitarias de seguridad: codificación y verificación de contraseñas con BCrypt.
 */
@DisplayName("Pruebas Unidad - Seguridad (BCrypt)")
class SeguridadUnidadTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("BCrypt: encode genera hash no vacío y distinto al texto plano")
    void bcryptEncodeGeneraHash() {
        String password = "password123";
        String hash = encoder.encode(password);
        assertThat(hash).isNotBlank();
        assertThat(hash).isNotEqualTo(password);
        assertThat(hash).startsWith("$2a$"); // formato BCrypt
    }

    @Test
    @DisplayName("BCrypt: matches verifica correctamente contraseña correcta")
    void bcryptMatchesContrasenaCorrecta() {
        String password = "password123";
        String hash = encoder.encode(password);
        assertThat(encoder.matches(password, hash)).isTrue();
    }

    @Test
    @DisplayName("BCrypt: matches rechaza contraseña incorrecta")
    void bcryptMatchesRechazaContrasenaIncorrecta() {
        String password = "password123";
        String hash = encoder.encode(password);
        assertThat(encoder.matches("otraClave", hash)).isFalse();
    }

    @Test
    @DisplayName("BCrypt: mismo texto genera hashes distintos (salt aleatorio)")
    void bcryptGeneraHashesDistintosPorSalt() {
        String password = "password123";
        String hash1 = encoder.encode(password);
        String hash2 = encoder.encode(password);
        assertThat(hash1).isNotEqualTo(hash2);
        assertThat(encoder.matches(password, hash1)).isTrue();
        assertThat(encoder.matches(password, hash2)).isTrue();
    }
}
