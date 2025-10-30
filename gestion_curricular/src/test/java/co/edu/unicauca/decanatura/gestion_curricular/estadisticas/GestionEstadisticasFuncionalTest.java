package co.edu.unicauca.decanatura.gestion_curricular.estadisticas;

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
 * PRUEBAS FUNCIONALES - GENERACIÓN DE ESTADÍSTICAS (ME6)
 * ============================================================
 *
 * Objetivo: Validar FLUJOS COMPLETOS DE NEGOCIO del módulo de Estadísticas.
 * Estas pruebas se enfocan en casos de uso end-to-end para la generación,
 * consulta y exportación de estadísticas institucionales.
 *
 * Escenarios de negocio cubiertos:
 * 1. Flujo completo de consulta de estadísticas globales
 * 2. Flujo de generación de reportes por proceso
 * 3. Flujo de análisis de tendencias
 * 4. Flujo de exportación de datos
 *
 * @author Andrés Felipe Herrera Artunduaga
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Pruebas Funcionales - Generación de Estadísticas")
class GestionEstadisticasFuncionalTest {

    @Autowired
    private MockMvc mockMvc;

    // ==================== FLUJO 1: CONSULTA DE ESTADÍSTICAS GLOBALES ====================

    @Test
    @DisplayName("Funcional 1: Flujo completo - Decano consulta estadísticas globales")
    void testFlujoConsultaEstadisticasGlobales() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Decano ingresa al módulo de estadísticas
         * 2. Decano solicita vista general de todas las solicitudes
         * 3. Sistema genera estadísticas consolidadas de todos los procesos
         * 4. Decano visualiza métricas clave (total solicitudes, aprobadas, pendientes)
         * 
         * ACTORES: Decano, Directivos
         * RESULTADO ESPERADO: Dashboard con estadísticas globales
         */
        
