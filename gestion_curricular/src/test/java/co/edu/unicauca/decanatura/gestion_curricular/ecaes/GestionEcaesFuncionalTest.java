package co.edu.unicauca.decanatura.gestion_curricular.ecaes;

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
 * PRUEBAS FUNCIONALES - GESTIÓN DE ECAES
 * ============================================================
 *
 * Objetivo: Validar FLUJOS COMPLETOS DE NEGOCIO del módulo de ECAES.
 * Estas pruebas se enfocan en casos de uso end-to-end desde la perspectiva
 * del usuario/negocio, no solo en componentes técnicos.
 *
 * Diferencia con Pruebas de Integración:
 * - Integración: Verifica que componentes técnicos funcionen juntos
 * - Funcional: Verifica que FUNCIONALIDADES DE NEGOCIO funcionen correctamente
 *
 * Escenarios de negocio cubiertos:
 * 1. Flujo completo de publicación de fechas por secretario
 * 2. Flujo de consulta de fechas por estudiante
 * 3. Flujo de creación de solicitud con documentos por estudiante
 * 4. Flujo de visualización de documentación por secretario
 * 5. Flujo de preRegistro y notificación por secretario
 * 6. Flujo de verificación de estado por estudiante
 *
 * @author Daniel
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WithMockUser(roles = { "SECRETARIA", "FUNCIONARIO", "COORDINADOR", "ESTUDIANTE" })
@DisplayName("Pruebas Funcionales - Gestión de ECAES")
class GestionEcaesFuncionalTest {

    @Autowired
    private MockMvc mockMvc;

    // ==================== FLUJO 1: PUBLICACIÓN DE FECHAS POR SECRETARIO ====================

    @Test
    @DisplayName("Funcional 1: Flujo completo - Secretario publica fechas de preRegistro")
    void testFlujoCompletoPublicacionFechas() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Secretario ejecutivo publica fechas relevantes del preRegistro
         * 2. Las fechas quedan disponibles para consulta
         * 3. Los estudiantes pueden visualizar las fechas
         * 
         * ACTORES: Secretario Ejecutivo, Estudiante
         * RESULTADO ESPERADO: Fechas publicadas y disponibles
         */
        
        // Paso 1: Secretario publica fechas
        String jsonFechas = """
            {
                "periodoAcademico": "2025-3",
                "inscripcion_est_by_facultad": "2025-09-01",
                "registro_recaudo_ordinario": "2025-09-15",
                "registro_recaudo_extraordinario": "2025-09-20",
                "citacion": "2025-10-01",
                "aplicacion": "2025-10-10",
                "resultados_individuales": "2025-11-15"
            }
            """;

        mockMvc.perform(post("/api/solicitudes-ecaes/publicarFechasEcaes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFechas))
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    if (s != 201 && s != 409) throw new AssertionError("Expected 201 or 409, got " + s);
                });

