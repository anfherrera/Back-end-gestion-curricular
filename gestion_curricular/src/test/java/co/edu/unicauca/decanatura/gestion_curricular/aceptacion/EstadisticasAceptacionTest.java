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
 * PRUEBAS DE ACEPTACIÓN - ESTADÍSTICAS (ME6)
 * ============================================================
 *
 * Objetivo: Validar CRITERIOS DE ACEPTACIÓN definidos por el cliente/negocio
 * para el módulo de generación de estadísticas institucionales.
 *
 * Criterios de Aceptación cubiertos:
 * 1. CA-ME6-01: Directivo puede consultar estadísticas globales
 * 2. CA-ME6-02: Coordinador puede analizar estadísticas por proceso
 * 3. CA-ME6-03: Usuario puede exportar estadísticas a PDF
 * 4. CA-ME6-04: Usuario puede exportar estadísticas a Excel
 * 5. CA-ME6-05: Sistema proporciona estadísticas en tiempo real
 *
 * @author Andrés Felipe Herrera Artunduaga
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Pruebas de Aceptación - Estadísticas")
class EstadisticasAceptacionTest {

    @Autowired
    private MockMvc mockMvc;

    // ==================== CA-ME6-01: DIRECTIVO CONSULTA ESTADÍSTICAS GLOBALES ====================

