package co.edu.unicauca.decanatura.gestion_curricular.homologacion;

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
@DisplayName("Integración - Solicitudes Homologación")
public class HomologacionIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Listar solicitudes homologación - general")
    void listarSolicitudesHomologacion() throws Exception {
        mockMvc.perform(get("/api/solicitudes-homologacion/listarSolicitud-Homologacion"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Listar solicitudes homologación por rol - Funcionario")
    void listarSolicitudesHomologacionFuncionario() throws Exception {
        mockMvc.perform(get("/api/solicitudes-homologacion/listarSolicitud-Homologacion/Funcionario"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Listar solicitudes homologación por rol - Coordinador")
    void listarSolicitudesHomologacionCoordinador() throws Exception {
        mockMvc.perform(get("/api/solicitudes-homologacion/listarSolicitud-Homologacion/Coordinador"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Listar solicitudes homologación por rol - Secretaria")
    void listarSolicitudesHomologacionSecretaria() throws Exception {
        mockMvc.perform(get("/api/solicitudes-homologacion/listarSolicitud-Homologacion/Secretaria"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Crear solicitud homologación - valida endpoint")
    void crearSolicitudHomologacion() throws Exception {
        String json = "{"
                + "\"id_usuario\": 1,"
                + "\"id_programa\": 1,"
                + "\"observaciones\": \"Solicitud de homologación de prueba\""
                + "}";

        mockMvc.perform(post("/api/solicitudes-homologacion/crearSolicitud-Homologacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));
    }

    @Test
    @DisplayName("Actualizar estado solicitud homologación - valida endpoint")
    void actualizarEstadoSolicitudHomologacion() throws Exception {
        String body = "{"
                + "\"idSolicitud\": 1,"
                + "\"nuevoEstado\": \"ENVIADA\""
                + "}";

        mockMvc.perform(put("/api/solicitudes-homologacion/actualizarEstadoSolicitud")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body));
    }
}


