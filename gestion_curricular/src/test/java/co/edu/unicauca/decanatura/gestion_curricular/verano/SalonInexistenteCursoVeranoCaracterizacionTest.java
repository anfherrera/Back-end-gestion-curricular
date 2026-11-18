package co.edu.unicauca.decanatura.gestion_curricular.verano;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSalonesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarCursoOfertadoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocenteGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarMateriasIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSalonesIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarCursoOfertadoVeranoCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.CursosVeranoRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.Correcciones;

/**
 * Fija la respuesta HTTP de crear y editar un curso de verano con un {@code id_salon} inexistente, con el
 * controlador y el caso de uso reales (BUG-39). El gateway de salones simulado no conoce ningún salón. Los
 * cuerpos de error se arman con {@code Map.of}, así que {@code claves} se ordena.
 */
class SalonInexistenteCursoVeranoCaracterizacionTest {
    private static final String BASE = "/api/cursos-intersemestrales/cursos-verano";

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void respondeAlSalonInexistente() throws Exception {
        var casoDeUso = new GestionarCursoOfertadoVeranoCUAdapter(mock(GestionarCursoOfertadoVeranoGatewayIntPort.class),
            mock(GestionarSolicitudGatewayIntPort.class), mock(GestionarUsuarioGatewayIntPort.class),
            mock(GestionarMateriasIntPort.class), mock(GestionarDocenteGatewayIntPort.class),
            mock(GestionarSalonesIntPort.class), mock(GestionarNotificacionCUIntPort.class));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new CursosVeranoRestController(casoDeUso,
                mock(GestionarSolicitudCursoVeranoCUIntPort.class), null, mock(GestionarSalonesCUIntPort.class), null))
            .setControllerAdvice(new RestApiExceptionHandler()).build();

        ObjectNode resultados = json.createObjectNode();
        registrar(resultados, "crear salon inexistente", mvc, post(BASE), "{\"id_materia\":7,\"id_docente\":8,"
            + "\"cupo_estimado\":30,\"fecha_inicio\":\"2025-07-01\",\"fecha_fin\":\"2025-08-15\","
            + "\"periodoAcademico\":\"2025-2\",\"id_salon\":6}");
        registrar(resultados, "editar salon inexistente", mvc, put(BASE + "/1"), "{\"id_salon\":6}");

        Path referencia = Path.of("src/test/resources/referencias/verano-endpoints/salon-inexistente.json");
        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(referencia)) {
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(resultados));
        }
        // BUG-39: salon-inexistente.json fija el 409 anterior; la corrección, el 400 del controlador
        assertThat(resultados).isEqualTo(Correcciones.aplicar(json.readTree(Files.readString(referencia)),
            "verano-endpoints/correcciones/salon-inexistente-bug39.json"));
    }

    private void registrar(ObjectNode resultados, String nombre, MockMvc mvc,
            MockHttpServletRequestBuilder peticion, String cuerpo)
            throws Exception {
        var respuesta = mvc.perform(peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo))
            .andReturn().getResponse();
        byte[] contenido = respuesta.getContentAsByteArray();
        JsonNode body = contenido.length == 0 ? json.nullNode() : json.readTree(contenido);
        ObjectNode resultado = resultados.putObject(nombre);
        resultado.put("status", respuesta.getStatus());
        resultado.set("body", body);
        List<String> claves = new ArrayList<>();
        body.fields().forEachRemaining(campo -> claves.add(campo.getKey() + ":" + campo.getValue().getNodeType()));
        claves.sort(null);
        ArrayNode lista = resultado.putArray("claves");
        claves.forEach(lista::add);
    }
}
