package co.edu.unicauca.decanatura.gestion_curricular.ecaes;

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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Integración - Solicitudes ECAES y Fechas")
public class EcaesIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Listar fechas ECAES")
    void listarFechasEcaes() throws Exception {
        mockMvc.perform(get("/api/solicitudes-ecaes/listarFechasEcaes"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Publicar fechas ECAES - valida endpoint")
    void publicarFechasEcaes() throws Exception {
        String body = "{"
                + "\"periodoAcademico\": \"2025-1\"," 
                + "\"fechaInicioInscripciones\": \"2025-02-01\"," 
                + "\"fechaFinInscripciones\": \"2025-02-15\"," 
                + "\"fechaExamen\": \"2025-03-10\""
                + "}";

        mockMvc.perform(post("/api/solicitudes-ecaes/publicarFechasEcaes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body));
    }

    @Test
    @DisplayName("Crear solicitud ECAES - valida endpoint")
    void crearSolicitudEcaes() throws Exception {
        String body = "{"
                + "\"id_usuario\": 1,"
                + "\"id_programa\": 1,"
                + "\"periodoAcademico\": \"2025-1\","
                + "\"observaciones\": \"Solicitud ECAES de prueba\""
                + "}";

        mockMvc.perform(post("/api/solicitudes-ecaes/crearSolicitud-Ecaes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body));
    }

    @Test
    @DisplayName("Actualizar estado solicitud ECAES - valida endpoint")
    void actualizarEstadoSolicitudEcaes() throws Exception {
        String body = "{"
                + "\"idSolicitud\": 1,"
                + "\"nuevoEstado\": \"ENVIADA\""
                + "}";

        mockMvc.perform(put("/api/solicitudes-ecaes/actualizarEstadoSolicitud")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body));
    }
}


