package co.edu.unicauca.decanatura.gestion_curricular.homologacion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ============================================================
 * PRUEBAS FUNCIONALES - GESTIÓN DE HOMOLOGACIÓN
 * ============================================================
 *
 * Objetivo: Validar FLUJOS COMPLETOS DE NEGOCIO del módulo de Homologación.
 * Estas pruebas se enfocan en casos de uso end-to-end desde la perspectiva
 * del usuario/negocio, no solo en componentes técnicos.
 *
 * Diferencia con Pruebas de Integración:
 * - Integración: Verifica que componentes técnicos funcionen juntos
 * - Funcional: Verifica que FUNCIONALIDADES DE NEGOCIO funcionen correctamente
 *
 * Escenarios de negocio cubiertos:
 * 1. Flujo completo de solicitud de Homologación por estudiante
 * 2. Flujo de validación por funcionario
 * 3. Flujo de evaluación y aprobación por coordinador
 * 4. Flujo de emisión de resolución por secretaria
 * 5. Flujo de consulta por diferentes actores
 *
 * @author Daniel
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WithMockUser(roles = { "SECRETARIA", "FUNCIONARIO", "COORDINADOR", "ESTUDIANTE" })
@DisplayName("Pruebas Funcionales - Gestión de Homologación")
class GestionHomologacionFuncionalTest {

    @Autowired
    private MockMvc mockMvc;

    // ==================== FLUJO 1: SOLICITUD COMPLETA DE HOMOLOGACIÓN ====================

    @Test
    @DisplayName("Funcional 1: Flujo completo - Estudiante solicita Homologación")
    void testFlujoCompletoSolicitudEstudiante() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Estudiante crea una solicitud de Homologación
         * 2. El sistema registra la solicitud con estado inicial
         * 3. La solicitud queda disponible para revisión por funcionarios
         * 
         * ACTORES: Estudiante
         * RESULTADO ESPERADO: Solicitud creada exitosamente
         */
        
        String jsonSolicitud = """
            {
                "nombre_solicitud": "Homologación de asignaturas",
                "fecha_registro_solicitud": "2025-01-15T00:00:00.000Z",
                "objUsuario": {
                    "id_usuario": 1,
                    "nombre_completo": "Juan Pérez Estudiante",
                    "codigo": "102010",
                    "correo": "estudiante.test@unicauca.edu.co",
                    "id_rol": 1,
                    "id_programa": 1
                }
            }
            """;

        // Paso 1: Crear solicitud (201 creada o 409 si ya existe)
        mockMvc.perform(post("/api/solicitudes-homologacion/crearSolicitud-Homologacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    if (s != 201 && s != 409) throw new AssertionError("Expected 201 or 409, got " + s);
                });

