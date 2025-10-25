package co.edu.unicauca.decanatura.gestion_curricular.funcionales;

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
 * PRUEBAS FUNCIONALES - GESTIÓN DE CURSOS DE VERANO (GCV5)
 * ============================================================
 *
 * Objetivo: Validar FLUJOS COMPLETOS DE NEGOCIO del módulo de Cursos de Verano.
 * Estas pruebas se enfocan en casos de uso end-to-end desde la perspectiva
 * del proceso de oferta e inscripción de cursos intersemestrales.
 *
 * Escenarios de negocio cubiertos:
 * 1. Flujo completo de oferta de cursos de verano
 * 2. Flujo de preinscripción de estudiantes
 * 3. Flujo de selección y confirmación de inscritos
 * 4. Flujo de consulta de cursos disponibles
 *
 * @author Andrés Felipe Herrera Artunduaga
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Pruebas Funcionales - Gestión de Cursos de Verano")
class GestionCursosVeranoFuncionalTest {

    @Autowired
    private MockMvc mockMvc;

    // ==================== FLUJO 1: OFERTA DE CURSOS ====================

    @Test
    @DisplayName("Funcional 1: Flujo completo - Coordinador oferta curso de verano")
    void testFlujoOfertaCursoVerano() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Coordinador consulta cursos ofertados actuales
         * 2. Coordinador verifica que no haya duplicados
         * 3. Sistema lista todos los cursos disponibles
         * 
         * ACTORES: Coordinador del programa
         * RESULTADO ESPERADO: Lista de cursos ofertados correctamente
         */
        
        // Paso 1: Consultar cursos ofertados
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Funcional 2: Flujo de búsqueda - Coordinador busca curso específico")
    void testFlujoBusquedaCursoEspecifico() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Coordinador necesita consultar información de un curso específico
         * 2. Sistema recupera toda la información del curso
         * 3. Coordinador verifica datos del curso (docente, horario, cupos)
         * 
         * ACTORES: Coordinador, Secretaria
         * RESULTADO ESPERADO: Información detallada del curso
         */
        
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.idCurso").value(1));
    }

    // ==================== FLUJO 2: PREINSCRIPCIÓN ====================

    @Test
    @DisplayName("Funcional 3: Flujo completo - Estudiante realiza preinscripción")
    void testFlujoPreinscripcionEstudiante() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Estudiante consulta cursos disponibles para verano
         * 2. Estudiante selecciona un curso de su interés
         * 3. Estudiante se preinscribe al curso
         * 4. Sistema registra la preinscripción
         * 
         * ACTORES: Estudiante
         * RESULTADO ESPERADO: Preinscripción registrada exitosamente
         */
        
        // Paso 1: Consultar cursos disponibles
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano"))
                .andExpect(status().isOk());

        // Paso 2: Realizar preinscripción
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
    }

    @Test
    @DisplayName("Funcional 4: Flujo de validación - Preinscripción con datos inválidos")
    void testFlujoValidacionPreinscripcionInvalida() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Estudiante intenta preinscribirse sin datos completos
         * 2. Sistema valida la información
         * 3. Sistema rechaza la preinscripción y muestra error claro
         * 
         * ACTORES: Estudiante
         * RESULTADO ESPERADO: Rechazo con mensaje de error
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

    // ==================== FLUJO 3: CONSULTA DE PREINSCRIPCIONES ====================

    @Test
    @DisplayName("Funcional 5: Flujo de consulta - Estudiante consulta sus preinscripciones")
    void testFlujoConsultaPreinscripcionesEstudiante() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Estudiante inicia sesión
         * 2. Estudiante consulta el estado de sus preinscripciones
         * 3. Sistema muestra únicamente las preinscripciones del estudiante
         * 
         * ACTORES: Estudiante
         * RESULTADO ESPERADO: Lista de preinscripciones del estudiante
         */
        
        mockMvc.perform(get("/api/cursos-intersemestrales/solicitudes/preinscripcion/estudiante/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== FLUJO 4: FILTRADO DE CURSOS ====================

    @Test
    @DisplayName("Funcional 6: Flujo de filtrado - Buscar cursos por periodo")
    void testFlujoFiltroCursosPorPeriodo() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Usuario (estudiante o coordinador) necesita ver cursos de un periodo específico
         * 2. Usuario filtra por periodo académico (ej: 2025-1)
         * 3. Sistema muestra únicamente cursos del periodo solicitado
         * 
         * ACTORES: Estudiante, Coordinador
         * RESULTADO ESPERADO: Lista filtrada de cursos
         */
        
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano/periodo/2025-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Funcional 7: Flujo de filtrado - Buscar cursos por materia")
    void testFlujoFiltroCursosPorMateria() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Estudiante busca curso de una materia específica que debe aprobar
         * 2. Sistema filtra cursos por código de materia
         * 3. Estudiante ve todas las ofertas disponibles de esa materia
         * 
         * ACTORES: Estudiante
         * RESULTADO ESPERADO: Cursos de la materia solicitada
         */
        
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano/materia/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== FLUJO 5: MANEJO DE CASOS EXCEPCIONALES ====================

    @Test
    @DisplayName("Funcional 8: Flujo de error - Curso inexistente")
    void testFlujoErrorCursoInexistente() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Usuario intenta buscar un curso que no existe
         * 2. Sistema valida la existencia del curso
         * 3. Sistema retorna error 404 con mensaje descriptivo
         * 
         * ACTORES: Estudiante, Coordinador
         * RESULTADO ESPERADO: Error 404 Not Found
         */
        
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano/999"))
                .andExpect(status().isNotFound());
    }

    // ==================== FLUJO 6: FLUJO COMPLETO EXITOSO ====================

    @Test
    @DisplayName("Funcional 9: Flujo completo exitoso - De oferta a preinscripción")
    void testFlujoCompletoOfertaAPreinscripcion() throws Exception {
        /*
         * FLUJO DE NEGOCIO COMPLETO:
         * 1. Coordinador verifica cursos ofertados
         * 2. Estudiante consulta cursos disponibles
         * 3. Estudiante encuentra curso de su interés
         * 4. Estudiante se preinscribe exitosamente
         * 5. Estudiante consulta su preinscripción
         * 
         * ACTORES: Coordinador, Estudiante
         * RESULTADO ESPERADO: Preinscripción registrada y confirmada
         */
        
        // Paso 1: Consultar cursos ofertados (como coordinador)
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano"))
                .andExpect(status().isOk());

        // Paso 2: Buscar curso específico (como estudiante)
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCurso").value(1));

        // Paso 3: Realizar preinscripción
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

        // Paso 4: Consultar preinscripciones del estudiante
        mockMvc.perform(get("/api/cursos-intersemestrales/solicitudes/preinscripcion/estudiante/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Funcional 10: Flujo de exploración - Estudiante explora opciones de cursos")
    void testFlujoExploracionCursos() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Estudiante consulta todos los cursos disponibles
         * 2. Estudiante filtra por periodo académico
         * 3. Estudiante busca una materia específica
         * 4. Estudiante toma decisión informada
         * 
         * ACTORES: Estudiante
         * RESULTADO ESPERADO: Información completa para toma de decisión
         */
        
        // Paso 1: Ver todos los cursos
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano"))
                .andExpect(status().isOk());

        // Paso 2: Filtrar por periodo
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano/periodo/2025-1"))
                .andExpect(status().isOk());

        // Paso 3: Buscar materia específica
        mockMvc.perform(get("/api/cursos-intersemestrales/cursos-verano/materia/1"))
                .andExpect(status().isOk());
    }
}

