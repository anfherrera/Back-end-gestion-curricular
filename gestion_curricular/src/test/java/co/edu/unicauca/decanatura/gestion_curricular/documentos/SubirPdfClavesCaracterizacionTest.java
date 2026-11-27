package co.edu.unicauca.decanatura.gestion_curricular.documentos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.SoporteMvc;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocumentosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.ArchivosRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.Autorizacion;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.Correcciones;

/**
 * Fija el orden de las claves y el tipo JSON de cada valor de {@code POST /api/archivos}
 * antes de tipar su respuesta (ARQ-10). Los valores ya los fija {@link SubirPdfArchivosCaracterizacionTest}.
 */
class SubirPdfClavesCaracterizacionTest {
    private final ObjectMapper json = new ObjectMapper();

    @Test
    void conservaElOrdenYElTipoDeLasClaves() throws Exception {
        Map<String, Object> resultados = new LinkedHashMap<>();
        resultados.put("sin parametros", claves(new String[]{}, true, true, "pdf"));
        resultados.put("inscripcion", claves(new String[]{"inscripcionId", "7"}, true, true, "pdf"));
        resultados.put("solicitud", claves(new String[]{"solicitudId", "5"}, true, true, "pdf"));
        resultados.put("tipo sin id", claves(new String[]{"tipoSolicitud", "reingreso"}, true, true, "pdf"));
        resultados.put("todos", claves(new String[]{"inscripcionId", "7", "solicitudId", "5",
            "tipoSolicitud", "reingreso"}, true, true, "pdf"));
        resultados.put("id invalido", claves(new String[]{"solicitudId", "abc"}, false, true, "pdf"));
        resultados.put("no dueno", claves(new String[]{"solicitudId", "5"}, false, false, "pdf"));
        resultados.put("no pdf", claves(new String[]{}, true, true, "noPdf"));

        JsonNode actual = json.valueToTree(resultados);
        Path referencia = Path.of("src/test/resources/referencias/documentos/subir-pdf-claves.json");
        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(referencia)) {
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(actual));
        }
        // BUG-40: fechaSubida es un Date, que el ObjectMapper por defecto de MockMvc escribe en milisegundos
        assertThat(actual).isEqualTo(Correcciones.aplicar(json.readTree(Files.readString(referencia)),
            "documentos/correcciones/subir-pdf-claves-bug40.json"));
    }

    /** Lista "clave:tipo" en el orden de la respuesta, más el estado HTTP. */
    private List<String> claves(String[] params, boolean staff, boolean dueno, String archivo) throws Exception {
        var archivos = mock(GestionarArchivosCUIntPort.class);
        when(archivos.saveFile(any(), anyString(), anyString())).thenAnswer(i -> i.getArgument(1));
        when(archivos.saveFile(any(), anyString(), anyString(), anyString(), any())).thenAnswer(i -> i.getArgument(1));
        var documentos = mock(GestionarDocumentosCUIntPort.class);
        when(documentos.registrarArchivoGuardado(any(), any(), any())).thenReturn(new Documento());
        var verano = mock(GestionarSolicitudCursoVeranoCUIntPort.class);
        when(verano.buscarPorIdInscripcion(anyInt())).thenReturn(null);
        // BUG-20: toda solicitud pedida existe, como la Solicitud con solo el id que se asociaba antes
        var solicitudes = mock(GestionarSolicitudCUIntPort.class);
        when(solicitudes.obtenerSolicitudPorId(anyInt())).thenAnswer(i -> {
            var s = new Solicitud();
            s.setIdSolicitud(i.getArgument(0));
            return s;
        });
        var auth = new Autorizacion(null, null, null, null) {
            @Override public boolean esStaff() { return staff; }
            @Override public boolean esDuenoSolicitud(Integer id) { return dueno; }
            @Override public Integer idUsuarioActual() { return 31; }
        };
        var mvc = SoporteMvc.standaloneSetup(new ArchivosRestController(archivos, documentos,
            verano, solicitudes, auth))
            .setControllerAdvice(new RestApiExceptionHandler()).build();

        byte[] contenido = "noPdf".equals(archivo)
            ? "no es pdf".getBytes(StandardCharsets.UTF_8)
            : "%PDF-1.4 contenido".getBytes(StandardCharsets.UTF_8);
        var peticion = multipart("/api/archivos")
            .file(new MockMultipartFile("file", "comprobante.pdf", "application/pdf", contenido));
        for (int i = 0; i < params.length; i += 2) {
            peticion.param(params[i], params[i + 1]);
        }
        var respuesta = mvc.perform(peticion).andReturn().getResponse();
        JsonNode cuerpo = json.readTree(respuesta.getContentAsByteArray());
        List<String> claves = new ArrayList<>();
        claves.add("status:" + respuesta.getStatus());
        cuerpo.properties().forEach(campo -> claves.add(campo.getKey() + ":" + campo.getValue().getNodeType()));
        return claves;
    }
}
