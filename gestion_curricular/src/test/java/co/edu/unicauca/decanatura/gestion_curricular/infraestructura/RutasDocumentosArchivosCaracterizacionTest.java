package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mapstruct.factory.Mappers;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GenerarDocumentosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocumentosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.PlantillaDocumento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.ArchivosRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.DocumentoRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.GeneradorDocumentosRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.DocumentosMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.Autorizacion;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.SoporteMvc;

/**
 * Caracterización previa a la migración de rutas de EST-7 (paso 3): documentos, generador de documentos y
 * archivos. Cada operación se pide por su ruta antigua y por la nueva, con los casos de uso simulados, el mapper
 * real y la seguridad de método inactiva (la fija {@code AutorizacionPorRolTest}). Además del estado, el tipo y el
 * cuerpo, cada caso guarda las cabeceras de descarga y las llamadas al caso de uso que cambian datos. El id y la
 * fecha de un PDF subido dependen del reloj y se sustituyen por marcadores.
 */
class RutasDocumentosArchivosCaracterizacionTest {

    private static final byte[] PDF = "%PDF-1.4 contenido fijo".getBytes(StandardCharsets.UTF_8);

    private final ObjectMapper json = new ObjectMapper();
    private final ArrayNode llamadas = json.createArrayNode();
    private boolean staff = true;

    @TempDir Path temporal;