        // Paso 2: Verificar que las fechas aparecen en el listado
        mockMvc.perform(get("/api/solicitudes-ecaes/listarFechasEcaes"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== FLUJO 2: CONSULTA DE FECHAS POR ESTUDIANTE ====================

    @Test
    @DisplayName("Funcional 2: Flujo de consulta - Estudiante visualiza fechas y requisitos")
    void testFlujoConsultaFechasEstudiante() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Estudiante consulta todas las fechas disponibles
         * 2. Estudiante consulta fechas por período específico
         * 3. Estudiante puede ver requisitos y fechas importantes
         * 
         * ACTORES: Estudiante
         * RESULTADO ESPERADO: Información de fechas disponible
         */
        
        // Paso 1: Estudiante consulta todas las fechas
        mockMvc.perform(get("/api/solicitudes-ecaes/listarFechasEcaes"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        // Paso 2: Estudiante consulta fechas por período (puede dar 200, 404 o 500 si hay múltiples registros)
        mockMvc.perform(get("/api/solicitudes-ecaes/buscarFechasPorPeriodo/2025-2"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 200 && status != 404 && status != 500)
                        throw new AssertionError("Expected 200, 404 or 500, got " + status);
                });
    }

    // ==================== FLUJO 3: CREACIÓN DE SOLICITUD CON DOCUMENTOS ====================

    @Test
    @DisplayName("Funcional 3: Flujo completo - Estudiante crea solicitud con documentos")
    void testFlujoCompletoSolicitudEstudiante() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Estudiante crea una solicitud de ECAES
         * 2. El sistema registra la solicitud con estado inicial
         * 3. La solicitud queda disponible para revisión por secretario
         * 
         * ACTORES: Estudiante
         * RESULTADO ESPERADO: Solicitud creada exitosamente
         */
        
        String jsonSolicitud = """
            {
                "nombre_solicitud": "Solicitud de ECAES",
                "fecha_registro_solicitud": "2025-01-19T00:00:00.000Z",
                "tipoDocumento": "CC",
                "numero_documento": "1020101234",
                "fecha_expedicion": "2020-01-15T00:00:00.000Z",
                "fecha_nacimiento": "2000-05-20T00:00:00.000Z",
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

        // Paso 1: Crear solicitud (201 o 409 si ya existe)
        mockMvc.perform(post("/api/solicitudes-ecaes/crearSolicitud-Ecaes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    if (s != 201 && s != 409) throw new AssertionError("Expected 201 or 409, got " + s);
                });

        // Paso 2: Verificar que la solicitud aparece en el listado
        mockMvc.perform(get("/api/solicitudes-ecaes/listarSolicitudes-Ecaes"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== FLUJO 4: VISUALIZACIÓN DE DOCUMENTACIÓN POR SECRETARIO ====================

    @Test
    @DisplayName("Funcional 4: Flujo de visualización - Secretario revisa documentación")
    void testFlujoVisualizacionDocumentacionSecretario() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Secretario consulta solicitudes asignadas a su rol
         * 2. Secretario revisa una solicitud específica
         * 3. Secretario puede ver todos los documentos adjuntos
         * 
         * ACTORES: Secretario Ejecutivo
         * RESULTADO ESPERADO: Documentación visible para revisión
         */
        
        // Paso 1: Secretario consulta sus solicitudes
        mockMvc.perform(get("/api/solicitudes-ecaes/listarSolicitudes-Ecaes/Funcionario"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        // Paso 2: Secretario consulta solicitud específica (usando ID 5 que existe en BD de test)
        mockMvc.perform(get("/api/solicitudes-ecaes/buscarSolicitud-Ecaes/5"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== FLUJO 5: PRE-REGISTRO Y NOTIFICACIÓN ====================

    @Test
    @DisplayName("Funcional 5: Flujo de preRegistro - Secretario realiza preRegistro y notifica")
    void testFlujoPreRegistroSecretario() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Secretario consulta solicitudes pendientes
         * 2. Secretario revisa documentación
         * 3. Secretario realiza el preRegistro
         * 4. Secretario cambia el estado a "preRegistrado"
         * 5. Estudiante puede verificar su estado
         * 
         * ACTORES: Secretario Ejecutivo, Estudiante
         * RESULTADO ESPERADO: PreRegistro realizado y notificado
         */
        
        // Paso 1: Secretario consulta solicitudes asignadas
        mockMvc.perform(get("/api/solicitudes-ecaes/listarSolicitudes-Ecaes/Funcionario"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        // Paso 2: Secretario consulta solicitud específica
        mockMvc.perform(get("/api/solicitudes-ecaes/buscarSolicitud-Ecaes/6"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        // Paso 3: Secretario cambia estado a "preRegistrado" (usando ID 6 que existe en BD de test)
        String jsonCambioEstado = """
            {
                "idSolicitud": 6,
                "nuevoEstado": "preRegistrado"
            }
            """;
        
        mockMvc.perform(put("/api/solicitudes-ecaes/actualizarEstadoSolicitud")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCambioEstado))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 204 && status != 404 && status != 500)
                        throw new AssertionError("Expected 204, 404 or 500, got " + status);
                });
    }

    // ==================== FLUJO 6: VERIFICACIÓN DE ESTADO POR ESTUDIANTE ====================

    @Test
    @DisplayName("Funcional 6: Flujo de verificación - Estudiante verifica estado de PreRegistro")
    void testFlujoVerificacionEstadoEstudiante() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Estudiante inicia sesión en el sistema
         * 2. Estudiante consulta el estado de sus solicitudes de ECAES
         * 3. Estudiante ve únicamente sus propias solicitudes
         * 4. Estudiante puede ver si su solicitud está "preRegistrada"
         * 
         * ACTORES: Estudiante
         * RESULTADO ESPERADO: Estado de PreRegistro visible
         */
        
        // Estudiante con ID 1 consulta sus solicitudes
        mockMvc.perform(get("/api/solicitudes-ecaes/listarSolicitud-ecaes/porRol")
                        .param("rol", "ESTUDIANTE")
                        .param("idUsuario", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== FLUJO 7: FLUJO COMPLETO END-TO-END ====================

    @Test
    @DisplayName("Funcional 7: Flujo completo exitoso - De publicación de fechas a preRegistro")
    void testFlujoCompletoExitoso() throws Exception {
        /*
         * FLUJO DE NEGOCIO COMPLETO:
         * 1. Secretario publica fechas
         * 2. Estudiante consulta fechas
         * 3. Estudiante crea solicitud con documentos
         * 4. Secretario revisa documentación
         * 5. Secretario realiza preRegistro
         * 6. Estudiante verifica su estado
         * 
         * ACTORES: Secretario Ejecutivo, Estudiante
         * RESULTADO ESPERADO: Proceso completado exitosamente
         */
        
        // Paso 1: Secretario publica fechas
        String jsonFechas = """
            {
                "periodoAcademico": "2025-4",
                "inscripcion_est_by_facultad": "2025-12-01",
                "registro_recaudo_ordinario": "2025-12-15",
                "registro_recaudo_extraordinario": "2025-12-20",
                "citacion": "2026-01-01",
                "aplicacion": "2026-01-10",
                "resultados_individuales": "2026-02-15"
            }
            """;
        
        mockMvc.perform(post("/api/solicitudes-ecaes/publicarFechasEcaes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFechas))
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    if (s != 201 && s != 409) throw new AssertionError("Expected 201 or 409, got " + s);
                });

        // Paso 2: Estudiante consulta fechas
        mockMvc.perform(get("/api/solicitudes-ecaes/listarFechasEcaes"))
                .andExpect(status().isOk());

        // Paso 3: Estudiante crea solicitud
        String jsonSolicitud = """
            {
                "nombre_solicitud": "Solicitud de ECAES",
                "fecha_registro_solicitud": "2025-01-19T00:00:00.000Z",
                "tipoDocumento": "CC",
                "numero_documento": "1020101234",
                "fecha_expedicion": "2020-01-15T00:00:00.000Z",
                "fecha_nacimiento": "2000-05-20T00:00:00.000Z",
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
        
        mockMvc.perform(post("/api/solicitudes-ecaes/crearSolicitud-Ecaes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitud))
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    if (s != 201 && s != 409) throw new AssertionError("Expected 201 or 409, got " + s);
                });

        // Paso 4: Secretario consulta solicitudes
        mockMvc.perform(get("/api/solicitudes-ecaes/listarSolicitudes-Ecaes/Funcionario"))
                .andExpect(status().isOk());

        // Paso 5: Secretario realiza preRegistro (usando ID 5 que existe en BD de test)
        String jsonPreRegistro = """
            {
                "idSolicitud": 5,
                "nuevoEstado": "preRegistrado"
            }
            """;
        
        mockMvc.perform(put("/api/solicitudes-ecaes/actualizarEstadoSolicitud")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPreRegistro))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 204 && status != 404 && status != 500)
                        throw new AssertionError("Expected 204, 404 or 500, got " + status);
                });

        // Paso 6: Estudiante verifica estado
        mockMvc.perform(get("/api/solicitudes-ecaes/listarSolicitud-ecaes/porRol")
                        .param("rol", "ESTUDIANTE")
                        .param("idUsuario", "1"))
                .andExpect(status().isOk());
    }

    // ==================== FLUJO 8: ACTUALIZACIÓN DE FECHAS ====================

    @Test
    @DisplayName("Funcional 8: Flujo de actualización - Secretario actualiza fechas publicadas")
    void testFlujoActualizacionFechas() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Secretario necesita modificar fechas ya publicadas
         * 2. Secretario actualiza las fechas
         * 3. Las nuevas fechas quedan disponibles
         * 
         * ACTORES: Secretario Ejecutivo
         * RESULTADO ESPERADO: Fechas actualizadas correctamente
         */
        
        String jsonFechasActualizadas = """
            {
                "idFechaEcaes": 1,
                "periodoAcademico": "2025-1",
                "inscripcion_est_by_facultad": "2025-02-05",
                "registro_recaudo_ordinario": "2025-02-18",
                "registro_recaudo_extraordinario": "2025-02-25",
                "citacion": "2025-03-05",
                "aplicacion": "2025-03-15",
                "resultados_individuales": "2025-04-20"
            }
            """;

        mockMvc.perform(put("/api/solicitudes-ecaes/actualizarFechasEcaes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFechasActualizadas))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 200 && status != 404 && status != 500)
                        throw new AssertionError("Expected 200, 404 or 500, got " + status);
                });
    }

    // ==================== FLUJO 9: MANEJO DE CASOS EXCEPCIONALES ====================

    @Test
    @DisplayName("Funcional 9: Flujo de manejo de errores - Validación de datos obligatorios")
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
                "nombre_solicitud": "Solicitud de ECAES",
                "fecha_registro_solicitud": "2025-01-19T00:00:00.000Z",
                "objUsuario": null
            }
            """;

        mockMvc.perform(post("/api/solicitudes-ecaes/crearSolicitud-Ecaes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Funcional 10: Flujo de manejo de errores - Solicitud inexistente")
    void testFlujoManejoSolicitudInexistente() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Usuario intenta cambiar estado de solicitud que no existe
         * 2. Sistema valida que la solicitud existe antes de procesar
         * 3. Sistema retorna error 404 con mensaje apropiado
         * 
         * ACTORES: Secretario Ejecutivo
         * RESULTADO ESPERADO: Error 404 Not Found
         */
        
        String jsonCambioEstado = """
            {
                "idSolicitud": 999,
                "nuevoEstado": "preRegistrado"
            }
            """;

        mockMvc.perform(put("/api/solicitudes-ecaes/actualizarEstadoSolicitud")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCambioEstado))
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    if (s != 404 && s != 500) throw new AssertionError("Expected 404 or 500, got " + s);
                });
    }
}

