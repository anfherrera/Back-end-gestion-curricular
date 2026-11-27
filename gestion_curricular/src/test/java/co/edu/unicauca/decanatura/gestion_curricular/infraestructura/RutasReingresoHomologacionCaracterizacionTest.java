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
import java.util.function.Supplier;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocumentosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudHomologacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudReingresoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.ArchivoSubido;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;

/**
 * Caracterización previa a la migración de rutas de EST-7 (paso 7b): Reingreso y Homologación. Cada operación se
 * pide por su ruta antigua y por la nueva, con la aplicación real (mappers, {@code Autorizacion},
 * {@code ValidadorCambioEstado} y seguridad de método activas) y los casos de uso, los archivos, los documentos y los
 * gateways de usuarios y solicitudes simulados. Cada caso indica quién pide: el estudiante 5 (dueño de las
 * solicitudes 40 y 60), el estudiante 6 (dueño de la 41 y la 61), un funcionario, un coordinador del programa 3, la
 * secretaría o un administrador. Guarda el estado, el tipo, el cuerpo (el texto, si no es JSON), el
 * {@code Content-Disposition} y las llamadas a los casos de uso, con el período actual como marcador.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class RutasReingresoHomologacionCaracterizacionTest {

    private static final Date FECHA = new Date(1740000000000L);
    private static final byte[] PDF = "%PDF-1.4 contenido".getBytes(StandardCharsets.US_ASCII);
    private static final String USUARIO_6 = "\"objUsuario\":{\"id_usuario\":6,\"nombre_completo\":\"Usuario 6\","
            + "\"codigo\":\"102010\",\"correo\":\"est6@unicauca.edu.co\",\"id_rol\":1,\"id_programa\":1}";

    private final ObjectMapper json = new ObjectMapper();
    private final ArrayNode llamadas = json.createArrayNode();

    @Autowired MockMvc mvc;
    @MockitoBean GestionarSolicitudReingresoCUIntPort reingreso;
    @MockitoBean GestionarSolicitudHomologacionCUIntPort homologacion;
    @MockitoBean GestionarArchivosCUIntPort archivos;
    @MockitoBean GestionarDocumentosCUIntPort documentos;
    @MockitoBean GestionarSolicitudGatewayIntPort solicitudGateway;
    @MockitoBean GestionarUsuarioGatewayIntPort usuarioGateway;
    /** El adaptador del gateway también es el UserDetailsService; al simular el gateway hay que darlo aparte. */
    @MockitoBean UserDetailsService userDetailsService;

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
        when(usuarioGateway.buscarUsuarioPorCorreo("admin@prueba.test")).thenReturn(usuario(7, "Administrador"));
        for (int id : new int[] {40, 60}) {
            when(solicitudGateway.idDuenoDeSolicitud(id)).thenReturn(5);
            when(solicitudGateway.idDuenoDeSolicitud(id + 1)).thenReturn(6);
        }

        // Reingreso: la carta del estudiante y un oficio de la secretaría
        SolicitudReingreso r40 = solicitud(SolicitudReingreso::new, 40, estudiante5, List.of(
                documento(70, "carta_solicitud_reingreso.pdf", "reingreso/40/carta_solicitud_reingreso.pdf", null),
                documento(71, "oficio_reingreso.pdf", "reingreso/40/oficio_reingreso.pdf", null)));
        SolicitudReingreso r41 = solicitud(SolicitudReingreso::new, 41, estudiante6, List.of());
        when(reingreso.obtenerSolicitudReingresoPorId(40)).thenReturn(r40);
        when(reingreso.obtenerSolicitudReingresoPorId(41)).thenReturn(r41);
        doAnswer(i -> {
            llamadas.addObject().put("listar", conMarcador(i.getArgument(0).toString()));
            return List.of(r40);
        }).when(reingreso).listar(any());
        doAnswer(i -> {
            llamadas.addObject().put("listarSolicitudesReingresoPorRol", i.getArgument(0) + ", " + i.getArgument(1));
            return List.of(r41);
        }).when(reingreso).listarSolicitudesReingresoPorRol(anyString(), any());
        doAnswer(i -> {
            SolicitudReingreso s = i.getArgument(0);
            llamadas.addObject().put("crearSolicitudReingreso", guardada(s));
            s.setIdSolicitud(50);
            return s;
        }).when(reingreso).crearSolicitudReingreso(any());
        doAnswer(i -> {
            llamadas.addObject().put("cambiarEstadoSolicitudReingreso", i.getArgument(0) + " -> " + i.getArgument(1));
            return null;
        }).when(reingreso).cambiarEstadoSolicitudReingreso(any(), any());

        // Homologación: el formulario del estudiante y la resolución marcada por la secretaría
        SolicitudHomologacion h60 = solicitud(SolicitudHomologacion::new, 60, estudiante5, List.of(
                documento(80, "solicitud_homologacion.pdf", "homologacion/60/solicitud_homologacion.pdf", null),
                documento(81, "resolucion_60.pdf", "homologacion/60/resolucion_60.pdf",
                        "[OFICIO_HOMOLOGACION] Archivo subido por secretaría")));
        SolicitudHomologacion h61 = solicitud(SolicitudHomologacion::new, 61, estudiante6, List.of());
        when(homologacion.buscarPorId(60)).thenReturn(h60);
        when(homologacion.buscarPorId(61)).thenReturn(h61);
        doAnswer(i -> {
            llamadas.addObject().put("listar", conMarcador(i.getArgument(0).toString()));
            return List.of(h60);
        }).when(homologacion).listar(any());
        doAnswer(i -> {
            llamadas.addObject().put("listarSolicitudesPorRol", i.getArgument(0) + ", " + i.getArgument(1));
            return List.of(h61);
        }).when(homologacion).listarSolicitudesPorRol(anyString(), any());
        doAnswer(i -> {
            SolicitudHomologacion s = i.getArgument(0);
            llamadas.addObject().put("guardar", guardada(s) + ", origen " + s.getProgramaOrigen());
            s.setIdSolicitud(50);
            return s;
        }).when(homologacion).guardar(any());
        doAnswer(i -> {
            llamadas.addObject().put("cambiarEstadoSolicitud", i.getArgument(0) + " -> " + i.getArgument(1));
            return null;
        }).when(homologacion).cambiarEstadoSolicitud(any(), any());

        when(archivos.getFile(anyString())).thenReturn("PDF por nombre".getBytes(StandardCharsets.UTF_8));
        when(archivos.getFileByPath(anyString())).thenAnswer(i ->
                ("PDF de " + i.getArgument(0)).getBytes(StandardCharsets.UTF_8));
        doAnswer(i -> {
            llamadas.addObject().put("adjuntarASolicitud", nombre(i.getArgument(0)) + " a "
                    + i.<Solicitud>getArgument(1).getIdSolicitud() + " en " + i.getArgument(2) + ", comentario "
                    + i.getArgument(3) + ", nombreGuardado " + i.getArgument(4));
            return documento(90, "resolucion_guardada.pdf", "homologacion/60/resolucion_guardada.pdf", null);
        }).when(documentos).adjuntarASolicitud(any(), any(), any(), any(), anyBoolean());
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void reingreso() throws Exception {
        ObjectNode casos = json.createObjectNode();
        proceso(casos, "/api/solicitudes-reingreso", "/crearSolicitud-Reingreso", "/listarSolicitudes-Reingreso",
                "/listarSolicitud-Reingreso", "/porUser", "/listarSolicitud-Reingreo/", "/actualizarEstadoSolicitud-Reingreso",
                40, "{\"nombre_solicitud\":\"Solicitud de reingreso\",\"fecha_registro_solicitud\":\"2025-01-19T00:00:00.000Z\","
                        + "\"periodo_academico\":\"2025-1\"," + USUARIO_6 + "}");
        // EST-7: las rutas nuevas responden como las antiguas, que pasan a 404
        GrafoObjetos.compararConReferencia(casos, "rutas/reingreso.json", "rutas/correcciones/reingreso-est7.json");
    }

    @Test
    void homologacion() throws Exception {
        String b = "/api/solicitudes-homologacion";
        ObjectNode casos = json.createObjectNode();
        proceso(casos, b, "/crearSolicitud-Homologacion", "/listarSolicitud-Homologacion", "/listarSolicitud-Homologacion",
                "/porRol", "/listarSolicitud-Homologacion/", "/actualizarEstadoSolicitud", 60,
                "{\"nombre_solicitud\":\"Solicitud de homologacion\",\"fecha_registro_solicitud\":\"2025-01-19T00:00:00.000Z\","
                        + "\"periodo_academico\":\"2025-1\",\"programa_origen\":\"Origen\",\"programa_destino\":\"Destino\","
                        + USUARIO_6 + "}");

        // Subir la resolución de la secretaría
        for (String[] rutas : List.of(new String[] {b + "/60/subir-resolucion", b + "/60/resoluciones"},
                new String[] {b + "/99/subir-resolucion", b + "/99/resoluciones"})) {
            for (String ruta : rutas) {
                subida(casos, "secretaria", ruta, "file", "resolucion.pdf", PDF);
            }
        }
        for (String ruta : List.of(b + "/60/subir-resolucion", b + "/60/resoluciones")) {
            subida(casos, "secretaria", ruta, "file", "resolucion.txt", "texto".getBytes(StandardCharsets.UTF_8));
            subida(casos, "est5", ruta, "file", "resolucion.pdf", PDF);
        }
        // EST-7: las rutas nuevas responden como las antiguas, que pasan a 404
        GrafoObjetos.compararConReferencia(casos, "rutas/homologacion.json", "rutas/correcciones/homologacion-est7.json");
    }

    /**
     * Las operaciones comunes de los dos procesos. {@code id} es la solicitud del estudiante 5, con un oficio;
     * {@code id + 1}, la del estudiante 6, sin documentos.
     */
    private void proceso(ObjectNode casos, String b, String crear, String listado, String bandejas, String porRol,
            String porId, String estado, int id, String cuerpoCrear) throws Exception {
        String otra = String.valueOf(id + 1);

        // Crear
        for (String ruta : List.of(b + crear, b)) {
            caso(casos, "est5", HttpMethod.POST, ruta, cuerpoCrear);
            caso(casos, "admin", HttpMethod.POST, ruta, cuerpoCrear);
            caso(casos, "funcionario", HttpMethod.POST, ruta, cuerpoCrear);
            caso(casos, "est5", HttpMethod.POST, ruta, "{}");
        }

        // Todas (se retira) y lo que queda en la raíz sin filtros
        caso(casos, "funcionario", HttpMethod.GET, b + listado, null);
        caso(casos, "est5", HttpMethod.GET, b + listado, null);
        caso(casos, "funcionario", HttpMethod.GET, b, null);

        // Por rol (o por usuario)
        List<String[]> consultas = List.of(
                new String[] {"est5", "rol=FUNCIONARIO"},
                new String[] {"est5", "rol=ESTUDIANTE&periodoAcademico=2025-1"},
                new String[] {"funcionario", "rol=FUNCIONARIO"},
                new String[] {"funcionario", "rol=ESTUDIANTE&idUsuario=6"},
                new String[] {"funcionario", "rol=ESTUDIANTE&idUsuario=6&periodoAcademico=2025-1"});
        for (String[] c : consultas) {
            caso(casos, c[0], HttpMethod.GET, b + bandejas + porRol + "?" + c[1], null);
            caso(casos, c[0], HttpMethod.GET, b + "?" + c[1], null);
        }

        // Bandejas de pendientes y de procesadas
        List<String[]> bandejasPorRol = List.of(
                new String[] {"funcionario", "/Funcionario", "rol=funcionario&bandeja=pendientes"},
                new String[] {"funcionario", "/Funcionario?periodoAcademico=2025-1",
                        "rol=funcionario&bandeja=pendientes&periodoAcademico=2025-1"},
                new String[] {"coordinador", "/Coordinador", "rol=coordinador&bandeja=pendientes"},
                new String[] {"coordinador", "/Coordinador?periodoAcademico=2025-1",
                        "rol=coordinador&bandeja=pendientes&periodoAcademico=2025-1"},
                new String[] {"funcionario", "/Coordinador", "rol=coordinador&bandeja=pendientes"},
                new String[] {"secretaria", "/Secretaria", "rol=secretaria&bandeja=pendientes"},
                new String[] {"secretaria", "/Secretaria?periodoAcademico=2025-1",
                        "rol=secretaria&bandeja=pendientes&periodoAcademico=2025-1"},
                new String[] {"est5", "/Funcionario", "rol=funcionario&bandeja=pendientes"},
                new String[] {"funcionario", "/Funcionario/Aprobadas", "rol=funcionario&bandeja=procesadas"},
                new String[] {"funcionario", "/Funcionario/Aprobadas?periodoAcademico=2025-1",
                        "rol=funcionario&bandeja=procesadas&periodoAcademico=2025-1"},
                new String[] {"funcionario", "/Funcionario/Aprobadas?periodoAcademico=todos",
                        "rol=funcionario&bandeja=procesadas&periodoAcademico=todos"},
                new String[] {"coordinador", "/Coordinador/Aprobadas", "rol=coordinador&bandeja=procesadas"},
                new String[] {"coordinador", "/Coordinador/Aprobadas?periodoAcademico=2025-1",
                        "rol=coordinador&bandeja=procesadas&periodoAcademico=2025-1"},
                new String[] {"funcionario", "/Coordinador/Aprobadas", "rol=coordinador&bandeja=procesadas"},
                new String[] {"secretaria", "/Secretaria/Aprobadas", "rol=secretaria&bandeja=procesadas"},
                new String[] {"secretaria", "/Secretaria/Aprobadas?periodoAcademico=2025-1",
                        "rol=secretaria&bandeja=procesadas&periodoAcademico=2025-1"},
                new String[] {"est5", "/Secretaria/Aprobadas", "rol=secretaria&bandeja=procesadas"});
        for (String[] c : bandejasPorRol) {
            caso(casos, c[0], HttpMethod.GET, b + bandejas + c[1], null);
            caso(casos, c[0], HttpMethod.GET, b + "?" + c[2], null);
        }

        // Una solicitud por id (se retira)
        caso(casos, "est5", HttpMethod.GET, b + porId + id, null);
        caso(casos, "est6", HttpMethod.GET, b + porId + id, null);
        caso(casos, "funcionario", HttpMethod.GET, b + porId + id, null);

        // Cambio de estado (la solicitud está en ENVIADA)
        String aprobar = "{\"idSolicitud\":" + id + ",\"nuevoEstado\":\"APROBADA_FUNCIONARIO\",\"comentario\":\"ok\"}";
        String noPermitido = "{\"idSolicitud\":" + id + ",\"nuevoEstado\":\"APROBADA\"}";
        String sinEstado = "{\"idSolicitud\":" + id + "}";
        for (String ruta : List.of(b + estado, b + "/" + id + "/estado")) {
            caso(casos, "funcionario", HttpMethod.PUT, ruta, aprobar);
            caso(casos, "coordinador", HttpMethod.PUT, ruta, aprobar);
            caso(casos, "admin", HttpMethod.PUT, ruta, aprobar);
            caso(casos, "funcionario", HttpMethod.PUT, ruta, noPermitido);
            caso(casos, "funcionario", HttpMethod.PUT, ruta, sinEstado);
            caso(casos, "est5", HttpMethod.PUT, ruta, aprobar);
        }

        // Oficios: listado y descarga
        for (String[] c : List.of(new String[] {"/obtenerOficios/", "/oficios"},
                new String[] {"/descargarOficio/", "/oficios/archivo"})) {
            for (String[] quienYId : List.of(new String[] {"est5", String.valueOf(id)},
                    new String[] {"est6", String.valueOf(id)}, new String[] {"funcionario", otra})) {
                caso(casos, quienYId[0], HttpMethod.GET, b + c[0] + quienYId[1], null);
                caso(casos, quienYId[0], HttpMethod.GET, b + "/" + quienYId[1] + c[1], null);
            }
        }

        // Documentos requeridos (se retira)
        caso(casos, "est5", HttpMethod.GET, b + "/validarDocumentosRequeridos/" + id, null);
        caso(casos, "funcionario", HttpMethod.GET, b + "/validarDocumentosRequeridos/" + otra, null);

        // Solo rutas nuevas: lo que la convención decide aparte
        caso(casos, "funcionario", HttpMethod.PUT, b + "/" + id + "/estado",
                "{\"idSolicitud\":" + otra + ",\"nuevoEstado\":\"APROBADA_FUNCIONARIO\"}");
        caso(casos, "funcionario", HttpMethod.PUT, b + "/" + id + "/estado", "{\"nuevoEstado\":\"APROBADA_FUNCIONARIO\"}");
        caso(casos, "funcionario", HttpMethod.PUT, b + estado, "{\"nuevoEstado\":\"APROBADA_FUNCIONARIO\"}");
        caso(casos, "funcionario", HttpMethod.GET, b + "?rol=otro&bandeja=pendientes", null);
        caso(casos, "funcionario", HttpMethod.GET, b + "?rol=funcionario&bandeja=otra", null);
        caso(casos, "funcionario", HttpMethod.GET, b + "?bandeja=pendientes", null);
        caso(casos, "funcionario", HttpMethod.GET, b + "/abc/oficios", null);
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
            case "admin" -> "Administrador";
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

    private static String guardada(Solicitud s) {
        return "usuario " + (s.getObjUsuario() == null ? null : s.getObjUsuario().getIdUsuario()) + ", periodo "
                + s.getPeriodoAcademico() + ", nombre " + s.getNombreSolicitud();
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

    private static <S extends Solicitud> S solicitud(Supplier<S> nueva, int id, Usuario usuario,
            List<Documento> documentos) {
        S s = nueva.get();
        s.setIdSolicitud(id);
        s.setNombreSolicitud("Solicitud " + id);
        s.setPeriodoAcademico("2025-1");
        s.setFechaRegistroSolicitud(FECHA);
        s.setObjUsuario(usuario);
        EstadoSolicitud e = new EstadoSolicitud();
        e.setIdEstado(id * 10);
        e.setEstadoActual("ENVIADA");
        e.setFechaRegistroEstado(FECHA);
        s.setEstadosSolicitud(new ArrayList<>(List.of(e)));
        s.setDocumentos(new ArrayList<>(documentos));
        return s;
    }
}
