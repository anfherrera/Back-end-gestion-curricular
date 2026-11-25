package co.edu.unicauca.decanatura.gestion_curricular.documentos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
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
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.configuracion.NombresJsonDominio;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.SoporteMvc;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarDocumentosCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.ArchivosRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.Autorizacion;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.Correcciones;

/**
 * Caracterización de {@code POST /api/archivos} antes de llevar su registro al caso de uso (DUP-5).
 * Fija estado, cuerpo, llamadas en orden (guardado, consultas, autorización, registro) y el documento registrado.
 */
class SubirPdfArchivosCaracterizacionTest {

    private record Caso(String nombre, Map<String, String> params, boolean staff, boolean dueno,
                        String registro, String archivo, boolean guardadoFalla) {}

    private static Caso caso(String nombre, Map<String, String> params) {
        return new Caso(nombre, params, true, true, "ok", "pdf", false);
    }

    private static Caso caso(String nombre, Map<String, String> params, boolean staff, boolean dueno) {
        return new Caso(nombre, params, staff, dueno, "ok", "pdf", false);
    }

    private static Map<String, String> p(String... kv) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put(kv[i], kv[i + 1]);
        return m;
    }

    @Test
    void conservaNombresCarpetasAsociacionYPoliticaDeErrores() throws Exception {
        List<Caso> casos = List.of(
            caso("sin parametros, staff", p()),
            caso("sin parametros, estudiante", p(), false, false),
            caso("inscripcion con nombre", p("inscripcionId", "7")),
            caso("inscripcion con nombre en blanco", p("inscripcionId", "8")),
            caso("inscripcion sin usuario", p("inscripcionId", "9")),
            caso("inscripcion inexistente", p("inscripcionId", "10")),
            caso("inscripcion con error", p("inscripcionId", "11")),
            caso("inscripcion con espacios", p("inscripcionId", " 7 ")),
            caso("inscripcion no numerica", p("inscripcionId", "abc")),
            caso("inscripcion en blanco", p("inscripcionId", "  ")),
            caso("inscripcion con tipo", p("inscripcionId", "7", "tipoSolicitud", "homologacion")),
            caso("solicitud con tipo", p("solicitudId", "5", "tipoSolicitud", "reingreso")),
            caso("solicitud no numerica con tipo", p("solicitudId", "x5", "tipoSolicitud", "reingreso")),
            caso("alias idSolicitud que es inscripcion", p("idSolicitud", "7")),
            caso("solicitud inexistente como inscripcion", p("solicitudId", "10")),
            caso("solicitud con error al buscar inscripcion", p("solicitudId", "11")),
            caso("solicitud en blanco usa alias", p("solicitudId", " ", "idSolicitud", "12")),
            caso("inscripcion tiene prioridad sobre solicitud", p("inscripcionId", "7", "solicitudId", "5"), false, true),
            caso("estudiante no dueno", p("solicitudId", "5"), false, false),
            caso("estudiante con id invalido", p("solicitudId", "abc"), false, true),
            caso("estudiante con id con espacios y tipo", p("solicitudId", " 5 ", "tipoSolicitud", "x"), false, true),
            new Caso("registro devuelve null", p("solicitudId", "5"), true, true, "null", "pdf", false),
            new Caso("registro lanza error", p("solicitudId", "5"), true, true, "error", "pdf", false),
            new Caso("usuario actual lanza error", p(), true, true, "usuarioError", "pdf", false),
            new Caso("archivo vacio", p("solicitudId", "5"), true, true, "ok", "vacio", false),
            new Caso("archivo no pdf", p("solicitudId", "5"), true, true, "ok", "noPdf", false),
            new Caso("guardado falla", p("inscripcionId", "7"), true, true, "ok", "pdf", true)
        );

        // Vuelca objetos del dominio: con sus nombres JSON históricos (OPC-2)
        ObjectMapper json = new ObjectMapper().registerModule(NombresJsonDominio.modulo());
        Map<String, Object> resultados = new LinkedHashMap<>();
        for (Caso c : casos) {
            resultados.put(c.nombre(), ejecutar(c, json));
        }

        JsonNode actual = json.valueToTree(resultados);
        Path referencia = Path.of("src/test/resources/referencias/documentos/subir-pdf-archivos.json");
        if (Boolean.getBoolean("grabarReferencia")) {
            assertThat(Files.exists(referencia)).isFalse();
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(actual));
        }
        // BUG-20: el id se recorta y debe ser de una solicitud existente; se asocia al destino real
        // EST-7, paso 3: la ruta pasa a POST /api/archivos y cambia la url del error
        assertThat(actual).isEqualTo(Correcciones.aplicar(Correcciones.aplicar(json.readTree(Files.readString(referencia)),
            "documentos/correcciones/subir-pdf-archivos-bug20.json"), "documentos/correcciones/subir-pdf-archivos-est7.json"));
    }

    private JsonNode ejecutar(Caso c, ObjectMapper json) throws Exception {
        List<String> eventos = new ArrayList<>();
        List<Documento> guardados = new ArrayList<>();

        var archivos = mock(GestionarArchivosCUIntPort.class);
        var gateway = mock(GestionarDocumentosGatewayIntPort.class);
        var documentos = new GestionarDocumentosCUAdapter(gateway, archivos);
        var verano = mock(GestionarSolicitudCursoVeranoCUIntPort.class);

        when(archivos.saveFile(any(), anyString(), anyString())).thenAnswer(i -> {
            eventos.add("guardar(" + i.getArgument(1) + "," + i.getArgument(2) + ")");
            if (c.guardadoFalla()) throw new IllegalStateException("disco lleno");
            return i.getArgument(1);
        });
        when(archivos.saveFile(any(), anyString(), anyString(), anyString(), any())).thenAnswer(i -> {
            eventos.add("guardar(" + i.getArgument(1) + "," + i.getArgument(2) + "," + i.getArgument(3) + "," + i.getArgument(4) + ")");
            if (c.guardadoFalla()) throw new IllegalStateException("disco lleno");
            return i.getArgument(3) + "/solicitud_" + i.getArgument(4) + "/" + i.getArgument(1);
        });
        when(verano.buscarPorIdInscripcion(anyInt())).thenAnswer(i -> {
            int id = i.getArgument(0);
            eventos.add("inscripcion(" + id + ")");
            return switch (id) {
                case 7 -> inscripcion(70, "José Pérez-Ñ");
                case 8 -> inscripcion(80, "   ");
                case 9 -> { var s = new SolicitudCursoVeranoInscripcion(); s.setIdSolicitud(90); yield s; }
                case 11 -> throw new IllegalStateException("fallo consulta");
                default -> null;
            };
        });
        when(gateway.crearDocumento(any())).thenAnswer(i -> {
            eventos.add("registro");
            Documento d = i.getArgument(0);
            guardados.add(d);
            if ("error".equals(c.registro())) throw new IllegalStateException("fallo BD");
            if ("null".equals(c.registro())) return null;
            d.setIdDocumento(11);
            return d;
        });

        var auth = new Autorizacion(null, null, null, null) {
            @Override public boolean esStaff() { eventos.add("esStaff"); return c.staff(); }
            @Override public boolean esDuenoSolicitud(Integer id) { eventos.add("esDueno(" + id + ")"); return c.dueno(); }
            @Override public Integer idUsuarioActual() {
                eventos.add("usuarioActual");
                if ("usuarioError".equals(c.registro())) throw new IllegalStateException("sin token");
                return 31;
            }
        };

        // BUG-20: las solicitudes 5, 11 y 12 existen (de otro tipo); la 10 no existe
        var solicitudes = mock(GestionarSolicitudCUIntPort.class);
        when(solicitudes.obtenerSolicitudPorId(anyInt())).thenAnswer(i -> {
            int id = i.getArgument(0);
            eventos.add("solicitud(" + id + ")");
            if (id != 5 && id != 11 && id != 12) return null;
            var s = new Solicitud();
            s.setIdSolicitud(id);
            return s;
        });

        var controlador = new ArchivosRestController(archivos, documentos,
            verano, solicitudes, auth);
        MockMvc mvc = SoporteMvc.standaloneSetup(controlador)
            .setControllerAdvice(new RestApiExceptionHandler()).build();

        byte[] contenido = switch (c.archivo()) {
            case "vacio" -> new byte[0];
            case "noPdf" -> "no es pdf".getBytes(StandardCharsets.UTF_8);
            default -> "%PDF-1.4 contenido".getBytes(StandardCharsets.UTF_8);
        };
        var request = multipart("/api/archivos")
            .file(new MockMultipartFile("file", "comprobante.pago.pdf", "application/pdf", contenido));
        c.params().forEach(request::param);

        var respuesta = mvc.perform(request).andReturn().getResponse();
        byte[] cuerpo = respuesta.getContentAsByteArray();
        JsonNode body = cuerpo.length == 0 ? json.nullNode() : json.readTree(cuerpo);

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("status", respuesta.getStatus());
        snapshot.put("body", body);
        snapshot.put("eventos", eventos);
        snapshot.put("documentos", guardados);
        JsonNode nodo = json.valueToTree(snapshot);
        normalizar(nodo);
        return nodo;
    }

    private static SolicitudCursoVeranoInscripcion inscripcion(int idSolicitud, String nombre) {
        var usuario = new Usuario();
        usuario.setIdUsuario(40);
        usuario.setNombreCompleto(nombre);
        var s = new SolicitudCursoVeranoInscripcion();
        s.setIdSolicitud(idSolicitud);
        s.setObjUsuario(usuario);
        return s;
    }

    private static void normalizar(JsonNode nodo) {
        if (nodo.isObject()) {
            ObjectNode objeto = (ObjectNode) nodo;
            if (objeto.has("fechaSubida")) objeto.put("fechaSubida", "<ahora>");
            if (objeto.has("fechaSubida") && objeto.has("id")) objeto.put("id", "<milisegundos>");
            if (objeto.hasNonNull("fecha_documento")) objeto.put("fecha_documento", "<ahora>");
            if (objeto.has("timestamp")) objeto.put("timestamp", "<ahora>");
            if (objeto.hasNonNull("fecha_registro_solicitud")) objeto.put("fecha_registro_solicitud", "<ahora>");
        }
        nodo.elements().forEachRemaining(SubirPdfArchivosCaracterizacionTest::normalizar);
    }
}
