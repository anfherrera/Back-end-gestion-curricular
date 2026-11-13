package co.edu.unicauca.decanatura.gestion_curricular.pazysalvo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import co.edu.unicauca.decanatura.gestion_curricular.Security.Autorizacion;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.*;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudPazYSalvoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudPazYSalvoDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladorExcepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.SolicitudPazYSalvoRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.*;

/**
 * Fija cómo {@code crearSolicitud-PazYSalvo} interpreta el cuerpo (ARQ-9): forma del cuerpo,
 * coerción de cada valor escalar o anidado y los datos que llegan al caso de uso o al mapeo del DTO.
 * Complementa {@code crear-solicitud.json} (flujo real) y {@code errores-operaciones.json} (errores).
 */
class CrearPazYSalvoCuerpoCaracterizacionTest {
    private static final String RUTA = "/api/solicitudes-pazysalvo/crearSolicitud-PazYSalvo";
    private static final Path REFERENCIA = Path.of("src/test/resources/referencias/pazysalvo/crear-cuerpo.json");

    private final ObjectMapper json = Jackson2ObjectMapperBuilder.json().build();
    private final List<JsonNode> capturas = new ArrayList<>();

    @Test
    void conservaLaInterpretacionDelCuerpo() throws Exception {
        Map<String, JsonNode> r = new LinkedHashMap<>();
        MockMvc staff = mvc(true, 99);
        String[] cuerposStaff = {
            // Forma del cuerpo
            "\"texto\"", "5", "true", "[]", "[{\"idUsuario\":1}]", "{}",
            // idUsuario
            "{\"idUsuario\":12}", "{\"idUsuario\":\"12\"}", "{\"idUsuario\":\" 12 \"}", "{\"idUsuario\":null}",
            "{\"idUsuario\":\"\"}", "{\"idUsuario\":12.5}", "{\"idUsuario\":12.0}", "{\"idUsuario\":true}",
            "{\"idUsuario\":{\"a\":1}}", "{\"idUsuario\":[12]}", "{\"idUsuario\":\"1e3\"}",
            "{\"idUsuario\":2147483648}", "{\"idUsuario\":-3}",
            // fecha_solicitud
            "{\"idUsuario\":1,\"fecha_solicitud\":20250115}", "{\"idUsuario\":1,\"fecha_solicitud\":true}",
            "{\"idUsuario\":1,\"fecha_solicitud\":{\"x\":1}}", "{\"idUsuario\":1,\"fecha_solicitud\":\"2025-02-30\"}",
            "{\"idUsuario\":1,\"fecha_solicitud\":null}", "{\"idUsuario\":1,\"fecha_solicitud\":\" 2025-01-15 \"}",
            "{\"idUsuario\":1,\"fecha_solicitud\":\"2025-01-15\"}",
            // periodo_academico
            "{\"idUsuario\":1,\"periodo_academico\":2024}", "{\"idUsuario\":1,\"periodo_academico\":\"2024-1\"}",
            "{\"idUsuario\":1,\"periodo_academico\":[\"2024-1\"]}", "{\"idUsuario\":1,\"periodo_academico\":\"  \"}",
            "{\"idUsuario\":1,\"periodo_academico\":null}",
            // título y director
            "{\"idUsuario\":1,\"titulo_trabajo_grado\":123,\"director_trabajo_grado\":45.0}",
            "{\"idUsuario\":1,\"titulo_trabajo_grado\":true,\"director_trabajo_grado\":{\"a\":1}}",
            "{\"idUsuario\":1,\"titulo_trabajo_grado\":[\"a\",\"b\"],\"director_trabajo_grado\":\"  Dr. X \"}",
            "{\"idUsuario\":1,\"titulo_trabajo_grado\":\"  \",\"director_trabajo_grado\":null}",
            // Prioridad de idUsuario sobre el DTO y claves desconocidas
            "{\"idUsuario\":1,\"objUsuario\":{\"id_usuario\":2},\"nombre_solicitud\":\"X\",\"otra\":[1]}",
            // Forma DTO
            "{\"objUsuario\":{\"id_usuario\":\"12\"},\"nombre_solicitud\":5,\"fecha_registro_solicitud\":\"2025-03-01\"}",
            "{\"objUsuario\":{\"id_usuario\":12},\"nombre_solicitud\":\"X\",\"fecha_registro_solicitud\":1700000000000,"
                + "\"periodo_academico\":\" 2024-2 \",\"id_solicitud\":7,\"otra\":1}",
            "{\"objUsuario\":{\"id_usuario\":12},\"nombre_solicitud\":\"X\",\"fecha_registro_solicitud\":\"01/03/2025\"}",
            "{\"objUsuario\":\"12\",\"nombre_solicitud\":\"X\",\"fecha_registro_solicitud\":\"2025-03-01\"}",
            "{\"objUsuario\":null,\"nombre_solicitud\":\"X\"}",
            "{\"objUsuario\":{\"id_usuario\":12},\"nombre_solicitud\":\" \",\"fecha_registro_solicitud\":\"2025-03-01\"}",
            "{\"objUsuario\":{\"id_usuario\":12},\"nombre_solicitud\":\"X\",\"fecha_registro_solicitud\":\"2025-03-01\","
                + "\"periodo_academico\":2024}",
        };
        for (String cuerpo : cuerposStaff) {
            r.put("staff " + cuerpo, pedir(staff, cuerpo));
        }
        MockMvc estudiante = mvc(false, 99);
        for (String cuerpo : new String[]{"{}", "{\"x\":1}", "{\"idUsuario\":5}", "{\"idUsuario\":null}",
                "{\"objUsuario\":{\"id_usuario\":5},\"nombre_solicitud\":\"X\",\"fecha_registro_solicitud\":\"2025-03-01\"}",
                "\"texto\"", "[1]"}) {
            r.put("estudiante " + cuerpo, pedir(estudiante, cuerpo));
        }
        MockMvc sinCuenta = mvc(false, null);
        for (String cuerpo : new String[]{"{}", "{\"x\":1}", "[1]"}) {
            r.put("sin cuenta " + cuerpo, pedir(sinCuenta, cuerpo));
        }

        JsonNode actual = json.valueToTree(r);
        if (Boolean.getBoolean("grabarReferencia")) {
            assertThat(Files.exists(REFERENCIA)).isFalse();
            Files.writeString(REFERENCIA, json.writerWithDefaultPrettyPrinter().writeValueAsString(actual));
        }
        assertThat(actual).isEqualTo(json.readTree(Files.readString(REFERENCIA)));
    }

