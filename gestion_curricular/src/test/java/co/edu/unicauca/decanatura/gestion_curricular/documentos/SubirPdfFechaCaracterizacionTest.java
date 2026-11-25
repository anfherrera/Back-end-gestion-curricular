package co.edu.unicauca.decanatura.gestion_curricular.documentos;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.configuracion.NombresJsonDominio;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.SoporteMvc;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocumentosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.ArchivosRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.Autorizacion;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.Correcciones;

/**
 * Fija el formato de {@code fechaSubida} de {@code POST /api/archivos} con el {@code ObjectMapper} por
 * defecto de MockMvc y con el que configura Spring Boot, el de la aplicación. La fecha es la del momento de la
 * petición: solo se sustituye por un marcador, que nombra su formato, si es de esta ejecución.
 */
class SubirPdfFechaCaracterizacionTest {
    private static final DateTimeFormatter ISO_UTC = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSxxx");

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void conservaElFormatoDeLaFechaDeSubida() throws Exception {
        var archivos = mock(GestionarArchivosCUIntPort.class);
        when(archivos.saveFile(any(), anyString(), anyString())).thenAnswer(i -> i.getArgument(1));
        var documentos = mock(GestionarDocumentosCUIntPort.class);
        when(documentos.registrarArchivoGuardado(any(), any(), any())).thenReturn(new Documento());
        var auth = new Autorizacion(null, null, null, null) {
            @Override public boolean esStaff() { return true; }
            @Override public Integer idUsuarioActual() { return 31; }
        };
        var controlador = new ArchivosRestController(archivos, documentos,
            mock(GestionarSolicitudCursoVeranoCUIntPort.class),
            mock(GestionarSolicitudCUIntPort.class), auth);
        MockMvc porDefecto = SoporteMvc.standaloneSetup(controlador)
            .setControllerAdvice(new RestApiExceptionHandler()).build();
        MockMvc deSpringBoot = SoporteMvc.standaloneSetup(controlador)
            .setControllerAdvice(new RestApiExceptionHandler())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(mapperDeSpringBoot())).build();

        ObjectNode resultados = json.createObjectNode();
        registrar(resultados, "por defecto", porDefecto);
        registrar(resultados, "spring boot", deSpringBoot);

        Path referencia = Path.of("src/test/resources/referencias/documentos/subir-pdf-fecha.json");
        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(referencia)) {
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(resultados));
        }
        // BUG-40: subir-pdf-fecha.json fija el Date.toString(); la corrección, el Date (milisegundos o ISO 8601)
        assertThat(resultados).isEqualTo(Correcciones.aplicar(json.readTree(Files.readString(referencia)),
            "documentos/correcciones/subir-pdf-fecha-bug40.json"));
    }

    private void registrar(ObjectNode resultados, String nombre, MockMvc mvc) throws Exception {
        var peticion = multipart("/api/archivos")
            .file(new MockMultipartFile("file", "comprobante.pdf", "application/pdf",
                "%PDF-1.4 contenido".getBytes(UTF_8)));
        long antes = System.currentTimeMillis();
        MockHttpServletResponse respuesta = mvc.perform(peticion).andReturn().getResponse();
        long despues = System.currentTimeMillis();
        JsonNode fecha = json.readTree(respuesta.getContentAsString(UTF_8)).path("fechaSubida");

        ObjectNode resultado = resultados.putObject(nombre);
        resultado.put("status", respuesta.getStatus());
        resultado.put("fechaTipo", fecha.getNodeType().toString());
        resultado.set("fechaSubida", marcar(fecha, antes, despues));
    }

    /** Marcador del formato si la fecha cae entre {@code antes} y {@code despues}; si no, el valor tal cual. */
    private JsonNode marcar(JsonNode fecha, long antes, long despues) {
        if (fecha.isNumber() && fecha.asLong() >= antes && fecha.asLong() <= despues) {
            return json.getNodeFactory().textNode("<milisegundos de ahora>");
        }
        if (fecha.isTextual()) {
            String texto = fecha.asText();
            for (long segundo = antes / 1000; segundo <= despues / 1000; segundo++) {
                if (texto.equals(new Date(segundo * 1000).toString())) {
                    return json.getNodeFactory().textNode("<Date.toString() de ahora>");
                }
            }
            try {
                OffsetDateTime iso = OffsetDateTime.parse(texto, ISO_UTC);
                long milis = iso.toInstant().toEpochMilli();
                if (iso.getOffset().getTotalSeconds() == 0 && milis >= antes && milis <= despues) {
                    return json.getNodeFactory().textNode("<ISO 8601 UTC de ahora>");
                }
            } catch (DateTimeParseException e) {
                // No es ISO 8601: se deja el valor tal cual para que la comparación lo muestre.
            }
        }
        return fecha;
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
