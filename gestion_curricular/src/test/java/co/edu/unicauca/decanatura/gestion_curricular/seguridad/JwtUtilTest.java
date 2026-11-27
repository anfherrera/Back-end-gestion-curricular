package co.edu.unicauca.decanatura.gestion_curricular.seguridad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Pruebas Unidad - Token de sesión (JWT)")
class JwtUtilTest {

    private static final String SECRETO = "clave_de_prueba_de_al_menos_32_bytes!";

    private final JwtUtil jwt = new JwtUtil(SECRETO, 3_600_000);

    @Test
    @DisplayName("El token lleva el correo del usuario")
    void idaYVuelta() {
        assertThat(jwt.correoDelToken(jwt.generarToken("ana@unicauca.edu.co"))).isEqualTo("ana@unicauca.edu.co");
    }

    @Test
    @DisplayName("Un token vencido se rechaza como vencido")
    void vencido() {
        String token = new JwtUtil(SECRETO, -1000).generarToken("ana@unicauca.edu.co");
        assertThatThrownBy(() -> jwt.correoDelToken(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("Un token firmado con otra clave se rechaza")
    void otraClave() {
        String ajeno = new JwtUtil("otra_clave_distinta_de_al_menos_32_bytes", 3_600_000).generarToken("ana@unicauca.edu.co");
        assertThatThrownBy(() -> jwt.correoDelToken(ajeno)).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Un token vacío o mal formado se rechaza")
    void malFormado() {
        assertThatThrownBy(() -> jwt.correoDelToken("")).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> jwt.correoDelToken("abc.def.ghi")).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("La aplicación no arranca con una clave de menos de 32 bytes")
    void claveCorta() {
        assertThatThrownBy(() -> new JwtUtil("corta", 3_600_000)).isInstanceOf(IllegalStateException.class);
    }
}
