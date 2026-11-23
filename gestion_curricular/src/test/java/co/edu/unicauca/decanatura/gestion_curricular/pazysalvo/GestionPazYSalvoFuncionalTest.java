package co.edu.unicauca.decanatura.gestion_curricular.pazysalvo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.RolEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.ProgramaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.RolRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;

/**
 * ============================================================
 * PRUEBAS FUNCIONALES - GESTIÓN DE PAZ Y SALVO (GEPA4)
 * ============================================================
 *
 * Objetivo: Validar FLUJOS COMPLETOS DE NEGOCIO del módulo de Paz y Salvo.
 * Estas pruebas se enfocan en casos de uso end-to-end desde la perspectiva
 * del usuario/negocio, no solo en componentes técnicos.
 *
 * Diferencia con Pruebas de Integración:
 * - Integración: Verifica que componentes técnicos funcionen juntos
 * - Funcional: Verifica que FUNCIONALIDADES DE NEGOCIO funcionen correctamente
 *
 * Escenarios de negocio cubiertos:
 * 1. Flujo completo de solicitud de Paz y Salvo
 * 2. Flujo de aprobación por diferentes roles
 * 3. Flujo de generación de documentos oficiales
 * 4. Flujo de consulta por diferentes actores
 *
 * @author Andrés Felipe Herrera Artunduaga
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Pruebas Funcionales - Gestión de Paz y Salvo")
class GestionPazYSalvoFuncionalTest {

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
        // Cada prueba empieza sin solicitudes en tramite del estudiante (regla: una a la vez)
        SoportePazYSalvo.cerrarSolicitudesEnTramite(mockMvc, objectMapper, tokenEstudiante,
                tokenFuncionario, tokenCoordinador, tokenSecretaria);
    }

    private void ensureUsuariosDePrueba() {
        ProgramaEntity programa = programaRepository.buscarPorCodigo(TEST_PROGRAMA_CODIGO)
                .orElseGet(() -> {
                    ProgramaEntity nuevoPrograma = new ProgramaEntity();
                    nuevoPrograma.setCodigo(TEST_PROGRAMA_CODIGO);
                    nuevoPrograma.setNombre_programa(TEST_PROGRAMA_NOMBRE);
                    return programaRepository.save(nuevoPrograma);
                });

        RolEntity rolEstudiante = obtenerORCrearRol("Estudiante");
        RolEntity rolCoordinador = obtenerORCrearRol("Coordinador");
        RolEntity rolFuncionario = obtenerORCrearRol("Funcionario");
        RolEntity rolSecretaria = obtenerORCrearRol("Secretario");

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
            usuario.setCedula("T" + Math.abs(correo.hashCode()));
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

    // ==================== FLUJO 1: SOLICITUD COMPLETA DE PAZ Y SALVO ====================

    @Test
    @DisplayName("Funcional 1: Flujo completo - Estudiante solicita Paz y Salvo")
    void testFlujoCompletoSolicitudEstudiante() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Estudiante crea una solicitud de Paz y Salvo
         * 2. El sistema registra la solicitud con estado inicial
         * 3. La solicitud queda disponible para revisión por funcionarios
         * 
         * ACTORES: Estudiante
         * RESULTADO ESPERADO: Solicitud creada exitosamente
         */
        
        String jsonSolicitud = """
            {
                "idUsuario": 1,
                "idTipoProceso": 1,
                "fecha_solicitud": "2025-01-15"
            }
            """;

        // Paso 1: Crear solicitud
        mockMvc.perform(post("/api/solicitudes-pazysalvo")
                        .header("Authorization", tokenEstudiante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andExpect(status().isCreated());

        // Paso 2: Verificar que la solicitud aparece en la bandeja de pendientes del funcionario
        mockMvc.perform(get("/api/solicitudes-pazysalvo?rol=funcionario&bandeja=pendientes")
                        .header("Authorization", tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== FLUJO 2: APROBACIÓN POR ROLES ====================

    @Test
    @DisplayName("Funcional 2: Flujo de aprobación - Coordinador aprueba solicitud")
    void testFlujoAprobacionCoordinador() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Coordinador consulta solicitudes pendientes de su área
         * 2. Coordinador revisa una solicitud específica
         * 3. Coordinador aprueba la solicitud
         * 
         * ACTORES: Coordinador del programa
         * RESULTADO ESPERADO: Solicitud aprobada y disponible para siguiente nivel
         */
        
        // Paso 1: Coordinador consulta solicitudes pendientes
        mockMvc.perform(get("/api/solicitudes-pazysalvo?rol=coordinador&bandeja=pendientes")
                        .header("Authorization", tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        // Paso 2: El coordinador aprueba una solicitud que el funcionario ya aprobó
        // (el flujo es Funcionario -> Coordinador -> Secretaria; no se pueden saltar pasos)
        int id = SoportePazYSalvo.crearSolicitud(mockMvc, objectMapper, tokenEstudiante);
        org.junit.jupiter.api.Assertions.assertEquals(204,
                SoportePazYSalvo.cambiarEstado(mockMvc, tokenFuncionario, id, "APROBADA_FUNCIONARIO"));
        org.junit.jupiter.api.Assertions.assertEquals(204,
                SoportePazYSalvo.cambiarEstado(mockMvc, tokenCoordinador, id, "APROBADA_COORDINADOR"));
    }

    @Test
    @DisplayName("Funcional 2b: El coordinador no puede aprobar antes que el funcionario")
    void testCoordinadorNoPuedeSaltarseAlFuncionario() throws Exception {
        int id = SoportePazYSalvo.crearSolicitud(mockMvc, objectMapper, tokenEstudiante);
        mockMvc.perform(put("/api/solicitudes-pazysalvo/" + id + "/estado")
                        .header("Authorization", tokenCoordinador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nuevoEstado\":\"APROBADA_FUNCIONARIO\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ROL_NO_AUTORIZADO"))
                .andExpect(jsonPath("$.estadoActual").value("ENVIADA"))
                .andExpect(jsonPath("$.error").value("Este paso le corresponde al rol Funcionario"));

        mockMvc.perform(put("/api/solicitudes-pazysalvo/" + id + "/estado")
                        .header("Authorization", tokenFuncionario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nuevoEstado\":\"APROBADA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("TRANSICION_NO_PERMITIDA"))
                .andExpect(jsonPath("$.estadoActual").value("ENVIADA"))
                .andExpect(jsonPath("$.error").isNotEmpty());

        org.junit.jupiter.api.Assertions.assertEquals(204,
                SoportePazYSalvo.cambiarEstado(mockMvc, tokenFuncionario, id, "APROBADA_FUNCIONARIO"));
    }

    @Test
    @DisplayName("Funcional 2c: Un estudiante no puede tener dos solicitudes en trámite")
    void testNoPermiteDosSolicitudesEnTramite() throws Exception {
        SoportePazYSalvo.crearSolicitud(mockMvc, objectMapper, tokenEstudiante);
        mockMvc.perform(post("/api/solicitudes-pazysalvo")
                        .header("Authorization", tokenEstudiante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fecha_solicitud\": \"2025-01-15\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Funcional 3: Flujo de revisión - Funcionario revisa documentos")
    void testFlujoRevisionFuncionario() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Funcionario consulta solicitudes asignadas a su rol
         * 2. Funcionario verifica que todos los documentos estén completos
         * 3. Funcionario avanza la solicitud al siguiente estado
         * 
         * ACTORES: Funcionario administrativo
         * RESULTADO ESPERADO: Solicitud verificada y lista para siguiente paso
         */
        
        // Paso 1: Funcionario consulta sus solicitudes
        mockMvc.perform(get("/api/solicitudes-pazysalvo?rol=funcionario&bandeja=pendientes")
                        .header("Authorization", tokenFuncionario))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        // Paso 2: Funcionario verifica documentos (aquí podría haber más lógica)
        // En un sistema real, verificaría cada documento requerido
    }

    @Test
    @DisplayName("Funcional 4: Flujo de validación final - Secretaria emite documento")
    void testFlujoValidacionSecretaria() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Secretaria consulta solicitudes aprobadas
         * 2. Secretaria realiza validación final
         * 3. Secretaria genera el documento oficial de Paz y Salvo
         * 
         * ACTORES: Secretaria de facultad
         * RESULTADO ESPERADO: Documento PDF generado y solicitud finalizada
         */
        
        // Paso 1: Secretaria consulta solicitudes aprobadas
        mockMvc.perform(get("/api/solicitudes-pazysalvo?rol=secretaria&bandeja=pendientes")
                        .header("Authorization", tokenSecretaria))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        // Paso 2: Secretaria genera documento oficial (usando ID 37 que existe en BD). El documento por defecto
        // en PDF y DOCX (/generarDocumentoPazYSalvo) se retiró en EST-7: el frontend genera este.
        mockMvc.perform(post("/api/solicitudes-pazysalvo/37/documento-generado")
                        .param("numeroDocumento", "PYS-37")
                        .param("fechaDocumento", "2025-03-01")
                        .header("Authorization", tokenSecretaria))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_OCTET_STREAM));
    }

    // ==================== FLUJO 4: CONSULTA POR DIFERENTES ACTORES ====================

    @Test
    @DisplayName("Funcional 6: Flujo de consulta - Estudiante consulta sus solicitudes")
    void testFlujoConsultaEstudiante() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Estudiante inicia sesión en el sistema
         * 2. Estudiante consulta el estado de sus solicitudes de Paz y Salvo
         * 3. Estudiante ve únicamente sus propias solicitudes
         * 
         * ACTORES: Estudiante
         * RESULTADO ESPERADO: Lista filtrada de solicitudes del estudiante
         */
        
        // El estudiante autenticado consulta sus propias solicitudes
        int idEstudiante = SoportePazYSalvo.idUsuario(usuarioRepository, ESTUDIANTE_EMAIL);
        mockMvc.perform(get("/api/solicitudes-pazysalvo/usuarios/" + idEstudiante + "/solicitudes")
                        .header("Authorization", tokenEstudiante))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        // Y no puede consultar las de otro usuario
        int idCoordinador = SoportePazYSalvo.idUsuario(usuarioRepository, COORDINADOR_EMAIL);
        mockMvc.perform(get("/api/solicitudes-pazysalvo/usuarios/" + idCoordinador + "/solicitudes")
                        .header("Authorization", tokenEstudiante))
                .andExpect(status().isForbidden());

        // Ve el detalle de una solicitud suya; la de otro usuario, no (BUG-4)
        int idSolicitud = SoportePazYSalvo.crearSolicitud(mockMvc, objectMapper, tokenEstudiante);
        mockMvc.perform(get("/api/solicitudes-pazysalvo/" + idSolicitud)
                        .header("Authorization", tokenEstudiante))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_solicitud").value(idSolicitud));
        mockMvc.perform(get("/api/solicitudes-pazysalvo/" + Integer.MAX_VALUE)
                        .header("Authorization", tokenEstudiante))
                .andExpect(status().isForbidden());

        // La ruta que mezclaba solicitud, usuario y rol ya no existe (BUG-4)
        mockMvc.perform(get("/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo/" + idEstudiante)
                        .header("Authorization", tokenEstudiante))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Funcional 7: Flujo de consulta - Diferentes roles ven diferentes solicitudes")
    void testFlujoConsultaPorRoles() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Sistema filtra solicitudes según el rol del usuario
         * 2. Cada rol ve únicamente las solicitudes pertinentes a su función
         * 3. Se mantiene la seguridad y privacidad de la información
         * 
         * ACTORES: Coordinador, Funcionario, Secretaria
         * RESULTADO ESPERADO: Cada rol ve su propio conjunto de solicitudes
         */
        
        // Coordinador ve solicitudes de su programa
        mockMvc.perform(get("/api/solicitudes-pazysalvo?rol=coordinador&bandeja=pendientes")
                        .header("Authorization", tokenCoordinador))
                .andExpect(status().isOk());

        // Funcionario ve solicitudes en proceso administrativo
        mockMvc.perform(get("/api/solicitudes-pazysalvo?rol=funcionario&bandeja=pendientes")
                        .header("Authorization", tokenFuncionario))
                .andExpect(status().isOk());

        // Secretaria ve solicitudes para emisión final
        mockMvc.perform(get("/api/solicitudes-pazysalvo?rol=secretaria&bandeja=pendientes")
                        .header("Authorization", tokenSecretaria))
                .andExpect(status().isOk());
    }

    // ==================== FLUJO 5: MANEJO DE CASOS EXCEPCIONALES ====================

    @Test
    @DisplayName("Funcional 8: Flujo de manejo de errores - Validación de datos obligatorios")
    void testFlujoValidacionDatosObligatorios() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Usuario intenta crear solicitud sin datos obligatorios
         * 2. Sistema valida y rechaza la solicitud
         * 3. Sistema retorna mensaje de error claro al usuario
         * 
         * ACTORES: Estudiante
         * RESULTADO ESPERADO: Rechazo con mensaje de error descriptivo
         */
        
        // El usuario sale del token JWT, así que el dato inválido es el período académico
        String jsonInvalido = """
            {
                "idTipoProceso": 1,
                "fecha_solicitud": "2025-01-15",
                "periodo_academico": "2025-9"
            }
            """;

        mockMvc.perform(post("/api/solicitudes-pazysalvo")
                        .header("Authorization", tokenEstudiante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Funcional 9: Flujo de manejo de errores - Solicitud inexistente")
    void testFlujoManejoSolicitudInexistente() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Usuario intenta cambiar estado de solicitud que no existe
         * 2. Sistema valida que la solicitud existe antes de procesar
         * 3. Sistema retorna error 404 con mensaje apropiado
         * 
         * ACTORES: Coordinador, Funcionario
         * RESULTADO ESPERADO: Error 404 Not Found
         */
        
        String jsonCambioEstado = """
            {
                "nuevoEstado": "APROBADO"
            }
            """;

        mockMvc.perform(put("/api/solicitudes-pazysalvo/999/estado")
                        .header("Authorization", tokenCoordinador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCambioEstado))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Funcional 10: Flujo completo exitoso - De solicitud a documento final")
    void testFlujoCompletoExitoso() throws Exception {
        /*
         * FLUJO DE NEGOCIO COMPLETO:
         * 1. Estudiante crea solicitud
         * 2. Coordinador la aprueba
         * 3. Funcionario verifica documentos
         * 4. Secretaria genera documento final
         * 5. Estudiante descarga su Paz y Salvo
         * 
         * ACTORES: Estudiante, Coordinador, Funcionario, Secretaria
         * RESULTADO ESPERADO: Documento generado y proceso finalizado
         */
        
        // Paso 1: Crear solicitud
        int id = SoportePazYSalvo.crearSolicitud(mockMvc, objectMapper, tokenEstudiante);

        // Paso 2: Consultar la bandeja del funcionario, donde está la solicitud nueva
        mockMvc.perform(get("/api/solicitudes-pazysalvo?rol=funcionario&bandeja=pendientes")
                        .header("Authorization", tokenCoordinador))
                .andExpect(status().isOk());

        // Paso 3: Aprobación completa: Funcionario -> Coordinador -> Secretaria
        org.junit.jupiter.api.Assertions.assertEquals(204,
                SoportePazYSalvo.cambiarEstado(mockMvc, tokenFuncionario, id, "APROBADA_FUNCIONARIO"));
        org.junit.jupiter.api.Assertions.assertEquals(204,
                SoportePazYSalvo.cambiarEstado(mockMvc, tokenCoordinador, id, "APROBADA_COORDINADOR"));
        org.junit.jupiter.api.Assertions.assertEquals(204,
                SoportePazYSalvo.cambiarEstado(mockMvc, tokenSecretaria, id, "APROBADA"));

        // Paso 4: Generar documento final de esa solicitud
        mockMvc.perform(post("/api/solicitudes-pazysalvo/" + id + "/documento-generado")
                        .param("numeroDocumento", "PYS-" + id)
                        .param("fechaDocumento", "2025-03-01")
                        .header("Authorization", tokenSecretaria))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_OCTET_STREAM));
    }
}

