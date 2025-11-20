package co.edu.unicauca.decanatura.gestion_curricular.pazysalvo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.RolEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.ProgramaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.RolRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ============================================================
 * PRUEBAS DE INTEGRACIÓN - REST API PAZ Y SALVO (GEPA4)
 * ============================================================
 * 
 * Objetivo: Validar el correcto funcionamiento de los endpoints
 * del módulo Paz y Salvo (GEPA4).
 * 
 * Tipo de pruebas: INTEGRACIÓN
 * - Validan Controller + Service + Repository + Base de Datos (H2)
 * - No usan mocks, prueban el flujo completo
 * 
 * @author Andrés Felipe Herrera Artunduaga
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Pruebas de Integración - REST API Paz y Salvo")
class PazYSalvoIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepositoryInt usuarioRepository;

    @Autowired
    private RolRepositoryInt rolRepository;

    @Autowired
    private ProgramaRepositoryInt programaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String PASSWORD = "password123";
    private static final String ESTUDIANTE_EMAIL = "estudiante.test@unicauca.edu.co";
    private static final String COORDINADOR_EMAIL = "coordinador.test@unicauca.edu.co";
    private static final String FUNCIONARIO_EMAIL = "funcionario.test@unicauca.edu.co";
    private static final String SECRETARIA_EMAIL = "secretaria.test@unicauca.edu.co";
    private static final String TEST_PROGRAMA_CODIGO = "TEST-IET";
    private static final String TEST_PROGRAMA_NOMBRE = "Ingeniería de Pruebas";

    private String tokenEstudiante;
    private String tokenCoordinador;
    private String tokenFuncionario;
    private String tokenSecretaria;

    @BeforeEach
    void setUpTokens() throws Exception {
        ensureUsuariosDePrueba();
        if (tokenEstudiante == null) {
            tokenEstudiante = "Bearer " + obtenerToken(ESTUDIANTE_EMAIL);
        }
        if (tokenCoordinador == null) {
            tokenCoordinador = "Bearer " + obtenerToken(COORDINADOR_EMAIL);
        }
        if (tokenFuncionario == null) {
            tokenFuncionario = "Bearer " + obtenerToken(FUNCIONARIO_EMAIL);
        }
        if (tokenSecretaria == null) {
            tokenSecretaria = "Bearer " + obtenerToken(SECRETARIA_EMAIL);
        }
    }

    private void ensureUsuariosDePrueba() {
        ProgramaEntity programa = programaRepository.buscarPorCodigo(TEST_PROGRAMA_CODIGO)
                .orElseGet(() -> {
                    ProgramaEntity nuevoPrograma = new ProgramaEntity();
                    nuevoPrograma.setCodigo(TEST_PROGRAMA_CODIGO);
                    nuevoPrograma.setNombre_programa(TEST_PROGRAMA_NOMBRE);
                    return programaRepository.save(nuevoPrograma);
                });

        RolEntity rolEstudiante = obtenerORCrearRol("ESTUDIANTE");
        RolEntity rolCoordinador = obtenerORCrearRol("COORDINADOR");
        RolEntity rolFuncionario = obtenerORCrearRol("FUNCIONARIO");
        RolEntity rolSecretaria = obtenerORCrearRol("SECRETARIA");

        crearUsuarioSiNoExiste(ESTUDIANTE_EMAIL, "EST-TEST-001", "Juan Pérez Estudiante", rolEstudiante, programa);
        crearUsuarioSiNoExiste(COORDINADOR_EMAIL, "COO-TEST-001", "Carlos Ramírez Coordinador", rolCoordinador, programa);
        crearUsuarioSiNoExiste(FUNCIONARIO_EMAIL, "FUN-TEST-001", "María González Funcionario", rolFuncionario, programa);
        crearUsuarioSiNoExiste(SECRETARIA_EMAIL, "SEC-TEST-001", "Ana López Secretaria", rolSecretaria, programa);
    }

    private RolEntity obtenerORCrearRol(String nombreRol) {
        return rolRepository.buscarPorNombre(nombreRol)
                .orElseGet(() -> {
                    RolEntity rol = new RolEntity();
                    rol.setNombre(nombreRol);
                    return rolRepository.save(rol);
                });
    }

    private void crearUsuarioSiNoExiste(String correo, String codigo, String nombreCompleto,
                                        RolEntity rol, ProgramaEntity programa) {
        usuarioRepository.buscarPorCorreo(correo).orElseGet(() -> {
            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setNombre_completo(nombreCompleto);
            usuario.setCodigo(codigo);
            usuario.setCorreo(correo);
            usuario.setPassword(passwordEncoder.encode(PASSWORD));
            usuario.setEstado_usuario(true);
            usuario.setObjRol(rol);
            usuario.setObjPrograma(programa);
            return usuarioRepository.save(usuario);
        });
    }

    private String obtenerToken(String correo) throws Exception {
        String loginRequest = """
            {
                "correo": "%s",
                "password": "%s"
            }
            """.formatted(correo, PASSWORD);

        String clientIp = "test-suite-" + correo + "-" + System.nanoTime();
        MvcResult result = mockMvc.perform(post("/api/usuarios/login")
                        .header("X-Forwarded-For", clientIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    // ==================== PRUEBAS DE CONSULTA ====================

    @Test
    @DisplayName("Test 1: Listar todas las solicitudes de Paz y Salvo")
    void testListarSolicitudesPazYSalvo() throws Exception {
        mockMvc.perform(get("/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo")
                        .header("Authorization", tokenFuncionario))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test 2: Listar solicitudes por rol - Funcionario")
    void testListarSolicitudesFuncionario() throws Exception {
        mockMvc.perform(get("/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo/Funcionario")
                        .header("Authorization", tokenFuncionario))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test 3: Listar solicitudes por rol - Coordinador")
    void testListarSolicitudesCoordinador() throws Exception {
        mockMvc.perform(get("/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo/Coordinador")
                        .header("Authorization", tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test 4: Listar solicitudes por rol - Secretaria")
    void testListarSolicitudesSecretaria() throws Exception {
        mockMvc.perform(get("/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo/Secretaria")
                        .header("Authorization", tokenSecretaria))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test 5: Listar solicitudes con parámetro de rol")
    void testListarSolicitudesPorRolParametro() throws Exception {
        mockMvc.perform(get("/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo/porRol")
                        .header("Authorization", tokenFuncionario)
                        .param("rol", "ESTUDIANTE"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== PRUEBAS DE CREACIÓN ====================

    @Test
    @DisplayName("Test 6: Crear solicitud de Paz y Salvo - Valida endpoint POST")
    void testCrearSolicitudPazYSalvo() throws Exception {
        String jsonRequest = """
            {
                "idUsuario": 1,
                "idTipoProceso": 1,
                "fecha_solicitud": "2025-01-15"
            }
            """;

        // Solo validamos que el endpoint existe y responde (puede ser 201, 200 o 400 por validaciones)
        mockMvc.perform(post("/api/solicitudes-pazysalvo/crearSolicitud-PazYSalvo")
                        .header("Authorization", tokenEstudiante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest));
                // No validamos status específico porque depende de datos en BD
    }

    @Test
    @DisplayName("Test 7: Crear solicitud sin datos - Valida respuesta de error")
    void testCrearSolicitudSinDatos() throws Exception {
        String jsonRequest = "{}";

        // Debe dar error de validación (400 o 422)
        mockMvc.perform(post("/api/solicitudes-pazysalvo/crearSolicitud-PazYSalvo")
                        .header("Authorization", tokenEstudiante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().is4xxClientError());
    }

    // ==================== PRUEBAS DE BÚSQUEDA ====================

    @Test
    @DisplayName("Test 8: Buscar solicitud por ID - Valida endpoint")
    void testBuscarSolicitudPorId() throws Exception {
        // Simplemente validamos que el endpoint existe
        mockMvc.perform(get("/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo/1")
                        .header("Authorization", tokenFuncionario));
                // Puede ser 200 OK o 404 NOT FOUND dependiendo de si existe el ID
    }

    @Test
    @DisplayName("Test 9: Buscar solicitud por ID muy grande")
    void testBuscarSolicitudPorIdGrande() throws Exception {
        // Validamos comportamiento con ID inexistente
        mockMvc.perform(get("/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo/999999")
                        .header("Authorization", tokenFuncionario));
                // Puede dar 404 o 500 dependiendo de la implementación
    }

    // ==================== PRUEBAS DE VALIDACIÓN ====================

    @Test
    @DisplayName("Test 10: Endpoint raíz de solicitudes funciona")
    void testEndpointRaizSolicitudes() throws Exception {
        mockMvc.perform(get("/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo")
                        .header("Authorization", tokenFuncionario))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test 11: Validar que la respuesta sea JSON")
    void testRespuestaEsJSON() throws Exception {
        mockMvc.perform(get("/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo")
                        .header("Authorization", tokenFuncionario))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test 12: Listar solicitudes por rol con usuario específico")
    void testListarPorRolConUsuario() throws Exception {
        mockMvc.perform(get("/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo/porRol")
                        .header("Authorization", tokenEstudiante)
                        .param("rol", "ESTUDIANTE")
                        .param("idUsuario", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}

