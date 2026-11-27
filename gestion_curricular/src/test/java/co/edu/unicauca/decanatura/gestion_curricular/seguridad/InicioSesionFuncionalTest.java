package co.edu.unicauca.decanatura.gestion_curricular.seguridad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Inicio de sesión de punta a punta: con la cuenta institucional de Google (con un verificador que confía en una
 * llave de prueba en lugar de las de Google) y el bloqueo de las cuentas desactivadas, también con un token
 * que la cuenta ya tenía.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Pruebas Funcionales - Inicio de sesión")
class InicioSesionFuncionalTest {

    private static final TokensGoogleDePrueba GOOGLE = new TokensGoogleDePrueba();
    private static final String ADMIN = "admin@unicauca.edu.co";
    private static final String DESACTIVADO = "desactivado@unicauca.edu.co";
    /** BCrypt de "password123", el mismo del administrador en test-data.sql */
    private static final String HASH_PASSWORD123 = "$2a$10$m32rNR7WwYjS4Tmu08UcpeeOokBlY1OPtQ1h3MgJv70zGeolkD.Qu";

    @TestConfiguration
    static class GoogleDePrueba {
        @Bean
        @Primary
        VerificadorCuentaGoogle verificadorDePrueba() {
            return GOOGLE.verificador();
        }
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtUtil jwtUtil;

    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach
    void crearCuentaDesactivada() {
        jdbc.update("INSERT INTO Usuarios(idUsuario, codigo, nombre_completo, correo, password, estado_usuario, "
                + "idfkRol, idfkPrograma, cedula) VALUES (950, 'INACT950', 'Cuenta Desactivada', ?, ?, 0, 2, 1, "
                + "'950950950')", DESACTIVADO, HASH_PASSWORD123);
    }

    @AfterEach
    void borrarCuentaDesactivada() {
        jdbc.update("DELETE FROM Usuarios WHERE idUsuario = 950");
    }

    // ==================== GOOGLE ====================

    @Test
    @DisplayName("Una cuenta institucional registrada entra con Google y su token sirve en la API")
    void googleCuentaRegistrada() throws Exception {
        String respuesta = loginGoogle(GOOGLE.token(ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.correo").value(ADMIN))
                .andReturn().getResponse().getContentAsString();

        String token = json.readTree(respuesta).get("token").asText();
        mockMvc.perform(get("/api/programas").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Una cuenta institucional sin registro recibe 403 con un mensaje claro")
    void googleCuentaSinRegistro() throws Exception {
        loginGoogle(GOOGLE.token("nuevo.estudiante@unicauca.edu.co"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigoError").value("GC-0007"))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("no está registrada")));
    }

    @Test
    @DisplayName("Una cuenta que no es del dominio recibe 403")
    void googleOtroDominio() throws Exception {
        loginGoogle(GOOGLE.token("alguien@gmail.com", c -> c.claims(claims -> claims.remove("hd"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigoError").value("GC-0007"));
    }

    @Test
    @DisplayName("Una cuenta desactivada no entra con Google")
    void googleCuentaDesactivada() throws Exception {
        loginGoogle(GOOGLE.token(DESACTIVADO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigoError").value("GC-0006"));
    }

    @Test
    @DisplayName("Un token de Google inválido recibe 401")
    void googleTokenInvalido() throws Exception {
        loginGoogle(new TokensGoogleDePrueba().token(ADMIN))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Sin credencial responde 400")
    void googleSinCredencial() throws Exception {
        loginGoogle("")
                .andExpect(status().isBadRequest());
    }

    // ==================== CUENTAS DESACTIVADAS ====================

    @Test
    @DisplayName("Una cuenta desactivada no entra con contraseña aunque sea correcta (403)")
    void contrasenaCuentaDesactivada() throws Exception {
        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + DESACTIVADO + "\",\"password\":\"password123\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigoError").value("GC-0006"));
    }

    @Test
    @DisplayName("El token que tenía una cuenta antes de desactivarla deja de servir")
    void tokenDeCuentaDesactivada() throws Exception {
        String tokenAnterior = jwtUtil.generarToken(DESACTIVADO);
        int estado = mockMvc.perform(get("/api/programas").header("Authorization", "Bearer " + tokenAnterior))
                .andReturn().getResponse().getStatus();
        assertThat(estado).isIn(401, 403);
    }

    @Test
    @DisplayName("Un correo de más de 30 caracteres puede iniciar sesión (antes el límite era 30)")
    void correoLargoValido() throws Exception {
        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"maria.fernanda.lopez.ruiz@unicauca.edu.co\",\"password\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Un login sin contraseña responde 400")
    void loginSinContrasena() throws Exception {
        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + ADMIN + "\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    private ResultActions loginGoogle(String credencial) throws Exception {
        return mockMvc.perform(post("/api/usuarios/login/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(java.util.Map.of("credencial", credencial))));
    }
}