        mockMvc.perform(get("/api/estadisticas/globales"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalSolicitudes").exists())
                .andExpect(jsonPath("$.porTipoProceso").exists());
    }

    // ==================== FLUJO 2: ESTADÍSTICAS POR PROCESO ====================

    @Test
    @DisplayName("Funcional 2: Flujo de análisis - Coordinador analiza estadísticas de Paz y Salvo")
    void testFlujoAnalisisPorProceso() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Coordinador necesita evaluar el proceso de Paz y Salvo
         * 2. Coordinador filtra estadísticas por tipo de proceso
         * 3. Sistema muestra métricas específicas del proceso seleccionado
         * 4. Coordinador identifica cuellos de botella o problemas
         * 
         * ACTORES: Coordinador de programa
         * RESULTADO ESPERADO: Estadísticas detalladas de un proceso específico
         */
        
        mockMvc.perform(get("/api/estadisticas/proceso/PAZ_Y_SALVO"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== FLUJO 3: ESTADÍSTICAS POR ESTADO ====================

    @Test
    @DisplayName("Funcional 3: Flujo de monitoreo - Funcionario monitorea solicitudes pendientes")
    void testFlujoMonitoreoPorEstado() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Funcionario necesita priorizar trabajo del día
         * 2. Funcionario consulta estadísticas de solicitudes por estado
         * 3. Sistema muestra cantidad de solicitudes pendientes, en proceso, etc.
         * 4. Funcionario organiza su trabajo según prioridades
         * 
         * ACTORES: Funcionario administrativo
         * RESULTADO ESPERADO: Estadísticas por estado de solicitud
         */
        
        mockMvc.perform(get("/api/estadisticas/estado/PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== FLUJO 4: ESTADÍSTICAS POR PROGRAMA ====================

    @Test
    @DisplayName("Funcional 4: Flujo de comparación - Director compara programas académicos")
    void testFlujoComparacionPorPrograma() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Director de facultad necesita comparar rendimiento entre programas
         * 2. Director consulta estadísticas por programa académico
         * 3. Sistema genera métricas específicas por programa
         * 4. Director identifica programas que requieren más apoyo
         * 
         * ACTORES: Director de facultad
         * RESULTADO ESPERADO: Estadísticas comparativas por programa
         */
        
        mockMvc.perform(get("/api/estadisticas/programa/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== FLUJO 5: ANÁLISIS DE TENDENCIAS ====================

    @Test
    @DisplayName("Funcional 5: Flujo de análisis temporal - Decano analiza tendencias")
    void testFlujoAnalisisTendencias() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Decano necesita identificar patrones y tendencias
         * 2. Decano solicita análisis de tendencias temporales
         * 3. Sistema compara datos históricos y actuales
         * 4. Decano toma decisiones estratégicas basadas en datos
         * 
         * ACTORES: Decano, Planificadores institucionales
         * RESULTADO ESPERADO: Análisis de tendencias con comparativas
         */
        
        mockMvc.perform(get("/api/estadisticas/tendencias"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== FLUJO 6: DASHBOARD EJECUTIVO ====================

    @Test
    @DisplayName("Funcional 6: Flujo de visualización - Directivo accede a dashboard")
    void testFlujoDashboardEjecutivo() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Directivo ingresa al sistema para reunión de seguimiento
         * 2. Directivo accede al dashboard consolidado
         * 3. Sistema presenta visualización ejecutiva de todas las métricas
         * 4. Directivo presenta datos en reunión de dirección
         * 
         * ACTORES: Decano, Vicerrector, Rector
         * RESULTADO ESPERADO: Dashboard con KPIs principales
         */
        
        mockMvc.perform(get("/api/estadisticas/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== FLUJO 7: ANÁLISIS DE RENDIMIENTO ====================

    @Test
    @DisplayName("Funcional 7: Flujo de evaluación - Coordinador evalúa tiempos de respuesta")
    void testFlujoEvaluacionRendimiento() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Coordinador necesita evaluar eficiencia del proceso
         * 2. Coordinador consulta métricas de rendimiento
         * 3. Sistema calcula tiempos promedio de procesamiento
         * 4. Coordinador propone mejoras al proceso
         * 
         * ACTORES: Coordinador, Responsable de procesos
         * RESULTADO ESPERADO: Métricas de rendimiento y tiempos
         */
        
        mockMvc.perform(get("/api/estadisticas/rendimiento"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== FLUJO 8: ESTADÍSTICAS ESPECÍFICAS DE CURSOS DE VERANO ====================

    @Test
    @DisplayName("Funcional 8: Flujo de planificación - Coordinador planifica cursos de verano")
    void testFlujoPlanificacionCursosVerano() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Coordinador necesita planificar oferta de cursos de verano
         * 2. Coordinador consulta estadísticas históricas de cursos
         * 3. Sistema muestra demanda, aprobación, deserción por materia
         * 4. Coordinador toma decisiones sobre qué cursos ofertar
         * 
         * ACTORES: Coordinador académico
         * RESULTADO ESPERADO: Estadísticas de cursos de verano
         */
        
        mockMvc.perform(get("/api/estadisticas/cursos-verano"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== FLUJO 9: EXPORTACIÓN DE DATOS ====================

    @Test
    @DisplayName("Funcional 9: Flujo de exportación - Secretaria genera reporte PDF")
    void testFlujoExportacionPDF() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Secretaria necesita presentar estadísticas en reunión oficial
         * 2. Secretaria solicita exportación de estadísticas a PDF
         * 3. Sistema genera documento PDF con gráficos y tablas
         * 4. Secretaria descarga e imprime reporte
         * 
         * ACTORES: Secretaria de facultad
         * RESULTADO ESPERADO: Documento PDF con estadísticas
         */
        
        mockMvc.perform(get("/api/estadisticas/exportar/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF));
    }

    @Test
    @DisplayName("Funcional 10: Flujo de exportación - Analista exporta datos a Excel")
    void testFlujoExportacionExcel() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Analista necesita realizar análisis avanzados en Excel
         * 2. Analista solicita exportación de estadísticas a Excel
         * 3. Sistema genera archivo Excel con datos estructurados
         * 4. Analista realiza análisis adicionales con herramientas propias
         * 
         * ACTORES: Analista de datos institucional
         * RESULTADO ESPERADO: Archivo Excel con datos estructurados
         */
        
        mockMvc.perform(get("/api/estadisticas/exportar/excel"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")));
    }

    // ==================== FLUJO 11: FILTRADO DINÁMICO ====================

    @Test
    @DisplayName("Funcional 11: Flujo de análisis personalizado - Usuario aplica múltiples filtros")
    void testFlujoFiltraoDinamico() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Usuario necesita análisis muy específico
         * 2. Usuario aplica filtros dinámicos (proceso, fecha, programa, estado)
         * 3. Sistema genera estadísticas personalizadas
         * 4. Usuario obtiene respuestas a preguntas específicas
         * 
         * ACTORES: Cualquier usuario autorizado
         * RESULTADO ESPERADO: Estadísticas filtradas según criterios
         */
        
        mockMvc.perform(get("/api/estadisticas/filtros-dinamicos")
                        .param("proceso", "PAZ_Y_SALVO")
                        .param("estado", "APROBADO"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== FLUJO 12: ESTADÍSTICAS CONSOLIDADAS ====================

    @Test
    @DisplayName("Funcional 12: Flujo de reporte ejecutivo - Rector solicita informe consolidado")
    void testFlujoReporteConsolidado() throws Exception {
        /*
         * FLUJO DE NEGOCIO:
         * 1. Rector necesita informe para junta directiva
         * 2. Rector solicita estadísticas consolidadas de todo el sistema
         * 3. Sistema genera reporte completo con todas las métricas
         * 4. Rector presenta resultados en junta directiva
         * 
         * ACTORES: Rector, Vicerrectores
         * RESULTADO ESPERADO: Reporte consolidado institucional
         */
        
        mockMvc.perform(get("/api/estadisticas/consolidadas"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== FLUJO 13: FLUJO COMPLETO DE ANÁLISIS ====================

    @Test
    @DisplayName("Funcional 13: Flujo completo - Coordinador realiza análisis integral")
    void testFlujoCompletoAnalisisIntegral() throws Exception {
        /*
         * FLUJO DE NEGOCIO COMPLETO:
         * 1. Coordinador consulta estadísticas globales para contexto
         * 2. Coordinador analiza estadísticas de su programa específico
         * 3. Coordinador identifica proceso con más solicitudes
         * 4. Coordinador analiza tendencias del proceso identificado
         * 5. Coordinador exporta reporte PDF para presentar a dirección
         * 
         * ACTORES: Coordinador de programa
         * RESULTADO ESPERADO: Análisis completo con reporte exportado
         */
        
        // Paso 1: Vista global
        mockMvc.perform(get("/api/estadisticas/globales"))
                .andExpect(status().isOk());

        // Paso 2: Estadísticas del programa
        mockMvc.perform(get("/api/estadisticas/programa/1"))
                .andExpect(status().isOk());

        // Paso 3: Analizar proceso específico
        mockMvc.perform(get("/api/estadisticas/proceso/PAZ_Y_SALVO"))
                .andExpect(status().isOk());

        // Paso 4: Ver tendencias
        mockMvc.perform(get("/api/estadisticas/tendencias"))
                .andExpect(status().isOk());

        // Paso 5: Exportar reporte
        mockMvc.perform(get("/api/estadisticas/exportar/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF));
    }
}

