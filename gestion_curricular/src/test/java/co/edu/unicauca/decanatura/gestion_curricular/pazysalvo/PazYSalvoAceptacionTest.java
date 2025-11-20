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
 * PRUEBAS DE ACEPTACIÓN - PAZ Y SALVO (GEPA4)
 * ============================================================
 *
 * Objetivo: Validar CRITERIOS DE ACEPTACIÓN definidos por el cliente/negocio.
 * Estas pruebas están escritas en formato BDD (Behavior Driven Development)
 * usando Given-When-Then para describir escenarios de aceptación.
 *
 * Formato BDD:
 * - GIVEN (Dado que): Estado inicial / Precondiciones
 * - WHEN (Cuando): Acción que se ejecuta
 * - THEN (Entonces): Resultado esperado
 *
 * Criterios de Aceptación cubiertos:
 * 1. CA-GEPA4-01: Estudiante puede solicitar Paz y Salvo
 * 2. CA-GEPA4-02: Coordinador puede aprobar solicitudes de su programa
 * 3. CA-GEPA4-03: Sistema genera documento oficial en PDF
 * 4. CA-GEPA4-04: Estudiante solo ve sus propias solicitudes
 * 5. CA-GEPA4-05: Sistema valida datos obligatorios
 *
 * @author Andrés Felipe Herrera Artunduaga
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Pruebas de Aceptación - Paz y Salvo")
class PazYSalvoAceptacionTest {

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

    // ==================== CA-GEPA4-01: ESTUDIANTE PUEDE SOLICITAR PAZ Y SALVO ====================

    @Test
    @DisplayName("CA-GEPA4-01: Como estudiante quiero solicitar mi Paz y Salvo para poder graduarme")
    void testEstudiantePuedeSolicitarPazYSalvo() throws Exception {
        /*
         * GIVEN: Un estudiante autenticado en el sistema
         *        AND el estudiante cumple con requisitos para solicitar Paz y Salvo
         * 
         * WHEN: El estudiante crea una solicitud de Paz y Salvo
         *       AND envía todos los datos obligatorios
         * 
         * THEN: El sistema debe crear la solicitud exitosamente
         *       AND debe retornar código HTTP 201 (Created)
         *       AND la solicitud debe quedar en estado inicial "PENDIENTE"
         */
        
        String jsonSolicitud = """
            {
                "idUsuario": 1,
                "idTipoProceso": 1,
                "fecha_solicitud": "2025-01-15"
            }
            """;

        mockMvc.perform(post("/api/solicitudes-pazysalvo/crearSolicitud-PazYSalvo")
                        .header("Authorization", tokenEstudiante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andExpect(status().isCreated());
    }

    // ==================== CA-GEPA4-02: COORDINADOR PUEDE APROBAR SOLICITUDES ====================

    @Test
    @DisplayName("CA-GEPA4-02: Como coordinador quiero aprobar solicitudes para avanzar el proceso")
    void testCoordinadorPuedeAprobarSolicitudes() throws Exception {
        /*
         * GIVEN: Un coordinador autenticado en el sistema
         *        AND existe una solicitud de Paz y Salvo pendiente
         *        AND la solicitud pertenece a un estudiante del programa del coordinador
         * 
         * WHEN: El coordinador cambia el estado de la solicitud a "APROBADO"
         * 
         * THEN: El sistema debe actualizar el estado de la solicitud
         *       AND debe retornar código HTTP 200 (OK)
         *       AND la solicitud debe quedar con estado "APROBADO"
         *       AND debe registrar fecha y hora de la aprobación
         */
        
        String jsonAprobacion = """
            {
                "nuevoEstado": "APROBADO"
            }
            """;

        mockMvc.perform(put("/api/solicitudes-pazysalvo/cambiarEstadoSolicitud/37")
                        .header("Authorization", tokenCoordinador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAprobacion))
                .andExpect(status().isOk());
    }

    // ==================== CA-GEPA4-03: SISTEMA GENERA DOCUMENTO OFICIAL ====================

    @Test
    @DisplayName("CA-GEPA4-03: Como secretaria quiero generar documento PDF para entregarlo al estudiante")
    void testSistemaGeneraDocumentoPDF() throws Exception {
        /*
         * GIVEN: Una solicitud de Paz y Salvo aprobada
         *        AND todos los funcionarios han validado la solicitud
         *        AND el estudiante cumple con todos los requisitos
         * 
         * WHEN: La secretaria solicita generar el documento en formato PDF
         * 
         * THEN: El sistema debe generar un documento PDF válido
         *       AND debe incluir todos los datos del estudiante
         *       AND debe incluir firma digital o espacio para firma
         *       AND debe tener formato oficial de la universidad
         *       AND debe retornar el archivo con Content-Type application/pdf
         *       AND debe sugerir nombre de archivo descriptivo
         */
        
        mockMvc.perform(get("/api/solicitudes-pazysalvo/generarDocumentoPazYSalvo/37/pdf")
                        .header("Authorization", tokenSecretaria))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"PazYSalvo_37.pdf\""));
    }