    @Test
    @DisplayName("CA-ME6-01: Como directivo quiero ver estadísticas globales para tomar decisiones estratégicas")
    void testDirectivoConsultaEstadisticasGlobales() throws Exception {
        /*
         * GIVEN: Un directivo (Decano/Vicerrector) autenticado
         *        AND existen solicitudes registradas en el sistema
         *        AND las solicitudes pertenecen a diferentes procesos
         * 
         * WHEN: El directivo consulta estadísticas globales
         * 
         * THEN: El sistema debe retornar un resumen consolidado
         *       AND debe retornar código HTTP 200 (OK)
         *       AND debe incluir total de solicitudes de todos los procesos
         *       AND debe mostrar distribución por tipo de proceso
         *       AND debe mostrar distribución por estado
         *       AND los datos deben ser actuales (tiempo real o cache reciente)
         */
        
        mockMvc.perform(get("/api/estadisticas/globales"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalSolicitudes").exists())
                .andExpect(jsonPath("$.porTipoProceso").exists());
    }

    // ==================== CA-ME6-02: ANÁLISIS POR PROCESO ESPECÍFICO ====================

    @Test
    @DisplayName("CA-ME6-02: Como coordinador quiero analizar un proceso específico para identificar mejoras")
    void testCoordinadorAnalizaProcesoEspecifico() throws Exception {
        /*
         * GIVEN: Un coordinador evaluando el proceso de Paz y Salvo
         *        AND existen solicitudes de Paz y Salvo en diferentes estados
         * 
         * WHEN: El coordinador filtra estadísticas por proceso "PAZ_Y_SALVO"
         * 
         * THEN: El sistema debe retornar estadísticas del proceso solicitado
         *       AND debe retornar código HTTP 200 (OK)
         *       AND debe incluir métricas específicas del proceso
         *       AND debe mostrar distribución por estado
         *       AND debe calcular tiempos promedio si aplica
         */
        
        mockMvc.perform(get("/api/estadisticas/proceso/PAZ_Y_SALVO"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("CA-ME6-02.1: Como coordinador quiero analizar estadísticas por estado")
    void testCoordinadorAnalizaPorEstado() throws Exception {
        /*
         * GIVEN: Un coordinador monitoreando carga de trabajo
         *        AND existen solicitudes en estado PENDIENTE
         * 
         * WHEN: El coordinador consulta estadísticas de solicitudes PENDIENTES
         * 
         * THEN: El sistema debe retornar métricas de solicitudes pendientes
         *       AND debe mostrar cantidad por proceso
         *       AND debe ayudar a priorizar trabajo
         */
        
        mockMvc.perform(get("/api/estadisticas/estado/PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== CA-ME6-03: EXPORTACIÓN A PDF ====================

    @Test
    @DisplayName("CA-ME6-03: Como secretaria quiero exportar estadísticas a PDF para presentarlas en reunión")
    void testUsuarioExportaEstadisticasPDF() throws Exception {
        /*
         * GIVEN: Un usuario que necesita presentar estadísticas en reunión oficial
         *        AND las estadísticas están disponibles en el sistema
         * 
         * WHEN: El usuario solicita exportar estadísticas a formato PDF
         * 
         * THEN: El sistema debe generar un documento PDF profesional
         *       AND debe retornar código HTTP 200 (OK)
         *       AND debe incluir gráficos y tablas legibles
         *       AND debe tener formato oficial de la universidad
         *       AND debe incluir fecha de generación
         *       AND debe retornar con Content-Type application/pdf
         *       AND debe sugerir nombre de archivo descriptivo
         */
        
        mockMvc.perform(get("/api/estadisticas/exportar/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF));
    }

    // ==================== CA-ME6-04: EXPORTACIÓN A EXCEL ====================

    @Test
    @DisplayName("CA-ME6-04: Como analista quiero exportar datos a Excel para análisis avanzados")
    void testAnalistaExportaEstadisticasExcel() throws Exception {
        /*
         * GIVEN: Un analista que necesita realizar análisis estadísticos detallados
         *        AND el analista usa herramientas de análisis de datos (Excel, R, Python)
         * 
         * WHEN: El analista solicita exportar estadísticas a formato Excel
         * 
         * THEN: El sistema debe generar un archivo Excel válido
         *       AND debe retornar código HTTP 200 (OK)
         *       AND debe incluir datos en formato tabular
         *       AND debe ser editable en Microsoft Excel
         *       AND debe retornar con Content-Type correcto
         *       AND debe permitir análisis posterior con fórmulas/pivotes
         */
        
        mockMvc.perform(get("/api/estadisticas/exportar/excel"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")));
    }

    // ==================== CA-ME6-05: ESTADÍSTICAS EN TIEMPO REAL ====================

    @Test
    @DisplayName("CA-ME6-05: Como usuario quiero datos actualizados para decisiones informadas")
    void testSistemaProporcionaDatosActualizados() throws Exception {
        /*
         * GIVEN: Un usuario consultando estadísticas
         *        AND se han creado nuevas solicitudes recientemente
         * 
         * WHEN: El usuario consulta estadísticas globales
         * 
         * THEN: El sistema debe retornar datos actualizados
         *       AND debe reflejar cambios recientes en el sistema
         *       AND los datos deben ser consistentes
         *       AND debe retornar en tiempo razonable (< 3 segundos)
         */
        
        mockMvc.perform(get("/api/estadisticas/globales"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== CA-ME6-06: ESTADÍSTICAS POR PROGRAMA ====================

    @Test
    @DisplayName("CA-ME6-06: Como director quiero comparar rendimiento entre programas")
    void testDirectorComparaProgramas() throws Exception {
        /*
         * GIVEN: Un director de facultad evaluando programas
         *        AND existen solicitudes de múltiples programas académicos
         * 
         * WHEN: El director consulta estadísticas de un programa específico
         * 
         * THEN: El sistema debe retornar métricas del programa solicitado
         *       AND debe permitir comparación con otros programas
         *       AND debe mostrar fortalezas y áreas de mejora
         */
        
        mockMvc.perform(get("/api/estadisticas/programa/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== CA-ME6-07: ANÁLISIS DE TENDENCIAS ====================

    @Test
    @DisplayName("CA-ME6-07: Como directivo quiero identificar tendencias para planificación")
    void testDirectivoIdentificaTendencias() throws Exception {
        /*
         * GIVEN: Un directivo planificando recursos para próximos periodos
         *        AND existen datos históricos de múltiples periodos
         * 
         * WHEN: El directivo consulta análisis de tendencias
         * 
         * THEN: El sistema debe comparar datos actuales con históricos
         *       AND debe identificar tendencias al alza o a la baja
         *       AND debe proporcionar insights para planificación
         */
        
        mockMvc.perform(get("/api/estadisticas/tendencias"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== CA-ME6-08: DASHBOARD EJECUTIVO ====================

    @Test
    @DisplayName("CA-ME6-08: Como rector quiero dashboard consolidado para junta directiva")
    void testRectorAccedeDashboardEjecutivo() throws Exception {
        /*
         * GIVEN: Un rector preparando presentación para junta directiva
         *        AND necesita visión consolidada de toda la institución
         * 
         * WHEN: El rector accede al dashboard ejecutivo
         * 
         * THEN: El sistema debe retornar KPIs principales
         *       AND debe presentar información de forma visual
         *       AND debe incluir métricas estratégicas
         *       AND debe ser comprensible sin conocimiento técnico
         */
        
        mockMvc.perform(get("/api/estadisticas/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== CA-ME6-09: MÉTRICAS DE RENDIMIENTO ====================

    @Test
    @DisplayName("CA-ME6-09: Como responsable de procesos quiero evaluar eficiencia")
    void testResponsableEvaluaEficiencia() throws Exception {
        /*
         * GIVEN: Un responsable de procesos buscando optimizar tiempos
         *        AND existen solicitudes procesadas con registro de tiempo
         * 
         * WHEN: El responsable consulta métricas de rendimiento
         * 
         * THEN: El sistema debe calcular tiempos promedio de procesamiento
         *       AND debe identificar cuellos de botella
         *       AND debe comparar con objetivos establecidos
         */
        
        mockMvc.perform(get("/api/estadisticas/rendimiento"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== CA-ME6-10: ESTADÍSTICAS DE CURSOS DE VERANO ====================

    @Test
    @DisplayName("CA-ME6-10: Como coordinador quiero analizar demanda de cursos de verano")
    void testCoordinadorAnalizaDemandaCursosVerano() throws Exception {
        /*
         * GIVEN: Un coordinador planificando oferta de cursos para próximo verano
         *        AND existen datos históricos de inscripciones
         * 
         * WHEN: El coordinador consulta estadísticas de cursos de verano
         * 
         * THEN: El sistema debe mostrar demanda por materia
         *       AND debe mostrar tasas de aprobación
         *       AND debe ayudar a decidir qué cursos ofertar
         */
        
        mockMvc.perform(get("/api/estadisticas/cursos-verano"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== CA-ME6-11: FLUJO COMPLETO DE ANÁLISIS ====================

    @Test
    @DisplayName("CA-ME6-11: Flujo completo - Coordinador analiza, identifica problema y exporta reporte")
    void testFlujoCompletoAnalisisYExportacion() throws Exception {
        /*
         * GIVEN: Un coordinador que necesita presentar análisis en reunión
         * 
         * WHEN: El coordinador sigue el proceso completo:
         *       1. Consulta estadísticas globales para contexto
         *       2. Analiza su programa específico
         *       3. Identifica proceso con más solicitudes
         *       4. Analiza tendencias
         *       5. Exporta reporte en PDF
         * 
         * THEN: Cada paso debe completarse exitosamente
         *       AND debe obtener información valiosa en cada paso
         *       AND debe poder exportar reporte profesional al final
         */
        
        // Paso 1: Vista global
        mockMvc.perform(get("/api/estadisticas/globales"))
                .andExpect(status().isOk());

        // Paso 2: Programa específico
        mockMvc.perform(get("/api/estadisticas/programa/1"))
                .andExpect(status().isOk());

        // Paso 3: Proceso con más solicitudes
        mockMvc.perform(get("/api/estadisticas/proceso/PAZ_Y_SALVO"))
                .andExpect(status().isOk());

        // Paso 4: Tendencias
        mockMvc.perform(get("/api/estadisticas/tendencias"))
                .andExpect(status().isOk());

        // Paso 5: Exportar reporte
        mockMvc.perform(get("/api/estadisticas/exportar/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF));
    }
}

