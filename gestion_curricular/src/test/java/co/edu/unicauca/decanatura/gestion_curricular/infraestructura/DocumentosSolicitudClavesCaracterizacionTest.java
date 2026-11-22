package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocumentosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.SoporteMvc;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudHomologacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudReingresoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.SolicitudHomologacionRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.SolicitudReingresoRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.Autorizacion;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.Correcciones;

/**
 * Fija oficios, validación de documentos requeridos y subida de resolución de Reingreso y Homologación
 * antes de tiparlos (ARQ-10). {@code claves} lista cada clave con su ruta y tipo JSON en el orden de
 * escritura, así que la comparación de listas también cubre el orden.
 */
class DocumentosSolicitudClavesCaracterizacionTest {
    private static final int INEXISTENTE = 404;
    private static final int SIN_DOCUMENTOS = 1;
    private static final int VACIA = 2;
    private static final int MEZCLA = 3;
    private static final int COMPLETA = 4;

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void conservaCuerposYOrdenDeClaves() throws Exception {
        var homologacion = mock(GestionarSolicitudHomologacionCUIntPort.class);
        when(homologacion.buscarPorId(anyInt())).thenAnswer(i -> solicitud(SolicitudHomologacion::new, i.getArgument(0)));
        var reingreso = mock(GestionarSolicitudReingresoCUIntPort.class);
        when(reingreso.obtenerSolicitudReingresoPorId(anyInt()))
            .thenAnswer(i -> solicitud(SolicitudReingreso::new, i.getArgument(0)));
        var documentos = mock(GestionarDocumentosCUIntPort.class);
        when(documentos.adjuntarASolicitud(any(), any(), anyString(), anyString(), anyBoolean())).thenAnswer(i -> {
            Documento guardado = new Documento();
            guardado.setIdDocumento(55);
            guardado.setNombre("resolucion_guardada.pdf");
            return guardado;
        });
        var auth = new Autorizacion(null, null, null, null);
        MockMvc mvc = SoporteMvc.standaloneSetup(
                new SolicitudHomologacionRestController(homologacion, null, null, null, documentos, auth, null),
                new SolicitudReingresoRestController(null, reingreso, null, null, auth, null))
            .setControllerAdvice(new RestApiExceptionHandler()).build();

        ObjectNode resultados = json.createObjectNode();
        int[] ids = {INEXISTENTE, SIN_DOCUMENTOS, VACIA, MEZCLA, COMPLETA};
        String[] bases = {"/api/solicitudes-homologacion", "/api/solicitudes-reingreso"};
        for (String base : bases) {
            for (String operacion : new String[] {"obtenerOficios", "validarDocumentosRequeridos"}) {
                for (int id : ids) {
                    String ruta = base + "/" + operacion + "/" + id;
                    registrar(resultados, ruta, mvc.perform(get(ruta)).andReturn().getResponse());
                }
            }
        }

        byte[] pdf = "%PDF-1.4 resolucion".getBytes(StandardCharsets.US_ASCII);
        Object[][] subidas = {
            {MEZCLA, new MockMultipartFile("file", "resolucion.pdf", "application/pdf", pdf)},
            {INEXISTENTE, new MockMultipartFile("file", "resolucion.pdf", "application/pdf", pdf)},
            {MEZCLA, new MockMultipartFile("file", "vacio.pdf", "application/pdf", new byte[0])},
            {MEZCLA, new MockMultipartFile("file", "resolucion.docx", "application/pdf", pdf)},
            {MEZCLA, new MockMultipartFile("file", "falso.pdf", "application/pdf", "texto".getBytes())},
            {MEZCLA, new MockMultipartFile("file", "grande.pdf", "application/pdf", new byte[11 * 1024 * 1024])},
        };
        for (Object[] subida : subidas) {
            MockMultipartFile archivo = (MockMultipartFile) subida[1];
            String ruta = "/api/solicitudes-homologacion/" + subida[0] + "/subir-resolucion";
            RequestBuilder peticion = multipart(ruta).file(archivo);
            registrar(resultados, ruta + " " + archivo.getOriginalFilename() + " (" + archivo.getSize() + ")",
                mvc.perform(peticion).andReturn().getResponse());
        }

        Path referencia = Path.of("src/test/resources/referencias/documentos/documentos-solicitud-claves.json");
        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(referencia)) {
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(resultados));
        }
        // BUG-30: la carta de solicitud del estudiante ya no sale entre los oficios de Reingreso
        assertThat(resultados).isEqualTo(Correcciones.aplicar(json.readTree(Files.readString(referencia)),
            "documentos/correcciones/documentos-solicitud-claves-bug30.json"));
    }

    private void registrar(ObjectNode resultados, String nombre, MockHttpServletResponse respuesta) throws Exception {
        byte[] cuerpo = respuesta.getContentAsByteArray();
        JsonNode body = cuerpo.length == 0 ? json.nullNode() : json.readTree(cuerpo);
        ObjectNode resultado = resultados.putObject(nombre);
        resultado.put("status", respuesta.getStatus());
        resultado.set("body", body);
        ArrayNode claves = resultado.putArray("claves");
        recorrer(body, "", claves);
    }

    private static void recorrer(JsonNode nodo, String ruta, ArrayNode claves) {
        if (nodo.isObject()) {
            nodo.fields().forEachRemaining(campo -> {
                String hija = ruta + "." + campo.getKey();
                claves.add(hija + ":" + campo.getValue().getNodeType());
                recorrer(campo.getValue(), hija, claves);
            });
        } else if (nodo.isArray()) {
            for (int i = 0; i < nodo.size(); i++) {
                recorrer(nodo.get(i), ruta + "[" + i + "]", claves);
            }
        }
    }

    private static <T extends Solicitud> T solicitud(Supplier<T> nueva, int id) {
        if (id == INEXISTENTE) {
            return null;
        }
        T solicitud = nueva.get();
        solicitud.setIdSolicitud(id);
        solicitud.setDocumentos(switch (id) {
            case SIN_DOCUMENTOS -> null;
            case VACIA -> new ArrayList<>();
            case MEZCLA -> new ArrayList<>(List.of(
                documento("oficio_reingreso.pdf", "ruta/oficio_reingreso.pdf", null),
                documento("Resolucion_Final.PDF", null, null),
                documento("aprobacion.docx", "ruta/aprobacion.docx", null),
                documento("marcado.docx", "ruta/marcado.docx", "[OFICIO_HOMOLOGACION] Archivo subido por secretaría"),
                documento("solicitud_homologacion_resolucion.pdf", "ruta/s.pdf", null),
                documento(null, "ruta/sin_nombre", null),
                documento("certificado_notas.pdf", "ruta/c.pdf", null),
                documento("carnet_estudiantil.pdf", "ruta/carnet.pdf", null),
                documento("Programa_Academico.pdf", "ruta/p.pdf", null)));
            default -> new ArrayList<>(List.of(
                documento("carta_solicitud_reingreso.pdf", "r/1", null),
                documento("certificado_notas.pdf", "r/2", null),
                documento("documento_identidad.pdf", "r/3", null),
                documento("justificacion_ausencia.pdf", "r/4", null),
                documento("formulario_homologacion.pdf", "r/5", null),
                documento("programa_academico.pdf", "r/6", null)));
        });
        return solicitud;
    }

    private static Documento documento(String nombre, String ruta, String comentario) {
        Documento documento = new Documento();
        documento.setNombre(nombre);
        documento.setRutaDocumento(ruta);
        documento.setComentario(comentario);
        return documento;
    }
}
