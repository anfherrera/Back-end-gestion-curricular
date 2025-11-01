package co.edu.unicauca.decanatura.gestion_curricular.reingreso;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Integración - Solicitudes Reingreso")
public class ReingresoIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Listar solicitudes reingreso - general")
    void listarSolicitudesReingreso() throws Exception {
        mockMvc.perform(get("/api/solicitudes-reingreso/listarSolicitudes-Reingreso"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Crear solicitud reingreso - valida endpoint")
    void crearSolicitudReingreso() throws Exception {
        String json = "{"
                + "\"id_usuario\": 1,"
                + "\"motivo\": \"Reingreso por cambio de situación\","
                + "\"periodo\": \"2025-1\""
                + "}";

        mockMvc.perform(post("/api/solicitudes-reingreso/crearSolicitud-Reingreso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json));
    }

    @Test
    @DisplayName("Actualizar estado solicitud reingreso - valida endpoint")
    void actualizarEstadoSolicitudReingreso() throws Exception {
        String body = "{"
                + "\"idSolicitud\": 1,"
                + "\"nuevoEstado\": \"ENVIADA\""
                + "}";

        mockMvc.perform(put("/api/solicitudes-reingreso/actualizarEstadoSolicitud-Reingreso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body));
    }

    @Test
    @DisplayName("Subir archivo PDF a solicitud reingreso - valida endpoint")
    void subirArchivoReingreso() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "prueba.pdf",
                "application/pdf",
                "contenido".getBytes()
        );

        mockMvc.perform(multipart("/api/solicitudes-reingreso/1/subir-archivo").file(file));
    }
}


