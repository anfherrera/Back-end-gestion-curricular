package co.edu.unicauca.decanatura.gestion_curricular.verano;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.configuracion.NombresJsonDominio;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.SoporteMvc;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.PreinscripcionesVeranoRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.CursosOfertadosMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudCursoDeVeranoPreinscripcionMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.Correcciones;

/**
 * Fija la {@code fecha} de {@code POST /solicitudes-curso-nuevo} con el {@code ObjectMapper} que configura Spring
 * Boot, el de la aplicación, y la respuesta cuando el caso de uso devuelve la solicitud sin fecha (BUG-32). El
 * {@code Date.toString()} depende de la zona horaria y del idioma de la JVM, por eso se sustituye por un marcador.
 */
class CursoNuevoFechaCaracterizacionTest {
    private static final long MARZO = 1710522000000L;
    private static final String RUTA = "/api/cursos-intersemestrales/solicitudes-curso-nuevo";

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void conservaLaFechaDeLaSolicitudDeCursoNuevo() throws Exception {
        var cu = mock(GestionarSolicitudCursoVeranoCUIntPort.class);
        when(cu.crearSolicitudCursoVeranoPreinscripcion(any())).thenAnswer(i -> {
            SolicitudCursoVeranoPreinscripcion recibida = i.getArgument(0);
            SolicitudCursoVeranoPreinscripcion guardada = new SolicitudCursoVeranoPreinscripcion();
            guardada.setIdSolicitud(50);
            if (!recibida.getObservacion().endsWith("sin fecha")) {
                guardada.setFechaRegistroSolicitud(new Date(MARZO));
            }
            return guardada;
        });
        var controlador = new PreinscripcionesVeranoRestController(cu, mock(CursosOfertadosMapperDominio.class),
            mock(SolicitudCursoDeVeranoPreinscripcionMapperDominio.class));
        MockMvc porDefecto = SoporteMvc.standaloneSetup(controlador)
            .setControllerAdvice(new RestApiExceptionHandler()).build();
        MockMvc deSpringBoot = SoporteMvc.standaloneSetup(controlador)
            .setControllerAdvice(new RestApiExceptionHandler())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(mapperDeSpringBoot())).build();

        ObjectNode resultados = json.createObjectNode();
        registrar(resultados, "sin fecha", porDefecto, "sin fecha");
        registrar(resultados, "spring boot", deSpringBoot, "Cálculo");
        registrar(resultados, "spring boot sin fecha", deSpringBoot, "sin fecha");

        Path referencia = Path.of("src/test/resources/referencias/verano-endpoints/curso-nuevo-fecha.json");
        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(referencia)) {
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(resultados));
        }
        // BUG-32: curso-nuevo-fecha.json fija el Date.toString() y el 500 sin fecha; la corrección, el Date
        assertThat(resultados).isEqualTo(Correcciones.aplicar(json.readTree(Files.readString(referencia)),
            "verano-endpoints/correcciones/curso-nuevo-fecha-bug32.json"));
    }

    private void registrar(ObjectNode resultados, String nombre, MockMvc mvc, String curso) throws Exception {
        String cuerpo = "{\"nombreCompleto\":\"Ana Ruiz\",\"codigo\":\"1046\",\"condicion\":\"Primera_Vez\","
            + "\"idUsuario\":7,\"curso\":\"" + curso + "\"}";
        MockHttpServletResponse respuesta = mvc.perform(post(RUTA).contentType(MediaType.APPLICATION_JSON)
            .content(cuerpo)).andReturn().getResponse();
        String contenido = respuesta.getContentAsString(UTF_8);
        JsonNode body = contenido.isEmpty() ? json.nullNode() : json.readTree(contenido);
        JsonNode fecha = body.path("fecha");
        if (fecha.isTextual() && fecha.asText().equals(new Date(MARZO).toString())) {
            ((ObjectNode) body).put("fecha", "<Date.toString()>");
        }
        ObjectNode resultado = resultados.putObject(nombre);
        resultado.put("status", respuesta.getStatus());
        resultado.set("body", body);
        resultado.put("fechaTipo", fecha.getNodeType().toString());
    }

    private static ObjectMapper mapperDeSpringBoot() {
        AtomicReference<ObjectMapper> mapper = new AtomicReference<>();
        // Como en la aplicación, con el módulo de nombres históricos del dominio (OPC-2)
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
            .withBean(com.fasterxml.jackson.databind.Module.class, NombresJsonDominio::modulo)
            .run(contexto -> mapper.set(contexto.getBean(ObjectMapper.class)));
        return mapper.get();
    }
}
