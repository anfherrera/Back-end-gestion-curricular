package co.edu.unicauca.decanatura.gestion_curricular.seguridad;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ============================================================
 * PRUEBAS FUNCIONALES - SEGURIDAD
 * ============================================================
 *
 * Objetivo: Validar comportamiento de seguridad de extremo a extremo:
 * - Login con credenciales incorrectas o correctas
 * - Filtrado por rol/estado en listados (solo "Enviada" para funcionario,
 *   solo "APROBADA_FUNCIONARIO" para coordinador)
 *
 * Requiere perfil "test" y datos de prueba (import.sql).
 * Usuario admin en import.sql: admin@unicauca.edu.co / password123 (BCrypt).
 *
 * @author Daniel
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Pruebas Funcionales - Seguridad")
class SeguridadFuncionalTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String LOGIN_URI = "/api/usuarios/login";

    // ==================== LOGIN - CREDENCIALES INCORRECTAS ====================

    @Test
    @DisplayName("Seguridad Funcional 1: Login con usuario inexistente retorna 401")
    void testLoginUsuarioInexistenteRetorna401() throws Exception {
        String json = """
            {"correo": "noexiste@test.com", "password": "clave"}
            """;

        mockMvc.perform(post(LOGIN_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(result2 -> assertThat(result2.getResponse().getContentAsString()).contains("Credenciales incorrectas"));
    }

    @Test
    @DisplayName("Seguridad Funcional 2: Login con contraseña incorrecta retorna 401")
    void testLoginContrasenaIncorrectaRetorna401() throws Exception {
        // Usuario existente en import.sql (admin tiene BCrypt)
        String json = """
            {"correo": "admin@unicauca.edu.co", "password": "clave_incorrecta"}
            """;

        mockMvc.perform(post(LOGIN_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(result2 -> assertThat(result2.getResponse().getContentAsString()).contains("Credenciales incorrectas"));
    }

    // ==================== LOGIN - CREDENCIALES CORRECTAS ====================

    @Test
    @DisplayName("Seguridad Funcional 3: Login con credenciales correctas retorna 200 y token")
    void testLoginCredencialesCorrectasRetorna200YToken() throws Exception {
        // Admin en import.sql: password123 (hash BCrypt); DataInitializer asegura que coincida
        String json = """
            {"correo": "admin@unicauca.edu.co", "password": "password123"}
            """;

        MvcResult result = mockMvc.perform(post(LOGIN_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(body);
        String token = node.get("token").asText();
        assertThat(token).isNotBlank();
    }

    // ==================== FILTRADO POR ROL/ESTADO ====================

    @Test
    @DisplayName("Seguridad Funcional 4: Listado funcionario ECAES solo retorna solicitudes con último estado Enviada")
    void testListadoFuncionarioEcaesSoloEnviada() throws Exception {
        String token = obtenerTokenAdmin();

        String response = mockMvc.perform(get("/api/solicitudes-ecaes/listarSolicitudes-Ecaes/Funcionario")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode list = objectMapper.readTree(response);
        assertThat(list.isArray()).isTrue();
        for (JsonNode solicitud : list) {
            JsonNode estados = solicitud.get("estadosSolicitud");
            assertThat(estados).isNotNull();
            assertThat(estados.isArray()).isTrue();
            if (estados.size() > 0) {
                String ultimoEstado = estados.get(estados.size() - 1).get("estado_actual").asText();
                assertThat(ultimoEstado).isEqualTo("Enviada");
            }
        }
    }

    @Test
    @DisplayName("Seguridad Funcional 5: Listado coordinador ECAES por programa/periodo solo retorna APROBADA_FUNCIONARIO")
    void testListadoCoordinadorEcaesSoloAprobadaFuncionario() throws Exception {
        String token = obtenerTokenAdmin();
        // Coordinador obtiene idPrograma del usuario autenticado; admin es Administrador, puede retornar vacío o según implementación
        String response = mockMvc.perform(get("/api/solicitudes-ecaes/listarSolicitud-ecaes/porRol")
                        .param("rol", "COORDINADOR")
                        .param("periodoAcademico", "2025-2")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode list = objectMapper.readTree(response);
        assertThat(list.isArray()).isTrue();
        for (JsonNode solicitud : list) {
            JsonNode estados = solicitud.get("estadosSolicitud");
            if (estados != null && estados.isArray() && estados.size() > 0) {
                String ultimoEstado = estados.get(estados.size() - 1).get("estado_actual").asText();
                assertThat(ultimoEstado).isEqualTo("APROBADA_FUNCIONARIO");
            }
        }
    }

    @Test
    @DisplayName("Seguridad Funcional 6: Listado funcionario Homologación solo retorna solicitudes con último estado Enviada")
    void testListadoFuncionarioHomologacionSoloEnviada() throws Exception {
        String token = obtenerTokenAdmin();

        String response = mockMvc.perform(get("/api/solicitudes-homologacion/listarSolicitud-Homologacion/Funcionario")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode list = objectMapper.readTree(response);
        assertThat(list.isArray()).isTrue();
        for (JsonNode solicitud : list) {
            JsonNode estados = solicitud.get("estadosSolicitud");
            if (estados != null && estados.isArray() && estados.size() > 0) {
                String ultimoEstado = estados.get(estados.size() - 1).get("estado_actual").asText();
                assertThat(ultimoEstado).isEqualTo("Enviada");
            }
        }
    }

    private String obtenerTokenAdmin() throws Exception {
        String json = """
            {"correo": "admin@unicauca.edu.co", "password": "password123"}
            """;
        MvcResult result = mockMvc.perform(post(LOGIN_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(body);
        return node.get("token").asText();
    }
}
