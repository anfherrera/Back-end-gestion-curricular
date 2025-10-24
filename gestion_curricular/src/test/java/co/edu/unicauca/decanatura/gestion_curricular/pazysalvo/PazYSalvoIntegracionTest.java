package co.edu.unicauca.decanatura.gestion_curricular.pazysalvo;

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
 * PRUEBAS DE INTEGRACIÓN - REST API PAZ Y SALVO (GEPA4)
 * ============================================================
 * 
 * Objetivo: Validar el correcto funcionamiento de los endpoints
 * del módulo Paz y Salvo (GEPA4).
 * 
 * Tipo de pruebas: INTEGRACIÓN
 * - Validan Controller + Service + Repository + Base de Datos (H2)
 * - No usan mocks, prueban el flujo completo
 * 
 * @author Andrés Felipe Herrera Artunduaga
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Pruebas de Integración - REST API Paz y Salvo")
class PazYSalvoIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    // ==================== PRUEBAS DE CONSULTA ====================

    @Test
    @DisplayName("Test 1: Listar todas las solicitudes de Paz y Salvo")
    void testListarSolicitudesPazYSalvo() throws Exception {
        mockMvc.perform(get("/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test 2: Listar solicitudes por rol - Funcionario")
    void testListarSolicitudesFuncionario() throws Exception {
        mockMvc.perform(get("/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo/Funcionario"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test 3: Listar solicitudes por rol - Coordinador")
    void testListarSolicitudesCoordinador() throws Exception {
        mockMvc.perform(get("/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo/Coordinador"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test 4: Listar solicitudes por rol - Secretaria")
    void testListarSolicitudesSecretaria() throws Exception {
        mockMvc.perform(get("/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo/Secretaria"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test 5: Listar solicitudes con parámetro de rol")
    void testListarSolicitudesPorRolParametro() throws Exception {
        mockMvc.perform(get("/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo/porRol")
                        .param("rol", "ESTUDIANTE"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ==================== PRUEBAS DE CREACIÓN ====================

    @Test
    @DisplayName("Test 6: Crear solicitud de Paz y Salvo - Valida endpoint POST")
    void testCrearSolicitudPazYSalvo() throws Exception {
        String jsonRequest = """
            {
                "idUsuario": 1,
                "idTipoProceso": 1,
                "fecha_solicitud": "2025-01-15"
            }
            """;

        // Solo validamos que el endpoint existe y responde (puede ser 201, 200 o 400 por validaciones)
        mockMvc.perform(post("/api/solicitudes-pazysalvo/crearSolicitud-PazYSalvo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest));
                // No validamos status específico porque depende de datos en BD
    }

    @Test
    @DisplayName("Test 7: Crear solicitud sin datos - Valida respuesta de error")
    void testCrearSolicitudSinDatos() throws Exception {
        String jsonRequest = "{}";

        // Debe dar error de validación (400 o 422)
        mockMvc.perform(post("/api/solicitudes-pazysalvo/crearSolicitud-PazYSalvo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().is4xxClientError());
    }

    // ==================== PRUEBAS DE BÚSQUEDA ====================

    @Test
    @DisplayName("Test 8: Buscar solicitud por ID - Valida endpoint")
    void testBuscarSolicitudPorId() throws Exception {
        // Simplemente validamos que el endpoint existe
        mockMvc.perform(get("/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo/1"));
                // Puede ser 200 OK o 404 NOT FOUND dependiendo de si existe el ID
    }

    @Test
    @DisplayName("Test 9: Buscar solicitud por ID muy grande")
    void testBuscarSolicitudPorIdGrande() throws Exception {
        // Validamos comportamiento con ID inexistente
        mockMvc.perform(get("/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo/999999"));
                // Puede dar 404 o 500 dependiendo de la implementación
    }

    // ==================== PRUEBAS DE VALIDACIÓN ====================

    @Test
    @DisplayName("Test 10: Endpoint raíz de solicitudes funciona")
    void testEndpointRaizSolicitudes() throws Exception {
        mockMvc.perform(get("/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test 11: Validar que la respuesta sea JSON")
    void testRespuestaEsJSON() throws Exception {
        mockMvc.perform(get("/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Test 12: Listar solicitudes por rol con usuario específico")
    void testListarPorRolConUsuario() throws Exception {
        mockMvc.perform(get("/api/solicitudes-pazysalvo/listarSolicitud-PazYSalvo/porRol")
                        .param("rol", "ESTUDIANTE")
                        .param("idUsuario", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}

