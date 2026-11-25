package co.edu.unicauca.decanatura.gestion_curricular.estadisticas;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * PRUEBAS DE INTEGRACIÓN - MÓDULO ME6 (Estadísticas)
 * Objetivo: Probar la integración de los endpoints de estadísticas
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "Coordinador")
@Transactional
@DisplayName("Pruebas de Integración - REST API Estadísticas")
class EstadisticasIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithAnonymousUser
    @DisplayName("Seguridad: sin iniciar sesión no se pueden consultar estadísticas")
    void testEstadisticasSinAutenticacionRechazadas() throws Exception {
        mockMvc.perform(get("/api/estadisticas/globales"))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(get("/api/estadisticas/reportes").param("formato", "excel"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "Estudiante")
    @DisplayName("Seguridad: un estudiante no puede consultar estadísticas ni pedir reportes")
    void testEstadisticasEstudianteRechazado() throws Exception {
        mockMvc.perform(get("/api/estadisticas/globales"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/estadisticas/reportes").param("formato", "pdf"))
                .andExpect(status().isForbidden());
    }

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
        mockMvc.perform(get("/api/estadisticas/estadisticas-por-proceso"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test 3: Obtener estadísticas por estado retorna 200 OK")
    void testObtenerEstadisticasPorEstadoRetorna200() throws Exception {
        mockMvc.perform(get("/api/estadisticas/estado-solicitudes"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test 4: Obtener estadísticas por programa retorna 200 OK")
    void testObtenerEstadisticasPorProgramaRetorna200() throws Exception {
        mockMvc.perform(get("/api/estadisticas/por-programa"))
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
        mockMvc.perform(get("/api/estadisticas/reportes").param("formato", "pdf"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF));
    }

    @Test
    @DisplayName("Test 10: Exportar estadísticas a Excel retorna archivo")
    void testExportarEstadisticasExcelRetornaArchivo() throws Exception {
        mockMvc.perform(get("/api/estadisticas/reportes").param("formato", "excel"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().contentTypeCompatibleWith(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
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