    private JsonNode pedir(MockMvc mvc, String cuerpo) throws Exception {
        capturas.clear();
        var respuesta = mvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andReturn().getResponse();
        ObjectNode nodo = json.createObjectNode();
        nodo.put("status", respuesta.getStatus());
        byte[] contenido = respuesta.getContentAsByteArray();
        nodo.set("body", contenido.length == 0 ? json.nullNode() : json.readTree(contenido));
        nodo.set("capturas", json.valueToTree(capturas));
        return nodo;
    }

    private MockMvc mvc(boolean esStaff, Integer idActual) {
        var cu = mock(GestionarSolicitudPazYSalvoCUIntPort.class);
        when(cu.guardar(any())).thenAnswer(i -> {
            capturas.add(describir(i.getArgument(0)));
            return i.getArgument(0);
        });
        var mapper = mock(SolicitudPazYSalvoMapperDominio.class);
        when(mapper.mappearDeSolicitudDTOPeticionASolicitud(any())).thenAnswer(i -> {
            SolicitudPazYSalvoDTOPeticion dto = i.getArgument(0);
            ObjectNode nodo = json.createObjectNode();
            nodo.set("dto", json.valueToTree(dto));
            capturas.add(nodo);
            return new SolicitudPazYSalvo();
        });
        when(mapper.mappearDeSolicitudARespuesta(any())).thenReturn(new SolicitudPazYSalvoDTORespuesta());
        var autorizacion = new Autorizacion(null, null, null, null) {
            @Override public boolean esStaff() { return esStaff; }
            @Override public Integer idUsuarioActual() { return idActual; }
        };
        var controller = new SolicitudPazYSalvoRestController(cu, mapper, mock(GestionarArchivosCUIntPort.class),
                mock(GestionarDocumentosCUIntPort.class), mock(DocumentosMapperDominio.class),
                mock(GenerarDocumentosCUIntPort.class), json, autorizacion);
        return MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(json))
                .setControllerAdvice(new RestApiExceptionHandler()).build();
    }

    private JsonNode describir(SolicitudPazYSalvo s) {
        ObjectNode nodo = json.createObjectNode();
        nodo.put("guardar.idUsuario", s.getObjUsuario() != null ? s.getObjUsuario().getId_usuario() : null);
        nodo.put("guardar.fecha", fecha(s.getFecha_registro_solicitud()));
        nodo.put("guardar.periodo", s.getPeriodo_academico());
        nodo.put("guardar.titulo", s.getTitulo_trabajo_grado());
        nodo.put("guardar.director", s.getDirector_trabajo_grado());
        nodo.put("guardar.nombre", s.getNombre_solicitud());
        return nodo;
    }

    private static String fecha(Date fecha) {
        if (fecha == null) {
            return null;
        }
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
        String texto = formato.format(fecha);
        return texto.equals(formato.format(new Date())) ? "HOY" : texto;
    }
}
