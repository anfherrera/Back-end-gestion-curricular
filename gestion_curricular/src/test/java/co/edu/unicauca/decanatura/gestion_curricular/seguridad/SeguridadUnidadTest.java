package co.edu.unicauca.decanatura.gestion_curricular.seguridad;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ============================================================
 * PRUEBAS UNITARIAS - SEGURIDAD
 * ============================================================
 *
 * Objetivo: Validar reglas de seguridad de forma aislada (sin Spring Context).
 *
 * Tipo de pruebas: UNITARIAS
 * - Cifrado de contraseñas (BCrypt)
 * - Validación de que una contraseña en texto plano no coincide con el hash
 * - Que el mismo texto genera hashes diferentes (salt)
 *
 * Para pruebas de login (401 con credenciales incorrectas) y filtrado por rol/estado,
 * usar SeguridadFuncionalTest o SeguridadIntegracionTest.
 *
 * @author Daniel
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - Seguridad")
class SeguridadUnidadTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("Seguridad 1: Contraseña se codifica con BCrypt (no se guarda en texto plano)")
    void testPasswordSeCodificaConBCrypt() {
        String passwordPlano = "password123";
        String hash = passwordEncoder.encode(passwordPlano);

        assertThat(hash).isNotEqualTo(passwordPlano);
        assertThat(hash).startsWith("$2"); // BCrypt empieza con $2a$ o $2b$
    }

    @Test
    @DisplayName("Seguridad 2: matches() valida correctamente contraseña correcta")
    void testMatchesValidaContrasenaCorrecta() {
        String passwordPlano = "miClaveSecreta";
        String hash = passwordEncoder.encode(passwordPlano);

        assertThat(passwordEncoder.matches(passwordPlano, hash)).isTrue();
    }

    @Test
    @DisplayName("Seguridad 3: matches() rechaza contraseña incorrecta")
    void testMatchesRechazaContrasenaIncorrecta() {
        String passwordPlano = "password123";
        String hash = passwordEncoder.encode(passwordPlano);

        assertThat(passwordEncoder.matches("otraClave", hash)).isFalse();
        assertThat(passwordEncoder.matches("", hash)).isFalse();
    }

    @Test
    @DisplayName("Seguridad 4: Mismo texto genera hashes distintos (salt)")
    void testMismoTextoGeneraHashesDistintos() {
        String password = "password123";
        String hash1 = passwordEncoder.encode(password);
        String hash2 = passwordEncoder.encode(password);

        assertThat(hash1).isNotEqualTo(hash2);
        assertThat(passwordEncoder.matches(password, hash1)).isTrue();
        assertThat(passwordEncoder.matches(password, hash2)).isTrue();
    }
}
