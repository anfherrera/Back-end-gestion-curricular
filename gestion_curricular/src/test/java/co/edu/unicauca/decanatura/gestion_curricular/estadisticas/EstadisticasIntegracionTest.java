package co.edu.unicauca.decanatura.gestion_curricular.estadisticas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PRUEBAS DE INTEGRACIÓN - MÓDULO ME6 (Estadísticas)
 * Objetivo: Probar la integración de los endpoints de estadísticas
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Pruebas de Integración - REST API Estadísticas")
class EstadisticasIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Test 1: Obtener estadísticas globales retorna 200 OK")
    void testObtenerEstadisticasGlobalesRetorna200() throws Exception {
        mockMvc.perform(get("/api/estadisticas/globales"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalSolicitudes").exists())
                .andExpect(jsonPath("$.porTipoProceso").exists());
    }

    @Test
    @DisplayName("Test 2: Obtener estadísticas por proceso retorna 200 OK")
    void testObtenerEstadisticasPorProcesoRetorna200() throws Exception {
        mockMvc.perform(get("/api/estadisticas/proceso/PAZ_SALVO"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test 3: Obtener estadísticas por estado retorna 200 OK")
    void testObtenerEstadisticasPorEstadoRetorna200() throws Exception {
        mockMvc.perform(get("/api/estadisticas/estado/APROBADA"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test 4: Obtener estadísticas por programa retorna 200 OK")
    void testObtenerEstadisticasPorProgramaRetorna200() throws Exception {
        mockMvc.perform(get("/api/estadisticas/programa/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test 5: Obtener resumen completo de estadísticas retorna 200 OK")
    void testObtenerResumenCompletoRetorna200() throws Exception {
        mockMvc.perform(get("/api/estadisticas/resumen-completo"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test 6: Obtener dashboard ejecutivo retorna 200 OK")
    void testObtenerDashboardEjecutivoRetorna200() throws Exception {
        mockMvc.perform(get("/api/estadisticas/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.resumenGlobal").exists());
    }

    @Test
    @DisplayName("Test 7: Obtener estadísticas de rendimiento retorna 200 OK")
    void testObtenerEstadisticasRendimientoRetorna200() throws Exception {
        mockMvc.perform(get("/api/estadisticas/rendimiento"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.estadisticasGlobales").exists())
                .andExpect(jsonPath("$.indicadoresRendimiento").exists());
    }

    @Test
    @DisplayName("Test 8: Obtener estadísticas de cursos de verano retorna 200 OK")
    void testObtenerEstadisticasCursosVeranoRetorna200() throws Exception {
        mockMvc.perform(get("/api/estadisticas/cursos-verano"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test 9: Exportar estadísticas a PDF retorna archivo")
    void testExportarEstadisticasPDFRetornaArchivo() throws Exception {
        mockMvc.perform(get("/api/estadisticas/export/pdf"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF));
    }

    @Test
    @DisplayName("Test 10: Exportar estadísticas a Excel retorna archivo")
    void testExportarEstadisticasExcelRetornaArchivo() throws Exception {
        mockMvc.perform(get("/api/estadisticas/export/excel"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_OCTET_STREAM));
    }

    @Test
    @DisplayName("Test 11: Obtener estadísticas filtradas retorna 200 OK")
    void testObtenerEstadisticasFiltradasRetorna200() throws Exception {
        mockMvc.perform(get("/api/estadisticas/filtradas")
                        .param("nombreProceso", "PAZ_SALVO"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test 12: Obtener total de estudiantes retorna 200 OK")
    void testObtenerTotalEstudiantesRetorna200() throws Exception {
        mockMvc.perform(get("/api/estadisticas/total-estudiantes"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalEstudiantes").exists());
    }

    @Test
    @DisplayName("Test 13: Obtener estudiantes por programa retorna 200 OK")
    void testObtenerEstudiantesPorProgramaRetorna200() throws Exception {
        mockMvc.perform(get("/api/estadisticas/estudiantes-por-programa"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.estudiantesPorPrograma").exists());
    }

    @Test
    @DisplayName("Test 14: Obtener configuración de estilos para dashboard")
    void testObtenerConfiguracionEstilos() throws Exception {
        mockMvc.perform(get("/api/estadisticas/configuracion-estilos"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test 15: Validar estructura de respuesta de estadísticas globales")
    void testValidarEstructuraRespuestaEstadisticasGlobales() throws Exception {
        mockMvc.perform(get("/api/estadisticas/globales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSolicitudes").isNumber())
                .andExpect(jsonPath("$.totalAprobadas").exists())
                .andExpect(jsonPath("$.totalRechazadas").exists())
                .andExpect(jsonPath("$.totalEnProceso").exists())
                .andExpect(jsonPath("$.porcentajeAprobacion").exists());
    }
}