    @Test
    void documentosGeneradorYArchivos() throws Exception {
        Files.write(temporal.resolve("existe.pdf"), PDF);
        Files.write(temporal.resolve("oficio reingreso&1 á.pdf"), PDF);

        var documentos = mock(GestionarDocumentosCUIntPort.class);
        var documento = new Documento();
        documento.setIdDocumento(7);
        documento.setNombre("oficio á +.pdf");
        documento.setRutaDocumento("carpeta/oficio.pdf");
        documento.setComentario("previo");
        documento.setEsValido(true);
        when(documentos.buscarDocumentoId(7)).thenReturn(documento);
        doAnswer(i -> {
            ObjectNode llamada = llamadas.addObject();
            llamada.put("añadirComentario", i.getArgument(0) == null ? null : i.<Integer>getArgument(0).toString());
            llamada.put("comentario", i.<String>getArgument(1));
            llamada.put("usuario", i.<String>getArgument(2));
            llamada.put("rol", i.<String>getArgument(3));
            return null;
        }).when(documentos).añadirComentario(any(), any(), any(), any());
        doAnswer(i -> {
            ObjectNode llamada = llamadas.addObject();
            llamada.put("registrarArchivoGuardado", i.<String>getArgument(0));
            llamada.put("solicitud", i.getArgument(1) == null ? null : "id " + ((co.edu.unicauca.decanatura
                    .gestion_curricular.dominio.modelos.Solicitud) i.getArgument(1)).getIdSolicitud());
            llamada.put("usuario", i.getArgument(2) == null ? null : i.getArgument(2).toString());
            return new Documento();
        }).when(documentos).registrarArchivoGuardado(any(), any(), any());

        var archivos = mock(GestionarArchivosCUIntPort.class);
        when(archivos.getFileByPath("carpeta/oficio.pdf")).thenReturn(PDF);
        when(archivos.getFileAsPath(anyString())).thenAnswer(i -> temporal.resolve(
                i.<String>getArgument(0).replace("carpeta/", "")));
        when(archivos.getFileByPathAsPath(anyString())).thenAnswer(i -> temporal.resolve(
                i.<String>getArgument(0).replace("carpeta/", "")));
        when(archivos.saveFile(any(), anyString(), eq("pdf"))).thenAnswer(i -> i.<String>getArgument(1));
        when(archivos.saveFile(any(), anyString(), eq("pdf"), anyString(), anyInt()))
                .thenAnswer(i -> i.getArgument(3) + "/" + i.getArgument(4) + "/" + i.getArgument(1));

        var generador = mock(GenerarDocumentosCUIntPort.class);
        when(generador.generarDocumento(anyInt(), any(), any(), any()))
                .thenReturn("docx fijo".getBytes(StandardCharsets.UTF_8));
        when(generador.getTemplates("reingreso")).thenReturn(List.of(new PlantillaDocumento("RESOLUCION_REINGRESO",
                "Resolución de reingreso", "Plantilla", List.of("numeroDocumento"), List.of("observaciones"))));
        when(generador.getTemplates("desconocido")).thenReturn(List.of());

        var verano = mock(GestionarSolicitudCursoVeranoCUIntPort.class);
        var inscripcion = new SolicitudCursoVeranoInscripcion();
        inscripcion.setIdSolicitud(7);
        var comprobante = new Documento();
        comprobante.setNombre("existe.pdf");
        comprobante.setRutaDocumento("carpeta/existe.pdf");
        inscripcion.setDocumentos(List.of(comprobante));
        when(verano.buscarPorIdInscripcion(7)).thenReturn(inscripcion);
        var sinDocumentos = new SolicitudCursoVeranoInscripcion();
        sinDocumentos.setIdSolicitud(9);
        sinDocumentos.setDocumentos(List.of());
        when(verano.buscarPorIdInscripcion(9)).thenReturn(sinDocumentos);

        var autorizacion = new Autorizacion(null, null, null, null) {
            @Override public boolean esStaff() { return staff; }
            @Override public boolean esDuenoSolicitud(Integer idSolicitud) { return false; }
            @Override public Optional<String> rutaArchivoVisible(String nombre) {
                return nombre.startsWith("ajeno") ? Optional.empty() : Optional.of("carpeta/" + nombre);
            }
            @Override public Usuario usuarioActual() {
                var usuario = new Usuario();
                usuario.setIdUsuario(5);
                usuario.setNombreCompleto("Funcionaria de prueba");
                var rol = new Rol();
                rol.setNombre("Funcionario");
                usuario.setObjRol(rol);
                return usuario;
            }
        };

        MockMvc mvc = SoporteMvc.standaloneSetup(
                new DocumentoRestController(documentos, Mappers.getMapper(DocumentosMapperDominio.class), archivos,
                        autorizacion),
                new GeneradorDocumentosRestController(generador),
                new ArchivosRestController(archivos, documentos,
                        verano, mock(GestionarSolicitudCUIntPort.class), autorizacion))
                .setControllerAdvice(new RestApiExceptionHandler()).build();

        ObjectNode casos = json.createObjectNode();

        // DocumentoRestController
        String d = "/api/documentos";
        for (String id : List.of("7", "8", "0", "abc")) {
            caso(casos, mvc, "GET " + d + "/buscarPorId/" + id, get(d + "/buscarPorId/" + id));
            caso(casos, mvc, "GET " + d + "/" + id, get(d + "/" + id));
            caso(casos, mvc, "GET " + d + "/" + id + "/descargar", get(d + "/" + id + "/descargar"));
            caso(casos, mvc, "GET " + d + "/" + id + "/archivo", get(d + "/" + id + "/archivo"));
        }
        for (String cuerpo : List.of("{\"idDocumento\":7,\"comentario\":\"hola\"}", "{\"comentario\":\"hola\"}",
                "{\"idDocumento\":8,\"comentario\":\"hola\"}", "{")) {
            caso(casos, mvc, "PUT " + d + "/agregarComentario " + cuerpo, put(d + "/agregarComentario")
                    .contentType(MediaType.APPLICATION_JSON).content(cuerpo));
        }
        for (String id : List.of("7", "8", "abc")) {
            for (String cuerpo : List.of("{\"comentario\":\"hola\"}", "{\"idDocumento\":99,\"comentario\":\"hola\"}",
                    "{")) {
                caso(casos, mvc, "PUT " + d + "/" + id + "/comentarios " + cuerpo, put(d + "/" + id + "/comentarios")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo));
            }
        }

