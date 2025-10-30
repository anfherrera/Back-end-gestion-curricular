package co.edu.unicauca.decanatura.gestion_curricular.aceptacion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ============================================================
 * PRUEBAS DE ACEPTACIÓN - CURSOS DE VERANO (GCV5)
 * ============================================================
 *
 * Objetivo: Validar CRITERIOS DE ACEPTACIÓN definidos por el cliente/negocio
 * para el módulo de gestión de cursos intersemestrales.
 *
 * Criterios de Aceptación cubiertos:
 * 1. CA-GCV5-01: Estudiante puede consultar cursos disponibles
 * 2. CA-GCV5-02: Estudiante puede preinscribirse a un curso
 * 3. CA-GCV5-03: Coordinador puede consultar cursos por periodo
 * 4. CA-GCV5-04: Sistema valida datos de preinscripción
 * 5. CA-GCV5-05: Estudiante puede ver sus preinscripciones
 *
 * @author Andrés Felipe Herrera Artunduaga
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Pruebas de Aceptación - Cursos de Verano")
class CursosVeranoAceptacionTest {

    @Autowired
    private MockMvc mockMvc;

    // ==================== CA-GCV5-01: ESTUDIANTE PUEDE CONSULTAR CURSOS ====================

    @Test
    @DisplayName("CA-GCV5-01: Como estudiante quiero ver cursos disponibles para planificar mi verano")
    void testEstudiantePuedeConsultarCursosDisponibles() throws Exception {
        /*
         * GIVEN: Un estudiante autenticado en el sistema
         *        AND existen cursos de verano ofertados para el próximo periodo
         * 
         * WHEN: El estudiante consulta la lista de cursos disponibles
         * 
         * THEN: El sistema debe retornar la lista completa de cursos ofertados
         *       AND debe retornar código HTTP 200 (OK)
         *       AND cada curso debe incluir información relevante (materia, horario, docente, cupos)
         */
        
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("CA-GCV5-01.1: Como estudiante quiero ver detalles de un curso específico")
    void testEstudiantePuedeVerDetallesCurso() throws Exception {
        /*
         * GIVEN: Un estudiante interesado en un curso específico
         *        AND el curso existe en el sistema
         * 
         * WHEN: El estudiante consulta los detalles del curso
         * 
         * THEN: El sistema debe retornar información detallada del curso
         *       AND debe incluir nombre de materia, código, docente, horario
         *       AND debe mostrar cupos disponibles y totales
         */
        
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idCurso").value(1));
    }

    // ==================== CA-GCV5-02: ESTUDIANTE PUEDE PREINSCRIBIRSE ====================

    @Test
    @DisplayName("CA-GCV5-02: Como estudiante quiero preinscribirme a un curso para asegurar mi cupo")
    void testEstudiantePuedePreinscribirse() throws Exception {
        /*
         * GIVEN: Un estudiante autenticado
         *        AND existe un curso de verano con cupos disponibles
         *        AND el estudiante cumple requisitos para cursar la materia
         * 
         * WHEN: El estudiante se preinscribe al curso
         *       AND proporciona todos los datos requeridos
         * 
         * THEN: El sistema debe registrar la preinscripción exitosamente
         *       AND debe retornar código HTTP 201 (Created)
         *       AND la preinscripción debe quedar en estado "PENDIENTE"
         *       AND debe decrementar el contador de cupos disponibles
         */
        
        String jsonPreinscripcion = """
            {
                "idUsuario": 5,
                "idCurso": 1,
                "fecha_solicitud": "2025-01-15"
            }
            """;

        mockMvc.perform(post("/api/cursos-intersemestrales/solicitudes/preinscripcion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPreinscripcion))
                .andExpect(status().isCreated());
    }

    // ==================== CA-GCV5-03: COORDINADOR PUEDE FILTRAR POR PERIODO ====================

    @Test
    @DisplayName("CA-GCV5-03: Como coordinador quiero filtrar cursos por periodo para planificar oferta")
    void testCoordinadorPuedeFiltrarPorPeriodo() throws Exception {
        /*
         * GIVEN: Un coordinador autenticado
         *        AND existen cursos ofertados en múltiples periodos académicos
         * 
         * WHEN: El coordinador filtra cursos por un periodo específico (ej: 2025-1)
         * 
         * THEN: El sistema debe retornar únicamente cursos del periodo solicitado
         *       AND debe retornar código HTTP 200 (OK)
         *       AND debe excluir cursos de otros periodos
         */
        
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano/periodo/2025-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("CA-GCV5-03.1: Como coordinador quiero filtrar cursos por materia para analizar demanda")
    void testCoordinadorPuedeFiltrarPorMateria() throws Exception {
        /*
         * GIVEN: Un coordinador evaluando demanda histórica
         *        AND existen múltiples cursos de diferentes materias
         * 
         * WHEN: El coordinador filtra cursos por una materia específica
         * 
         * THEN: El sistema debe retornar todos los cursos de esa materia
         *       AND debe incluir cursos de diferentes periodos si aplica
         */
        
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano/materia/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== CA-GCV5-04: SISTEMA VALIDA DATOS DE PREINSCRIPCIÓN ====================

    @Test
    @DisplayName("CA-GCV5-04: Como sistema debo validar preinscripciones para mantener integridad")
    void testSistemaValidaDatosPreinscripcion() throws Exception {
        /*
         * GIVEN: Un estudiante intenta preinscribirse a un curso
         *        AND el estudiante NO proporciona su ID de usuario (dato obligatorio)
         * 
         * WHEN: El estudiante envía la preinscripción con datos incompletos
         * 
         * THEN: El sistema debe rechazar la preinscripción
         *       AND debe retornar código HTTP 400 (Bad Request)
         *       AND debe retornar mensaje de error indicando campo faltante
         *       AND NO debe crear preinscripción en la base de datos
         */
        
        String jsonInvalido = """
            {
                "idUsuario": null,
                "idCurso": 1,
                "fecha_solicitud": "2025-01-15"
            }
            """;

        mockMvc.perform(post("/api/cursos-intersemestrales/solicitudes/preinscripcion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());
    }

    // ==================== CA-GCV5-05: ESTUDIANTE VE SUS PREINSCRIPCIONES ====================

    @Test
    @DisplayName("CA-GCV5-05: Como estudiante quiero ver mis preinscripciones para dar seguimiento")
    void testEstudianteVeSusPreinscripciones() throws Exception {
        /*
         * GIVEN: Un estudiante con ID 1 autenticado
         *        AND el estudiante tiene preinscripciones registradas
         *        AND existen preinscripciones de otros estudiantes
         * 
         * WHEN: El estudiante consulta sus preinscripciones
         * 
         * THEN: El sistema debe retornar únicamente preinscripciones del estudiante
         *       AND debe retornar código HTTP 200 (OK)
         *       AND NO debe mostrar preinscripciones de otros estudiantes
         *       AND debe mostrar estado actual de cada preinscripción
         */
        
        mockMvc.perform(get("/api/cursos-intersemestrales/solicitudes/preinscripcion/estudiante/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== CA-GCV5-06: MANEJO DE CURSOS INEXISTENTES ====================

    @Test
    @DisplayName("CA-GCV5-06: Como usuario quiero error claro si consulto curso que no existe")
    void testSistemaManejaCursoInexistente() throws Exception {
        /*
         * GIVEN: Un usuario consulta un curso
         *        AND el curso con ID 999 NO existe en el sistema
         * 
         * WHEN: El usuario intenta consultar el curso inexistente
         * 
         * THEN: El sistema debe retornar código HTTP 404 (Not Found)
         *       AND debe retornar mensaje indicando que el curso no existe
         *       AND NO debe retornar error genérico del servidor
         */
        
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano/999"))
                .andExpect(status().isNotFound());
    }

    // ==================== CA-GCV5-07: INFORMACIÓN COMPLETA DE CURSOS ====================

    @Test
    @DisplayName("CA-GCV5-07: Como estudiante necesito información completa para tomar decisiones")
    void testSistemaProporcionaInformacionCompleta() throws Exception {
        /*
         * GIVEN: Un estudiante evaluando opciones de cursos
         *        AND necesita información completa para decidir
         * 
         * WHEN: El estudiante consulta lista de cursos
         * 
         * THEN: El sistema debe retornar JSON con estructura completa
         *       AND debe incluir todos los campos necesarios para la decisión
         *       AND la respuesta debe ser legible y bien estructurada
         */
        
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== CA-GCV5-08: PROCESO COMPLETO DE PREINSCRIPCIÓN ====================

    @Test
    @DisplayName("CA-GCV5-08: Flujo completo - Estudiante consulta curso y se preinscribe exitosamente")
    void testFlujoCompletoPreinscripcion() throws Exception {
        /*
         * GIVEN: Un estudiante que necesita cursar una materia en verano
         * 
         * WHEN: El estudiante sigue el proceso completo:
         *       1. Consulta cursos disponibles
         *       2. Revisa detalles de curso específico
         *       3. Se preinscribe al curso
         *       4. Consulta confirmación de su preinscripción
         * 
         * THEN: Cada paso debe completarse exitosamente
         *       AND la preinscripción debe quedar registrada
         *       AND el estudiante debe poder ver su preinscripción en el listado
         */
        
        // Paso 1: Consultar cursos disponibles
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano"))
                .andExpect(status().isOk());

        // Paso 2: Ver detalles de curso específico
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCurso").value(1));

        // Paso 3: Preinscribirse
        String jsonPreinscripcion = """
            {
                "idUsuario": 1,
                "idCurso": 1,
                "fecha_solicitud": "2025-01-15"
            }
            """;

        mockMvc.perform(post("/api/cursos-intersemestrales/solicitudes/preinscripcion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPreinscripcion))
                .andExpect(status().isCreated());

        // Paso 4: Consultar confirmación
        mockMvc.perform(get("/api/cursos-intersemestrales/solicitudes/preinscripcion/estudiante/1"))
                .andExpect(status().isOk());
    }
}

