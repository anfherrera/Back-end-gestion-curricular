package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GenerarDocumentosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocumentosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudPazYSalvoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.ArchivoSubido;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.DocumentosPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;

/**
 * Caracterización previa a la migración de rutas de EST-7 (paso 7a): Paz y Salvo. Cada operación se pide por su
 * ruta antigua y por la nueva, con la aplicación real (mappers, {@code Autorizacion} y seguridad de método activas)
 * y los casos de uso, el generador de documentos, los archivos y los gateways de usuarios y solicitudes simulados.
 * Cada caso indica quién pide: el estudiante 5 (dueño de la solicitud 40), el estudiante 6 (dueño de la 41), un
 * funcionario, un coordinador del programa 3 o la secretaría. Guarda el estado, el tipo, el cuerpo (el texto, si no
 * es JSON), el {@code Content-Disposition} y las llamadas a los puertos, con el período actual como marcador.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class RutasPazYSalvoCaracterizacionTest {

    private static final Date FECHA = new Date(1740000000000L);
    private static final byte[] PDF = "%PDF-1.4 contenido".getBytes(StandardCharsets.US_ASCII);

    private final ObjectMapper json = new ObjectMapper();
    private final ArrayNode llamadas = json.createArrayNode();

    @Autowired MockMvc mvc;
    @MockBean GestionarSolicitudPazYSalvoCUIntPort pazYSalvo;
    @MockBean GestionarArchivosCUIntPort archivos;
    @MockBean GestionarDocumentosCUIntPort documentos;
    @MockBean GenerarDocumentosCUIntPort generador;
    @MockBean GestionarSolicitudGatewayIntPort solicitudGateway;
    @MockBean GestionarUsuarioGatewayIntPort usuarioGateway;
    /** El adaptador del gateway también es el UserDetailsService; al simular el gateway hay que darlo aparte. */
    @MockBean UserDetailsService userDetailsService;

    @BeforeEach
    void preparar() throws Exception {
        Usuario estudiante5 = usuario(5, "Estudiante");
        Usuario estudiante6 = usuario(6, "Estudiante");
        Usuario coordinador = usuario(2, "Coordinador");
        Programa programa = new Programa();
        programa.setIdPrograma(3);
        coordinador.setObjPrograma(programa);
        when(usuarioGateway.buscarUsuarioPorCorreo("est5@prueba.test")).thenReturn(estudiante5);
        when(usuarioGateway.buscarUsuarioPorCorreo("est6@prueba.test")).thenReturn(estudiante6);
        when(usuarioGateway.buscarUsuarioPorCorreo("funcionario@prueba.test")).thenReturn(usuario(1, "Funcionario"));
        when(usuarioGateway.buscarUsuarioPorCorreo("coordinador@prueba.test")).thenReturn(coordinador);
        when(usuarioGateway.buscarUsuarioPorCorreo("secretaria@prueba.test")).thenReturn(usuario(4, "Secretario"));
        when(solicitudGateway.idDuenoDeSolicitud(40)).thenReturn(5);
        when(solicitudGateway.idDuenoDeSolicitud(41)).thenReturn(6);

        Documento carta = documento(70, "carta.pdf", "pazysalvo/40/carta.pdf", null);
        Documento oficio = documento(71, "oficio.pdf", "pazysalvo/40/oficio.pdf", "Archivo subido por secretaría");
        SolicitudPazYSalvo s40 = solicitud(40, estudiante5, List.of(carta, oficio));
        SolicitudPazYSalvo s41 = solicitud(41, estudiante6, List.of());
        when(pazYSalvo.buscarPorId(40)).thenReturn(s40);
        when(pazYSalvo.buscarPorId(41)).thenReturn(s41);
        when(pazYSalvo.listarSolicitudes()).thenReturn(List.of(s40, s41));
        doAnswer(i -> {
            llamadas.addObject().put("listar", conMarcador(i.getArgument(0).toString()));
            return List.of(s40);
        }).when(pazYSalvo).listar(any());
        doAnswer(i -> {
            llamadas.addObject().put("listarSolicitudesPorRol", i.getArgument(0) + ", " + i.getArgument(1));
            return "ESTUDIANTE".equals(i.getArgument(0)) && Integer.valueOf(5).equals(i.getArgument(1))
                    ? List.of(s40) : List.of(s41);
        }).when(pazYSalvo).listarSolicitudesPorRol(anyString(), any());
        doAnswer(i -> {
            SolicitudPazYSalvo s = i.getArgument(0);
            llamadas.addObject().put("guardar", "usuario " + s.getObjUsuario().getIdUsuario() + ", periodo "
                    + s.getPeriodoAcademico() + ", titulo " + s.getTituloTrabajoGrado());
            s.setIdSolicitud(50);
            return s;
        }).when(pazYSalvo).guardar(any());
        doAnswer(i -> {
            Predicate<String> puede = i.getArgument(3);
            llamadas.addObject().put("cambiarEstadoSolicitud", i.getArgument(0) + " -> " + i.getArgument(1)
                    + ", comentario " + i.getArgument(2) + ", como funcionario " + puede.test("Funcionario"));
            return "XYZ".equals(i.getArgument(1))
                    ? Optional.of(new GestionarSolicitudPazYSalvoCUIntPort.RechazoCambioEstado(
                            GestionarSolicitudPazYSalvoCUIntPort.RechazoCambioEstado.Motivo.TRANSICION_NO_PERMITIDA,
                            "Transición no permitida", "ENVIADA"))
                    : Optional.empty();
        }).when(pazYSalvo).cambiarEstadoSolicitud(any(), any(), any(), any());
        when(pazYSalvo.documentosConTipo(40)).thenReturn(List.of(
                new GestionarSolicitudPazYSalvoCUIntPort.DocumentoConTipo(carta, "ESTUDIANTE"),
                new GestionarSolicitudPazYSalvoCUIntPort.DocumentoConTipo(oficio, "OFICIO")));
        when(pazYSalvo.oficios(40)).thenReturn(List.of(oficio));
        when(pazYSalvo.oficios(41)).thenReturn(List.of());
        when(pazYSalvo.comentarios(40)).thenReturn(new GestionarSolicitudPazYSalvoCUIntPort.ComentariosSolicitud(
                "Falta la firma", List.of(new GestionarSolicitudPazYSalvoCUIntPort.ComentarioDocumento(
                        70, "carta.pdf", "Ilegible"))));
        when(pazYSalvo.comentarios(41)).thenReturn(
                new GestionarSolicitudPazYSalvoCUIntPort.ComentariosSolicitud(null, List.of()));
        when(pazYSalvo.revisarDocumentosRequeridos(40)).thenReturn(new DocumentosPazYSalvo.RevisionDocumentos(
                Map.of(DocumentosPazYSalvo.Requerido.FORMATO_PM_FO_4_FOR_27, true), false, 2));

        when(archivos.getFile(anyString())).thenReturn(new byte[0]);
        when(archivos.getFile("pazysalvo/40/carta.pdf")).thenReturn("PDF carta".getBytes(StandardCharsets.UTF_8));
        when(archivos.getFile("pazysalvo/40/oficio.pdf")).thenReturn("PDF oficio".getBytes(StandardCharsets.UTF_8));

        doAnswer(i -> {
            llamadas.addObject().put("generarPazYSalvo", i.getArgument(0) + ", cedula " + i.getArgument(1)
                    + ", titulo " + i.getArgument(2) + ", director " + i.getArgument(3) + ", porDefecto "
                    + i.getArgument(4) + ", pdf " + i.getArgument(5) + ", numero " + i.getArgument(6) + ", fecha "
                    + i.getArgument(7) + ", observaciones " + i.getArgument(8));
            return Optional.of(new GenerarDocumentosCUIntPort.DocumentoGenerado("PazYSalvo_" + i.getArgument(0)
                    + ".docx", "DOCUMENTO".getBytes(StandardCharsets.UTF_8)));
        }).when(generador).generarPazYSalvo(any(), any(), any(), any(), anyBoolean(), anyBoolean(), any(), any(),
                any());

        doAnswer(i -> {
            llamadas.addObject().put("subirDocumento", nombre(i.getArgument(0)) + " de " + i.getArgument(1));
            return documento(80, nombre(i.getArgument(0)), "temp/" + nombre(i.getArgument(0)), null);
        }).when(documentos).subirDocumento(any(), any());
        doAnswer(i -> {
            llamadas.addObject().put("adjuntarASolicitud", nombre(i.getArgument(0)) + " a "
                    + i.<Solicitud>getArgument(1).getIdSolicitud() + " en " + i.getArgument(2) + ", comentario " + i.getArgument(3)
                    + ", nombreGuardado " + i.getArgument(4));
            return documento(81, nombre(i.getArgument(0)), "pazysalvo/40/" + nombre(i.getArgument(0)), null);
        }).when(documentos).adjuntarASolicitud(any(), any(), any(), any(), anyBoolean());
        doAnswer(i -> {
            boolean existe = i.<BooleanSupplier>getArgument(4).getAsBoolean();
            llamadas.addObject().put("guardarYAdjuntarSiExiste", nombre(i.getArgument(0)) + ", " + i.getArgument(1)
                    + " en " + i.getArgument(2) + " a " + i.getArgument(3) + ", existe " + existe);
            return new GestionarDocumentosCUIntPort.AdjuntoCondicional(existe,
                    existe ? documento(82, nombre(i.getArgument(0)), "pazysalvo/40/oficio.docx", null) : null);
        }).when(documentos).guardarYAdjuntarSiExiste(any(), any(), any(), any(), any());
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void pazYSalvo() throws Exception {
        String b = "/api/solicitudes-pazysalvo";
        ObjectNode casos = json.createObjectNode();

        // Crear
        for (String ruta : List.of(b + "/crearSolicitud-PazYSalvo", b)) {
            caso(casos, "est5", HttpMethod.POST, ruta, "{\"idUsuario\":\"6\",\"tituloTrabajoGrado\":\"Tesis\"}");
            caso(casos, "funcionario", HttpMethod.POST, ruta, "{\"idUsuario\":\"5\"}");
            caso(casos, "est5", HttpMethod.POST, ruta, "{}");
            caso(casos, "est5", HttpMethod.POST, ruta, "{\"idUsuario\":\"5\",\"periodoAcademico\":\"2024-9\"}");
        }

        // Todas (se retira) y lo que queda en la raíz sin filtros
        caso(casos, "funcionario", HttpMethod.GET, b + "/listarSolicitud-PazYSalvo", null);
        caso(casos, "est5", HttpMethod.GET, b + "/listarSolicitud-PazYSalvo", null);
        caso(casos, "funcionario", HttpMethod.GET, b, null);

        // Bandejas de pendientes
        List<String[]> bandejas = List.of(
                new String[] {"funcionario", "/Funcionario", "rol=funcionario"},
                new String[] {"funcionario", "/Funcionario?periodoAcademico=2025-1", "rol=funcionario&periodoAcademico=2025-1"},
                new String[] {"coordinador", "/Coordinador", "rol=coordinador"},
                new String[] {"coordinador", "/Coordinador?periodoAcademico=2025-1", "rol=coordinador&periodoAcademico=2025-1"},
                new String[] {"funcionario", "/Coordinador", "rol=coordinador"},
                new String[] {"secretaria", "/Secretaria", "rol=secretaria"},
                new String[] {"secretaria", "/Secretaria?periodoAcademico=2025-1", "rol=secretaria&periodoAcademico=2025-1"},
                new String[] {"est5", "/Funcionario", "rol=funcionario"});
        for (String[] c : bandejas) {
            caso(casos, c[0], HttpMethod.GET, b + "/listarSolicitud-PazYSalvo" + c[1], null);
            caso(casos, c[0], HttpMethod.GET, b + "?" + c[2].replaceFirst("(rol=[a-z]+)", "$1&bandeja=pendientes"), null);
        }

        // Procesadas (BUG-45) y errores de la raíz: no cambian de ruta
        for (String consulta : List.of("rol=funcionario&bandeja=procesadas", "rol=coordinador&bandeja=procesadas",
                "rol=otro&bandeja=procesadas", "rol=funcionario&bandeja=otra", "bandeja=procesadas", "rol=funcionario")) {
            caso(casos, "funcionario", HttpMethod.GET, b + "?" + consulta, null);
        }

        // Por rol
        for (String prefijo : List.of(b + "/listarSolicitud-PazYSalvo/porRol?", b + "?")) {
            caso(casos, "est5", HttpMethod.GET, prefijo + "rol=FUNCIONARIO", null);
            caso(casos, "funcionario", HttpMethod.GET, prefijo + "rol=FUNCIONARIO", null);
            caso(casos, "funcionario", HttpMethod.GET, prefijo + "rol=ESTUDIANTE&idUsuario=6", null);
        }

        // Ya con la convención: no cambian
        caso(casos, "est5", HttpMethod.GET, b + "/40", null);
        caso(casos, "est5", HttpMethod.GET, b + "/usuarios/5/solicitudes?periodoAcademico=2025-1", null);

        // Cambio de estado
        String aprobar = "{\"idSolicitud\":40,\"nuevoEstado\":\"APROBADA_FUNCIONARIO\",\"comentario\":\"ok\"}";
        String noPermitido = "{\"idSolicitud\":40,\"nuevoEstado\":\"XYZ\"}";
        String sinEstado = "{\"idSolicitud\":40}";
        for (String ruta : List.of(b + "/actualizarEstadoSolicitud", b + "/40/estado")) {
            caso(casos, "funcionario", HttpMethod.PUT, ruta, aprobar);
            caso(casos, "funcionario", HttpMethod.PUT, ruta, noPermitido);
            caso(casos, "funcionario", HttpMethod.PUT, ruta, sinEstado);
            caso(casos, "est5", HttpMethod.PUT, ruta, aprobar);
        }
        caso(casos, "funcionario", HttpMethod.PUT, b + "/actualizarEstadoSolicitud", "{\"nuevoEstado\":\"APROBADA\"}");

        // Documento por defecto (se retira)
        for (String formato : List.of("pdf", "docx")) {
            caso(casos, "est5", HttpMethod.GET, b + "/generarDocumentoPazYSalvo/40/" + formato, null);
            caso(casos, "est6", HttpMethod.GET, b + "/generarDocumentoPazYSalvo/40/" + formato, null);
        }

        // Documento generado con número y fecha
        String datos = "?numeroDocumento=PYS-1&fechaDocumento=2025-03-01&observaciones=Sin%20pendientes";
        for (String ruta : List.of(b + "/generar-documento/40", b + "/40/documento-generado")) {
            caso(casos, "funcionario", HttpMethod.POST, ruta + datos, null);
            caso(casos, "funcionario", HttpMethod.POST, ruta + "?fechaDocumento=2025-03-01", null);
            caso(casos, "est5", HttpMethod.POST, ruta + datos, null);
        }

        // Subir un documento sin asociar
        for (String ruta : List.of(b + "/subir-documento", b + "/documentos")) {
            subida(casos, "est5", ruta, "file", "cedula.pdf", PDF);
            subida(casos, "est5", ruta, "file", "cedula.txt", "texto".getBytes(StandardCharsets.UTF_8));
        }

        // Descargar por nombre
        for (String ruta : List.of(b + "/descargar-documento?filename=", b + "/documentos?nombre=")) {
            caso(casos, "est5", HttpMethod.GET, ruta + "carta.pdf", null);
            caso(casos, "est6", HttpMethod.GET, ruta + "carta.pdf", null);
            caso(casos, "funcionario", HttpMethod.GET, ruta + "carta.pdf", null);
            caso(casos, "funcionario", HttpMethod.GET, ruta + "nada.pdf", null);
        }

        // Por solicitud: oficio, documentos, comentarios y oficios
        List<String[]> porSolicitud = List.of(
                new String[] {"/descargarOficio/", "/oficios/archivo"},
                new String[] {"/obtenerDocumentos/coordinador/", "/documentos"},
                new String[] {"/obtenerComentarios/", "/comentarios"},
                new String[] {"/obtenerOficios/", "/oficios"});
        for (String[] c : porSolicitud) {
            for (String[] quienYId : List.of(new String[] {"est5", "40"}, new String[] {"est6", "40"},
                    new String[] {"funcionario", "41"})) {
                caso(casos, quienYId[0], HttpMethod.GET, b + c[0] + quienYId[1], null);
                caso(casos, quienYId[0], HttpMethod.GET, b + "/" + quienYId[1] + c[1], null);
            }
        }

        // Documentos requeridos (se retira)
        caso(casos, "est5", HttpMethod.GET, b + "/validarDocumentosRequeridos/40", null);
        caso(casos, "funcionario", HttpMethod.GET, b + "/validarDocumentosRequeridos/40", null);

        // Guardar oficio Word (se retira)
        String paramsOficio = "?idSolicitud=40&tipoDocumento=OFICIO&numeroDocumento=PYS-1&fechaDocumento=2025-03-01";
        subida(casos, "funcionario", b + "/guardarOficio" + paramsOficio, "file", "oficio.docx",
                "DOCX".getBytes(StandardCharsets.UTF_8));
        subida(casos, "funcionario", b + "/guardarOficio" + paramsOficio.replace("=40", "=99"), "file",
                "oficio.docx", "DOCX".getBytes(StandardCharsets.UTF_8));

        // Subir el oficio PDF de la secretaría
        for (String[] rutas : List.of(new String[] {b + "/subir-oficio-pdf/40", b + "/40/oficios"},
                new String[] {b + "/subir-oficio-pdf/99", b + "/99/oficios"})) {
            for (String ruta : rutas) {
                subida(casos, "secretaria", ruta, "file", "resolucion.pdf", PDF);
            }
        }
        subida(casos, "est5", b + "/subir-oficio-pdf/40", "file", "resolucion.pdf", PDF);
        subida(casos, "est5", b + "/40/oficios", "file", "resolucion.pdf", PDF);

        // Solo rutas nuevas: lo que la convención decide aparte
        caso(casos, "funcionario", HttpMethod.PUT, b + "/40/estado", "{\"idSolicitud\":41,\"nuevoEstado\":\"APROBADA\"}");
        caso(casos, "funcionario", HttpMethod.PUT, b + "/40/estado", "{\"nuevoEstado\":\"APROBADA\"}");
        caso(casos, "funcionario", HttpMethod.GET, b + "?rol=otro&bandeja=pendientes", null);
        caso(casos, "funcionario", HttpMethod.GET, b + "?bandeja=pendientes", null);

        // EST-7: las rutas nuevas responden como las antiguas, que pasan a 404
        GrafoObjetos.compararConReferencia(casos, "rutas/pazysalvo.json");
    }

    private void caso(ObjectNode casos, String quien, HttpMethod metodo, String ruta, String cuerpo) throws Exception {
        MockHttpServletRequestBuilder peticion = request(metodo, ruta);
        if (cuerpo != null) {
            peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
        }
        pedir(casos, quien, peticion, metodo + " " + ruta + (cuerpo == null ? "" : " " + cuerpo) + " [" + quien + "]");
    }

    private void subida(ObjectNode casos, String quien, String ruta, String campo, String nombre, byte[] contenido)
            throws Exception {
        var peticion = multipart(ruta).file(new MockMultipartFile(campo, nombre, "application/octet-stream", contenido));
        pedir(casos, quien, peticion, "POST " + ruta + " archivo " + nombre + " [" + quien + "]");
    }

    private void pedir(ObjectNode casos, String quien, MockHttpServletRequestBuilder peticion, String clave)
            throws Exception {
        llamadas.removeAll();
        String rol = switch (quien) {
            case "funcionario" -> "Funcionario";
            case "coordinador" -> "Coordinador";
            case "secretaria" -> "Secretario";
            default -> "Estudiante";
        };
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                quien + "@prueba.test", "x", List.of(new SimpleGrantedAuthority("ROLE_" + rol))));
        var respuesta = mvc.perform(peticion).andReturn().getResponse();
        SecurityContextHolder.clearContext();
        ObjectNode caso = json.createObjectNode();
        caso.put("status", respuesta.getStatus());
        caso.put("tipo", respuesta.getContentType());
        String texto = respuesta.getContentAsString(StandardCharsets.UTF_8);
        String tipo = respuesta.getContentType();
        JsonNode valor = texto.isEmpty() ? json.nullNode()
                : tipo != null && tipo.contains("json") ? json.readTree(texto) : json.getNodeFactory().textNode(texto);
        caso.set("cuerpo", valor);
        caso.put("disposicion", respuesta.getHeader(HttpHeaders.CONTENT_DISPOSITION));
        caso.set("llamadas", llamadas.deepCopy());
        casos.set(clave, caso);
    }

    private static String nombre(Object archivo) {
        return ((ArchivoSubido) archivo).nombreOriginal();
    }

    /** El período actual depende de la fecha: se sustituye por un marcador. */
    private static String conMarcador(String texto) {
        PeriodoAcademicoEnum actual = PeriodoAcademicoEnum.getPeriodoActual();
        // FiltroSolicitudes es un record: "…, periodo=2026-2, …"
        return actual == null ? texto : texto.replace("periodo=" + actual.getValor() + ",", "periodo=<ACTUAL>,");
    }

    private static Usuario usuario(int id, String rol) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setNombreCompleto("Usuario " + id);
        Rol objRol = new Rol();
        objRol.setNombre(rol);
        usuario.setObjRol(objRol);
        return usuario;
    }

    private static Documento documento(int id, String nombre, String ruta, String comentario) {
        Documento d = new Documento();
        d.setIdDocumento(id);
        d.setNombre(nombre);
        d.setRutaDocumento(ruta);
        d.setFechaDocumento(FECHA);
        d.setEsValido(true);
        d.setComentario(comentario);
        return d;
    }

    private static SolicitudPazYSalvo solicitud(int id, Usuario usuario, List<Documento> documentos) {
        SolicitudPazYSalvo s = new SolicitudPazYSalvo();
        s.setIdSolicitud(id);
        s.setNombreSolicitud("Paz y salvo " + id);
        s.setPeriodoAcademico("2025-1");
        s.setFechaRegistroSolicitud(FECHA);
        s.setObjUsuario(usuario);
        s.setTituloTrabajoGrado("Tesis " + id);
        EstadoSolicitud e = new EstadoSolicitud();
        e.setIdEstado(id * 10);
        e.setEstadoActual("ENVIADA");
        e.setFechaRegistroEstado(FECHA);
        s.setEstadosSolicitud(new ArrayList<>(List.of(e)));
        s.setDocumentos(new ArrayList<>(documentos));
        return s;
    }
}