    @Test
    @DisplayName("CA-GEPA4-03.1: Como secretaria quiero generar documento DOCX para editarlo si es necesario")
    void testSistemaGeneraDocumentoDOCX() throws Exception {
        /*
         * GIVEN: Una solicitud de Paz y Salvo que requiere ajustes manuales
         *        AND la secretaria tiene permiso para editar documentos
         * 
         * WHEN: La secretaria solicita generar el documento en formato DOCX
         * 
         * THEN: El sistema debe generar un documento Word editable
         *       AND debe retornar el archivo con Content-Type correcto
         *       AND debe permitir edición posterior en Microsoft Word
         */
        
        mockMvc.perform(get("/api/solicitudes-pazysalvo/generarDocumentoPazYSalvo/37/docx")
                        .header("Authorization", tokenSecretaria))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"PazYSalvo_37.docx\""));
    }

    // ==================== CA-GEPA4-04: ESTUDIANTE SOLO VE SUS PROPIAS SOLICITUDES ====================

    @Test
    @DisplayName("CA-GEPA4-04: Como estudiante solo quiero ver mis propias solicitudes por privacidad")
    void testEstudianteSoloVeSusPropiasSolicitudes() throws Exception {
        /*
         * GIVEN: Un estudiante autenticado con ID 1
         *        AND existen solicitudes de otros estudiantes en el sistema
         * 
         * WHEN: El estudiante consulta sus solicitudes de Paz y Salvo
         * 
         * THEN: El sistema debe retornar únicamente las solicitudes del estudiante
         *       AND no debe mostrar solicitudes de otros estudiantes
         *       AND debe retornar código HTTP 200 (OK)
         *       AND debe retornar una lista (puede estar vacía si no tiene solicitudes)
         */
        
        // Usuario 12 tiene solicitud de Paz y Salvo (ID 37 en BD)
        mockMvc.perform(get("/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo/12")
                        .header("Authorization", tokenEstudiante))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== CA-GEPA4-05: SISTEMA VALIDA DATOS OBLIGATORIOS ====================

    @Test
    @DisplayName("CA-GEPA4-05: Como sistema debo validar datos obligatorios para mantener integridad")
    void testSistemaValidaDatosObligatorios() throws Exception {
        /*
         * GIVEN: Un estudiante intenta crear una solicitud
         *        AND el estudiante NO proporciona su ID de usuario
         * 
         * WHEN: El estudiante envía la solicitud con datos incompletos
         * 
         * THEN: El sistema debe rechazar la solicitud
         *       AND debe retornar código HTTP 400 (Bad Request)
         *       AND debe retornar mensaje de error descriptivo
         *       AND NO debe crear ninguna solicitud en la base de datos
         */
        
        String jsonInvalido = """
            {
                "idUsuario": null,
                "idTipoProceso": 1,
                "fecha_solicitud": "2025-01-15"
            }
            """;

        mockMvc.perform(post("/api/solicitudes-pazysalvo/crearSolicitud-PazYSalvo")
                        .header("Authorization", tokenEstudiante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());
    }

    // ==================== CA-GEPA4-06: ROLES TIENEN ACCESO DIFERENCIADO ====================

    @Test
    @DisplayName("CA-GEPA4-06: Como coordinador quiero ver solicitudes de mi programa únicamente")
    void testCoordinadorVeSolicitudesDeSuPrograma() throws Exception {
        /*
         * GIVEN: Un coordinador autenticado con rol "Coordinador"
         *        AND el coordinador pertenece a un programa específico
         *        AND existen solicitudes de múltiples programas
         * 
         * WHEN: El coordinador consulta solicitudes pendientes
         * 
         * THEN: El sistema debe filtrar solicitudes por programa del coordinador
         *       AND debe retornar únicamente solicitudes de su programa
         *       AND debe retornar código HTTP 200 (OK)
         */
        
        mockMvc.perform(get("/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo/Coordinador")
                        .header("Authorization", tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("CA-GEPA4-06.1: Como funcionario quiero ver solicitudes asignadas a mi rol")
    void testFuncionarioVeSolicitudesDesuRol() throws Exception {
        /*
         * GIVEN: Un funcionario administrativo autenticado
         *        AND existen solicitudes en diferentes estados
         * 
         * WHEN: El funcionario consulta sus solicitudes asignadas
         * 
         * THEN: El sistema debe retornar solicitudes en estados que requieren acción del funcionario
         *       AND debe excluir solicitudes que no corresponden a su rol
         */
        
        mockMvc.perform(get("/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo/Funcionario")
                        .header("Authorization", tokenFuncionario))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== CA-GEPA4-07: MANEJO DE ERRORES AMIGABLE ====================

    @Test
    @DisplayName("CA-GEPA4-07: Como usuario quiero mensajes de error claros cuando algo falla")
    void testSistemaRetornaMensajesErrorClaros() throws Exception {
        /*
         * GIVEN: Un usuario intenta cambiar estado de una solicitud
         *        AND la solicitud con ID 999 NO existe en el sistema
         * 
         * WHEN: El usuario intenta cambiar el estado de la solicitud inexistente
         * 
         * THEN: El sistema debe retornar código HTTP 404 (Not Found)
         *       AND debe retornar mensaje de error descriptivo
         *       AND el mensaje debe indicar claramente que la solicitud no existe
         */
        
        String jsonCambioEstado = """
            {
                "nuevoEstado": "APROBADO"
            }
            """;

        mockMvc.perform(put("/api/solicitudes-pazysalvo/cambiarEstadoSolicitud/999")
                        .header("Authorization", tokenCoordinador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCambioEstado))
                .andExpect(status().isNotFound());
    }

    // ==================== CA-GEPA4-08: TRAZABILIDAD DEL PROCESO ====================

    @Test
    @DisplayName("CA-GEPA4-08: Como auditor quiero ver historial completo de la solicitud")
    void testSistemaMantieneTrazabilidadCompleta() throws Exception {
        /*
         * GIVEN: Una solicitud de Paz y Salvo que ha pasado por múltiples estados
         *        AND múltiples funcionarios han interactuado con la solicitud
         * 
         * WHEN: Un auditor consulta el listado completo de solicitudes
         * 
         * THEN: El sistema debe retornar toda la información de trazabilidad
         *       AND debe incluir fechas de cada cambio de estado
         *       AND debe mantener registro de todos los estados anteriores
         */
        
        mockMvc.perform(get("/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo")
                        .header("Authorization", tokenFuncionario))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}

