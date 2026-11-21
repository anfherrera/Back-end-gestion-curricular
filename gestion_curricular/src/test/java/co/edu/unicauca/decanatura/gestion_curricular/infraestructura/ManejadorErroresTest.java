package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * El manejador global (RestApiExceptionHandler) responde con el código HTTP adecuado y siempre
 * con el mismo formato: codigoError, mensaje, codigoHttp, url y metodo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Manejador global de errores")
class ManejadorErroresTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "Funcionario")
    @DisplayName("Solicitud inexistente: 404 con mensaje (antes 500)")
    void solicitudInexistente() throws Exception {
        mockMvc.perform(get("/api/solicitudes-homologacion/listarSolicitud-Homologacion/987654"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigoHttp").value(404))
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }

    @Test
    @WithMockUser(roles = "Funcionario")
    @DisplayName("JSON mal formado: 400 con mensaje")
    void jsonMalFormado() throws Exception {
        mockMvc.perform(put("/api/solicitudes-homologacion/actualizarEstadoSolicitud")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigoError").value("GC-0400"))
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }

    @Test
    @WithMockUser(roles = "Estudiante")
    @DisplayName("Sin permisos: 403 con mensaje")
    void sinPermisos() throws Exception {
        mockMvc.perform(get("/api/usuarios/listarUsuarios"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigoError").value("GC-0403"));
    }
}