        // GeneradorDocumentosRestController
        String cuerpoGenerar = "{\"idSolicitud\":7,\"tipoDocumento\":\"OFICIO_HOMOLOGACION\","
                + "\"datosDocumento\":{\"numeroDocumento\":\"004\"},\"datosSolicitud\":{\"nombreEstudiante\":\"Ana Pérez\"}}";
        for (String ruta : List.of(d + "/generar", "/api/documentos-generados")) {
            for (String cuerpo : List.of(cuerpoGenerar, "{")) {
                caso(casos, mvc, "POST " + ruta + " " + cuerpo, post(ruta)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo));
            }
        }
        for (String proceso : List.of("reingreso", "desconocido")) {
            caso(casos, mvc, "GET " + d + "/templates/" + proceso, get(d + "/templates/" + proceso));
            caso(casos, mvc, "GET /api/documentos-generados/plantillas/" + proceso,
                    get("/api/documentos-generados/plantillas/" + proceso));
        }

        // ArchivosRestController: subida
        String a = "/api/archivos";
        for (String ruta : List.of(a + "/subir/pdf", a)) {
            caso(casos, mvc, "POST " + ruta + " pdf", multipart(ruta).file(pdf("oficio.pdf", PDF)));
            caso(casos, mvc, "POST " + ruta + " pdf inscripcionId=7", multipart(ruta).file(pdf("oficio.pdf", PDF))
                    .param("inscripcionId", "7"));
            caso(casos, mvc, "POST " + ruta + " pdf idSolicitud=abc", multipart(ruta).file(pdf("oficio.pdf", PDF))
                    .param("idSolicitud", "abc"));
            caso(casos, mvc, "POST " + ruta + " texto", multipart(ruta)
                    .file(pdf("oficio.pdf", "no es pdf".getBytes(StandardCharsets.UTF_8))));
            caso(casos, mvc, "POST " + ruta + " sin archivo", multipart(ruta));
        }
        // Descarga por nombre
        for (String nombre : List.of("existe.pdf", "falta.pdf", "ajeno.pdf", "oficio reingreso&1 á.pdf")) {
            caso(casos, mvc, "GET " + a + "/descargar/pdf?filename=" + nombre,
                    get(a + "/descargar/pdf").param("filename", nombre));
            caso(casos, mvc, "GET " + a + "?nombre=" + nombre, get(a).param("nombre", nombre));
        }
        staff = false;
        caso(casos, mvc, "GET " + a + "/descargar/pdf?filename=ajeno.pdf (estudiante)",
                get(a + "/descargar/pdf").param("filename", "ajeno.pdf"));
        caso(casos, mvc, "GET " + a + "?nombre=ajeno.pdf (estudiante)", get(a).param("nombre", "ajeno.pdf"));
        staff = true;
        caso(casos, mvc, "GET " + a + "/descargar/pdf", get(a + "/descargar/pdf"));
        caso(casos, mvc, "GET " + a, get(a));
        // Comprobante de una inscripción
        for (String id : List.of("7", "8", "9", "0", "abc")) {
            caso(casos, mvc, "GET " + a + "/descargar/pdf/inscripcion/" + id, get(a + "/descargar/pdf/inscripcion/" + id));
            caso(casos, mvc, "GET " + a + "/inscripciones/" + id, get(a + "/inscripciones/" + id));
        }

        // EST-7: las rutas nuevas responden como las antiguas, que pasan a 404
        GrafoObjetos.compararConReferencia(casos, "rutas/documentos-archivos.json",
                "rutas/correcciones/documentos-archivos-est7.json");
    }

    private static MockMultipartFile pdf(String nombre, byte[] contenido) {
        return new MockMultipartFile("file", nombre, MediaType.APPLICATION_PDF_VALUE, contenido);
    }

    /** Estado, tipo, cabeceras de descarga, cuerpo (JSON o Base64) y llamadas al caso de uso que cambian datos. */
    private void caso(ObjectNode casos, MockMvc mvc, String clave, MockHttpServletRequestBuilder peticion)
            throws Exception {
        llamadas.removeAll();
        var resultado = mvc.perform(peticion).andReturn();
        if (resultado.getRequest().isAsyncStarted()) {
            resultado.getAsyncResult(10000);
            resultado = mvc.perform(asyncDispatch(resultado)).andReturn();
        }
        var respuesta = resultado.getResponse();
        ObjectNode caso = json.createObjectNode();
        caso.put("status", respuesta.getStatus());
        caso.put("tipo", respuesta.getContentType());
        for (String cabecera : List.of("Content-Disposition", "Cache-Control", "Content-Length")) {
            List<String> valores = new ArrayList<>(respuesta.getHeaders(cabecera));
            if (!valores.isEmpty()) {
                caso.put(cabecera, String.join(" | ", valores));
            }
        }
        byte[] bytes = respuesta.getContentAsByteArray();
        JsonNode cuerpo;
        if (bytes.length == 0) {
            cuerpo = json.nullNode();
        } else if (respuesta.getContentType() != null && respuesta.getContentType().contains("json")) {
            cuerpo = json.readTree(new String(bytes, StandardCharsets.UTF_8));
            if (cuerpo.has("fechaSubida")) {
                ((ObjectNode) cuerpo).put("id", "<reloj>");
                ((ObjectNode) cuerpo).put("fechaSubida", "<fecha>");
            }
        } else {
            cuerpo = json.getNodeFactory().textNode("base64:" + Base64.getEncoder().encodeToString(bytes));
        }
        caso.set("cuerpo", cuerpo);
        caso.set("llamadas", llamadas.deepCopy());
        casos.set(clave, caso);
    }
}