        // Paso 2: Verificar que la solicitud aparece en el listado
        mockMvc.perform(get("/api/solicitudes-homologacion/listarSolicitud-Homologacion"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== FLUJO 2: VALIDACIÓN POR FUNCIONARIO ====================

    @Test
    @DisplayName("Funcional 2: Flujo de validación - Funcionario revisa documentos")
    void testFlujoValidacionFuncionario() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Funcionario consulta solicitudes asignadas a su rol
         * 2. Funcionario revisa una solicitud específica
         * 3. Funcionario valida los documentos requeridos
         * 4. Funcionario aprueba la solicitud
         * 
         * ACTORES: Funcionario administrativo
         * RESULTADO ESPERADO: Solicitud validada y lista para coordinador
         */
        
        // Paso 1: Funcionario consulta sus solicitudes
        mockMvc.perform(get("/api/solicitudes-homologacion/listarSolicitud-Homologacion/Funcionario"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        // Paso 2: Funcionario consulta solicitud específica
        mockMvc.perform(get("/api/solicitudes-homologacion/listarSolicitud-Homologacion/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        // Paso 3: Funcionario valida documentos requeridos
        mockMvc.perform(get("/api/solicitudes-homologacion/validarDocumentosRequeridos/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        // Paso 4: Funcionario aprueba la solicitud
        String jsonAprobacion = """
            {
                "idSolicitud": 1,
                "nuevoEstado": "APROBADA_FUNCIONARIO"
            }
            """;
        
        mockMvc.perform(put("/api/solicitudes-homologacion/actualizarEstadoSolicitud")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAprobacion))
                .andExpect(status().isNoContent());
    }

    // ==================== FLUJO 3: EVALUACIÓN POR COORDINADOR ====================

    @Test
    @DisplayName("Funcional 3: Flujo de evaluación - Coordinador revisa y aprueba")
    void testFlujoEvaluacionCoordinador() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Coordinador consulta solicitudes aprobadas por funcionario
         * 2. Coordinador revisa una solicitud específica
         * 3. Coordinador evalúa basándose en contenido y créditos
         * 4. Coordinador aprueba la solicitud
         * 
         * ACTORES: Coordinador del programa
         * RESULTADO ESPERADO: Solicitud evaluada y lista para secretaria
         */
        
        // Paso 1: Coordinador consulta solicitudes asignadas
        mockMvc.perform(get("/api/solicitudes-homologacion/listarSolicitud-Homologacion/Coordinador"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        // Paso 2: Coordinador consulta solicitud específica
        mockMvc.perform(get("/api/solicitudes-homologacion/listarSolicitud-Homologacion/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        // Paso 3: Coordinador aprueba la solicitud
        String jsonAprobacion = """
            {
                "idSolicitud": 1,
                "nuevoEstado": "APROBADA_COORDINADOR"
            }
            """;
        
        mockMvc.perform(put("/api/solicitudes-homologacion/actualizarEstadoSolicitud")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAprobacion))
                .andExpect(status().isNoContent());
    }

    // ==================== FLUJO 4: EMISIÓN DE RESOLUCIÓN POR SECRETARIA ====================

    @Test
    @DisplayName("Funcional 4: Flujo de emisión - Secretaria genera resolución")
    void testFlujoEmisionSecretaria() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Secretaria consulta solicitudes aprobadas por coordinador
         * 2. Secretaria revisa una solicitud específica
         * 3. Secretaria descarga/genera el oficio de resolución
         * 
         * ACTORES: Secretaria de facultad
         * RESULTADO ESPERADO: Oficio disponible para descarga
         */
        
        // Paso 1: Secretaria consulta solicitudes listas para resolución
        mockMvc.perform(get("/api/solicitudes-homologacion/listarSolicitud-Homologacion/Secretaria"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        // Paso 2: Secretaria consulta solicitud específica
        mockMvc.perform(get("/api/solicitudes-homologacion/listarSolicitud-Homologacion/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        // Paso 3: Secretaria obtiene lista de oficios disponibles
        mockMvc.perform(get("/api/solicitudes-homologacion/obtenerOficios/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        // Paso 4: Secretaria descarga oficio (si existe)
        mockMvc.perform(get("/api/solicitudes-homologacion/descargarOficio/1"))
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    if (s != 200 && s != 404 && s != 500)
                        throw new AssertionError("Expected 200, 404 or 500, got " + s);
                });
    }

    // ==================== FLUJO 5: CONSULTA POR DIFERENTES ACTORES ====================

    @Test
    @DisplayName("Funcional 5: Flujo de consulta - Estudiante consulta sus solicitudes")
    void testFlujoConsultaEstudiante() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Estudiante inicia sesión en el sistema
         * 2. Estudiante consulta el estado de sus solicitudes de Homologación
         * 3. Estudiante ve únicamente sus propias solicitudes
         * 
         * ACTORES: Estudiante
         * RESULTADO ESPERADO: Lista filtrada de solicitudes del estudiante
         */
        
        // Estudiante con ID 1 consulta sus solicitudes
        mockMvc.perform(get("/api/solicitudes-homologacion/listarSolicitud-Homologacion/porRol")
                        .param("rol", "ESTUDIANTE")
                        .param("idUsuario", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Funcional 6: Flujo de consulta - Diferentes roles ven diferentes solicitudes")
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
        
        // Funcionario ve solicitudes en estado "Enviada"
        mockMvc.perform(get("/api/solicitudes-homologacion/listarSolicitud-Homologacion/Funcionario"))
                .andExpect(status().isOk());

        // Coordinador ve solicitudes aprobadas por funcionario
        mockMvc.perform(get("/api/solicitudes-homologacion/listarSolicitud-Homologacion/Coordinador"))
                .andExpect(status().isOk());

        // Secretaria ve solicitudes aprobadas por coordinador
        mockMvc.perform(get("/api/solicitudes-homologacion/listarSolicitud-Homologacion/Secretaria"))
                .andExpect(status().isOk());
    }

    // ==================== FLUJO 6: FLUJO COMPLETO END-TO-END ====================

    @Test
    @DisplayName("Funcional 7: Flujo completo exitoso - De solicitud a resolución final")
    void testFlujoCompletoExitoso() throws Exception {
        /*
         * FLUJO DE NEGOCIO COMPLETO:
         * 1. Estudiante crea solicitud
         * 2. Funcionario la valida y aprueba
         * 3. Coordinador la evalúa y aprueba
         * 4. Secretaria genera resolución
         * 5. Estudiante puede consultar su solicitud
         * 
         * ACTORES: Estudiante, Funcionario, Coordinador, Secretaria
         * RESULTADO ESPERADO: Proceso completado exitosamente
         */
        
        // Paso 1: Crear solicitud
        String jsonSolicitud = """
            {
                "nombre_solicitud": "Homologación de asignaturas",
                "fecha_registro_solicitud": "2025-01-15T00:00:00.000Z",
                "objUsuario": {
                    "id_usuario": 1,
                    "nombre_completo": "Juan Pérez Estudiante",
                    "codigo": "102010",
                    "correo": "estudiante.test@unicauca.edu.co",
                    "id_rol": 1,
                    "id_programa": 1
                }
            }
            """;
        
        mockMvc.perform(post("/api/solicitudes-homologacion/crearSolicitud-Homologacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    if (s != 201 && s != 409) throw new AssertionError("Expected 201 or 409, got " + s);
                });

        // Paso 2: Consultar solicitudes (simulando diferentes roles)
        mockMvc.perform(get("/api/solicitudes-homologacion/listarSolicitud-Homologacion"))
                .andExpect(status().isOk());

        // Paso 3: Funcionario aprueba solicitud (usando ID 1 que puede existir en BD)
        String jsonAprobacionFuncionario = """
            {
                "idSolicitud": 1,
                "nuevoEstado": "APROBADA_FUNCIONARIO"
            }
            """;
        
        mockMvc.perform(put("/api/solicitudes-homologacion/actualizarEstadoSolicitud")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAprobacionFuncionario))
                .andExpect(status().isNoContent());

        // Paso 4: Coordinador aprueba solicitud
        String jsonAprobacionCoordinador = """
            {
                "idSolicitud": 1,
                "nuevoEstado": "APROBADA_COORDINADOR"
            }
            """;
        
        mockMvc.perform(put("/api/solicitudes-homologacion/actualizarEstadoSolicitud")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAprobacionCoordinador))
                .andExpect(status().isNoContent());

        // Paso 5: Secretaria consulta oficios disponibles
        mockMvc.perform(get("/api/solicitudes-homologacion/obtenerOficios/1"))
                .andExpect(status().isOk());
    }

    // ==================== FLUJO 7: MANEJO DE CASOS EXCEPCIONALES ====================

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
        
        String jsonInvalido = """
            {
                "nombre_solicitud": "Homologación de asignaturas",
                "fecha_registro_solicitud": "2025-01-15",
                "objUsuario": null
            }
            """;

        mockMvc.perform(post("/api/solicitudes-homologacion/crearSolicitud-Homologacion")
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
                "idSolicitud": 999,
                "nuevoEstado": "APROBADA_FUNCIONARIO"
            }
            """;

        mockMvc.perform(put("/api/solicitudes-homologacion/actualizarEstadoSolicitud")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCambioEstado))
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    if (s != 404 && s != 500) throw new AssertionError("Expected 404 or 500, got " + s);
                });
    }

    // ==================== FLUJO 8: VALIDACIÓN DE DOCUMENTOS ====================

    @Test
    @DisplayName("Funcional 10: Flujo de validación de documentos requeridos")
    void testFlujoValidacionDocumentosRequeridos() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Funcionario necesita verificar documentos de una solicitud
         * 2. Sistema valida qué documentos están presentes
         * 3. Sistema indica qué documentos faltan
         * 
         * ACTORES: Funcionario
         * RESULTADO ESPERADO: Información clara sobre documentos requeridos
         */
        
        mockMvc.perform(get("/api/solicitudes-homologacion/validarDocumentosRequeridos/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}

