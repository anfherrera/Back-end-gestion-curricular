package co.edu.unicauca.decanatura.gestion_curricular.pazysalvo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;

/**
 * Utilidades para las pruebas de Paz y Salvo. Todo se hace a través de la API para respetar
 * las reglas del sistema (flujo de estados por rol y una sola solicitud en trámite por estudiante).
 */
final class SoportePazYSalvo {

    private static final String BASE = "/api/solicitudes-pazysalvo";

    private SoportePazYSalvo() {
    }

    /** Crea una solicitud como estudiante y devuelve su id. */
    static int crearSolicitud(MockMvc mockMvc, ObjectMapper objectMapper, String tokenEstudiante) throws Exception {
        MvcResult resultado = mockMvc.perform(post(BASE + "/crearSolicitud-PazYSalvo")
                        .header("Authorization", tokenEstudiante)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fecha_solicitud\": \"2025-01-15\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(resultado.getResponse().getContentAsString());
        return json.get("id_solicitud").asInt();
    }

    /** Cambia el estado con el token indicado y devuelve el código HTTP. */
    static int cambiarEstado(MockMvc mockMvc, String token, int idSolicitud, String nuevoEstado) throws Exception {
        return mockMvc.perform(put(BASE + "/cambiarEstadoSolicitud/" + idSolicitud)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nuevoEstado\": \"" + nuevoEstado + "\"}"))
                .andReturn().getResponse().getStatus();
    }

    /**
     * Rechaza las solicitudes en trámite del estudiante para que cada prueba empiece limpia
     * (el sistema solo permite una solicitud en trámite por estudiante). Cada solicitud se
     * rechaza con el rol que corresponde a su paso; los demás roles reciben 400/403 y se ignoran.
     */
    static void cerrarSolicitudesEnTramite(MockMvc mockMvc, ObjectMapper objectMapper, String tokenEstudiante,
                                           String... tokensStaff) throws Exception {
        MvcResult resultado = mockMvc.perform(get(BASE + "/listarSolicitud-PazYSalvo/porRol")
                        .param("rol", "ESTUDIANTE")
                        .header("Authorization", tokenEstudiante))
                .andReturn();
        if (resultado.getResponse().getStatus() != 200) {
            return;
        }
        for (JsonNode solicitud : objectMapper.readTree(resultado.getResponse().getContentAsString())) {
            int id = solicitud.get("id_solicitud").asInt();
            for (String token : tokensStaff) {
                if (cambiarEstado(mockMvc, token, id, "RECHAZADA") / 100 == 2) {
                    break;
                }
            }
        }
    }

    /** Id del usuario autenticado según el correo. */
    static int idUsuario(co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt repo,
                         String correo) {
        return repo.buscarPorCorreo(correo).orElseThrow().getId_usuario();
    }

    /**
     * Un id que no pertenece al estudiante: ni es su id de usuario ni el de una solicitud suya.
     * El endpoint /listarSolicitud-PazYSalvo/{id} interpreta el número como id de solicitud o de
     * usuario, y las demás pruebas crean solicitudes a nombre del estudiante, así que el id del
     * coordinador podría coincidir con una solicitud propia del estudiante.
     */
    static int idAjeno(SolicitudRepositoryInt solicitudes, int idEstudiante, int candidato) {
        int id = candidato;
        while (id == idEstudiante || esSolicitudDe(solicitudes, id, idEstudiante)) {
            id++;
        }
        return id;
    }

    private static boolean esSolicitudDe(SolicitudRepositoryInt solicitudes, int idSolicitud, int idUsuario) {
        return solicitudes.findById(idSolicitud)
                .map(s -> s.getObjUsuario() != null && s.getObjUsuario().getId_usuario() == idUsuario)
                .orElse(false);
    }
}
