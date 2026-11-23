package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.CupoAgotadoException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.CondicionSolicitudVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;

/**
 * Caracterización previa a la migración de rutas de EST-7 (paso 8b): preinscripciones e inscripciones de cursos de
 * verano. Cada operación se pide por su ruta antigua y por la nueva, con la aplicación real (mappers y seguridad de
 * método activas) y los casos de uso, los archivos y los gateways de usuarios y solicitudes simulados. Pide el
 * estudiante 5 (dueño de la preinscripción 91 y de las inscripciones 92 a 95), el estudiante 6, un funcionario o un
 * administrador. Guarda el estado, el tipo, el cuerpo (el texto, si no es JSON), el {@code Content-Disposition} y
 * las llamadas a los casos de uso, con el período actual como marcador.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class RutasPreinscripcionesInscripcionesCaracterizacionTest {

    private static final Date FECHA = new Date(1751371200000L);
    private static final String VIEJA = "/api/cursos-intersemestrales";
    private static final String NUEVA = "/api/cursos-verano";

    private final ObjectMapper json = new ObjectMapper();
    private final ArrayNode llamadas = json.createArrayNode();

    @Autowired MockMvc mvc;
    @MockBean GestionarSolicitudCursoVeranoCUIntPort solicitudCU;
    @MockBean GestionarCursoOfertadoVeranoCUIntPort cursoCU;
    @MockBean GestionarSolicitudCUIntPort solicitudGeneralCU;
    @MockBean GestionarArchivosCUIntPort archivos;
    @MockBean GestionarSolicitudGatewayIntPort solicitudGateway;
    @MockBean GestionarUsuarioGatewayIntPort usuarioGateway;
    /** El adaptador del gateway también es el UserDetailsService; al simular el gateway hay que darlo aparte. */
    @MockBean UserDetailsService userDetailsService;

    @BeforeEach
    void preparar() throws Exception {
        Usuario estudiante5 = usuario(5, "Estudiante");
        when(usuarioGateway.buscarUsuarioPorCorreo("est5@prueba.test")).thenReturn(estudiante5);
        when(usuarioGateway.buscarUsuarioPorCorreo("est6@prueba.test")).thenReturn(usuario(6, "Estudiante"));
        when(usuarioGateway.buscarUsuarioPorCorreo("funcionario@prueba.test")).thenReturn(usuario(1, "Funcionario"));
        when(usuarioGateway.buscarUsuarioPorCorreo("admin@prueba.test")).thenReturn(usuario(7, "Administrador"));
        for (int id = 91; id <= 95; id++) {
            when(solicitudGateway.idDuenoDeSolicitud(id)).thenReturn(5);
        }

        CursoOfertadoVerano curso = new CursoOfertadoVerano();
        curso.setIdCurso(71);
        Materia materia = new Materia();
        materia.setIdMateria(1);
        materia.setCodigo("MAT-1");
        materia.setNombre("Calculo");
        curso.setObjMateria(materia);
        curso.setPeriodoAcademico("2025-2");
        curso.setFecha_inicio(FECHA);

        // Preinscripciones
        SolicitudCursoVeranoPreinscripcion p91 = new SolicitudCursoVeranoPreinscripcion();
        llenar(p91, 91, estudiante5, curso, "Aprobado");
        p91.setCondicionSolicitud(CondicionSolicitudVerano.Primera_Vez);
        p91.setObservacion("Observacion 91");
        when(solicitudCU.buscarSolicitudesPorUsuario(anyInt())).thenAnswer(i ->
                i.<Integer>getArgument(0) == 5 ? List.of(p91) : List.of());
        when(solicitudCU.buscarPreinscripcionesPorCurso(anyInt())).thenAnswer(i ->
                i.<Integer>getArgument(0) == 71 ? List.of(p91) : List.of());
        when(solicitudCU.buscarPreinscripcionPorUsuarioYCurso(anyInt(), anyInt())).thenAnswer(i ->
                i.<Integer>getArgument(1) == 72 ? p91 : null);
        doAnswer(i -> {
            SolicitudCursoVeranoPreinscripcion s = i.getArgument(0);
            llamadas.addObject().put("crearSolicitudCursoVeranoPreinscripcion", "usuario "
                    + s.getObjUsuario().getIdUsuario() + ", curso " + s.getObjCursoOfertadoVerano().getIdCurso()
                    + ", condicion " + s.getCondicionSolicitud() + ", periodo " + conMarcador(s.getPeriodoAcademico())
                    + ", nombre " + s.getNombreSolicitud() + ", observacion " + s.getObservacion());
            SolicitudCursoVeranoPreinscripcion guardada = new SolicitudCursoVeranoPreinscripcion();
            guardada.setIdSolicitud(96);
            guardada.setFechaRegistroSolicitud(FECHA);
            return guardada;
        }).when(solicitudCU).crearSolicitudCursoVeranoPreinscripcion(any());
        when(solicitudCU.buscarSolicitudPorId(anyInt())).thenAnswer(i -> i.<Integer>getArgument(0) == 91 ? p91 : null);
        doAnswer(i -> {
            SolicitudCursoVeranoPreinscripcion s = i.getArgument(0);
            llamadas.addObject().put("actualizarObservacionesPreinscripcion", s.getIdSolicitud() + ", " + s.getObservacion());
            return s;
        }).when(solicitudCU).actualizarObservacionesPreinscripcion(any());
        doAnswer(i -> {
            llamadas.addObject().put("aprobarPreinscripcion", i.getArgument(0) + ", " + i.getArgument(1));
            if (i.<Integer>getArgument(0) == 99) {
                throw new EntidadNoExisteException("Preinscripcion no encontrada");
            }
            return p91;
        }).when(solicitudCU).aprobarPreinscripcion(any(), any());
        doAnswer(i -> {
            llamadas.addObject().put("rechazarPreinscripcion", i.getArgument(0) + ", " + i.getArgument(1));
            return p91;
        }).when(solicitudCU).rechazarPreinscripcion(any(), any());

        // Inscripciones: 92 enviada, 93 enviada sin cupo, 94 con el pago validado y 95 con el pago rechazado
        SolicitudCursoVeranoInscripcion i92 = inscripcion(92, estudiante5, curso, "Enviada");
        i92.setDocumentos(new ArrayList<>(List.of(documento())));
        SolicitudCursoVeranoInscripcion i93 = inscripcion(93, estudiante5, curso, "Enviada");
        SolicitudCursoVeranoInscripcion i94 = inscripcion(94, estudiante5, curso, "Pago_Validado");
        SolicitudCursoVeranoInscripcion i95 = inscripcion(95, estudiante5, curso, "Pago_Rechazado");
        when(solicitudCU.buscarPorIdInscripcion(anyInt())).thenAnswer(i -> switch (i.<Integer>getArgument(0)) {
            case 92 -> i92;
            case 93 -> i93;
            case 94 -> i94;
            case 95 -> i95;
            default -> null;
        });
        when(solicitudCU.buscarInscripcionesPorCurso(anyInt())).thenReturn(List.of(i92, i94));
        when(solicitudCU.buscarPreinscripcionAprobada(anyInt(), anyInt())).thenAnswer(i ->
                i.<Integer>getArgument(1) == 71 ? p91 : null);
        when(solicitudCU.tieneInscripcionActiva(anyInt(), anyInt())).thenReturn(false);
        doAnswer(i -> {
            SolicitudCursoVeranoInscripcion s = i.getArgument(0);
            llamadas.addObject().put("crearSolicitudCursoVeranoInscripcion", "usuario "
                    + s.getObjUsuario().getIdUsuario() + ", curso " + s.getObjCursoOfertadoVerano().getIdCurso()
                    + ", nombre " + s.getNombreSolicitud());
            SolicitudCursoVeranoInscripcion guardada = new SolicitudCursoVeranoInscripcion();
            guardada.setIdSolicitud(97);
            guardada.setFechaRegistroSolicitud(FECHA);
            return guardada;
        }).when(solicitudCU).crearSolicitudCursoVeranoInscripcion(any());
        doAnswer(i -> {
            if (i.<SolicitudCursoVeranoInscripcion>getArgument(0).getIdSolicitud() == 93) {
                throw new CupoAgotadoException(10, 10);
            }
            return null;
        }).when(solicitudCU).verificarCupoParaAceptar(any());
        doAnswer(i -> {
            llamadas.addObject().put("validarPago", i.getArgument(0) + ", " + i.getArgument(1) + ", " + i.getArgument(2));
            SolicitudCursoVeranoInscripcion s = inscripcion(i.getArgument(0), estudiante5, curso,
                    i.<Boolean>getArgument(1) ? "Pago_Validado" : "Pago_Rechazado");
            return s;
        }).when(solicitudCU).validarPago(anyInt(), anyBoolean(), any());
        when(solicitudCU.contarInscripcionesPorEstado(anyInt(), anyString())).thenReturn(1);
        doAnswer(i -> {
            llamadas.addObject().put("asociarEstudiante", i.getArgument(0) + ", " + i.getArgument(1));
            return true;
        }).when(cursoCU).asociarEstudiante(any(), any());
        when(solicitudGeneralCU.listarSolicitudes()).thenReturn(List.<Solicitud>of(i92));
        when(solicitudGeneralCU.obtenerSolicitudPorId(any())).thenAnswer(i -> i.<Integer>getArgument(0) == 92 ? i92 : null);
        when(archivos.getFileByPath(anyString())).thenReturn("%PDF comprobante".getBytes(StandardCharsets.US_ASCII));
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void preinscripciones() throws Exception {
        ObjectNode casos = json.createObjectNode();

        // Preinscripciones de un usuario
        for (String[] quienYId : List.of(new String[] {"est5", "5"}, new String[] {"est5", "6"},
                new String[] {"funcionario", "5"}, new String[] {"funcionario", "0"})) {
            caso(casos, quienYId[0], HttpMethod.GET, VIEJA + "/preinscripciones/usuario/" + quienYId[1], null);
            caso(casos, quienYId[0], HttpMethod.GET, NUEVA + "/preinscripciones?usuario=" + quienYId[1], null);
        }

        // Preinscripciones de un curso
        for (String quien : List.of("funcionario", "est5")) {
            caso(casos, quien, HttpMethod.GET, VIEJA + "/preinscripciones/curso/71", null);
            caso(casos, quien, HttpMethod.GET, NUEVA + "/preinscripciones?curso=71", null);
        }

        // Crear una preinscripción
        String valida = "{\"idUsuario\":5,\"idCurso\":71,\"nombreSolicitud\":\"Pre\",\"condicion\":\"Repitencia\"}";
        String duplicada = "{\"idUsuario\":5,\"idCurso\":72,\"nombreSolicitud\":\"Pre\"}";
        String sinCurso = "{\"idUsuario\":5}";
        String deOtro = "{\"idUsuario\":6,\"idCurso\":71}";
        for (String ruta : List.of(VIEJA + "/cursos-verano/preinscripciones", NUEVA + "/preinscripciones")) {
            caso(casos, "est5", HttpMethod.POST, ruta, valida);
            caso(casos, "est5", HttpMethod.POST, ruta, duplicada);
            caso(casos, "est5", HttpMethod.POST, ruta, sinCurso);
            caso(casos, "est5", HttpMethod.POST, ruta, deOtro);
            caso(casos, "funcionario", HttpMethod.POST, ruta, deOtro);
        }

        // Solicitud de curso nuevo
        String nuevo = "{\"nombreCompleto\":\"Ana Ruiz\",\"codigo\":\"104512\",\"curso\":\"Calculo\",\"idUsuario\":5,"
                + "\"condicion\":";
        for (String ruta : List.of(VIEJA + "/solicitudes-curso-nuevo", NUEVA + "/solicitudes-curso-nuevo")) {
            caso(casos, "est5", HttpMethod.POST, ruta, nuevo + "\"Primera_Vez\"}");
            caso(casos, "est5", HttpMethod.POST, ruta, nuevo + "\"Rara\"}");
            caso(casos, "est6", HttpMethod.POST, ruta, nuevo + "\"Primera_Vez\"}");
        }

        // Observaciones
        for (String base : List.of(VIEJA, NUEVA)) {
            caso(casos, "funcionario", HttpMethod.PUT, base + "/preinscripciones/91/observaciones", "{\"observaciones\":\"nota\"}");
            caso(casos, "funcionario", HttpMethod.PUT, base + "/preinscripciones/91/observaciones", "{\"observaciones\":\" \"}");
            caso(casos, "funcionario", HttpMethod.PUT, base + "/preinscripciones/99/observaciones", "{\"observaciones\":\"nota\"}");
            caso(casos, "est5", HttpMethod.PUT, base + "/preinscripciones/91/observaciones", "{\"observaciones\":\"nota\"}");
        }

        // Aprobar y rechazar
        String v = VIEJA + "/preinscripciones/";
        String n = NUEVA + "/preinscripciones/";
        List<String[]> decisiones = List.of(
                new String[] {"funcionario", "91/aprobar", "{\"comentarios\":\"ok\"}", "{\"nuevoEstado\":\"aprobada\",\"comentarios\":\"ok\"}"},
                new String[] {"admin", "91/aprobar", "{}", "{\"nuevoEstado\":\"aprobada\"}"},
                new String[] {"funcionario", "99/aprobar", "{}", "{\"nuevoEstado\":\"aprobada\"}"},
                new String[] {"est5", "91/aprobar", "{}", "{\"nuevoEstado\":\"aprobada\"}"},
                new String[] {"funcionario", "91/rechazar", "{\"motivo\":\"  sin cupo  \"}", "{\"nuevoEstado\":\"rechazada\",\"motivo\":\"  sin cupo  \"}"},
                new String[] {"funcionario", "91/rechazar", "{\"motivo\":\" \"}", "{\"nuevoEstado\":\"rechazada\",\"motivo\":\" \"}"},
                new String[] {"est5", "91/rechazar", "{\"motivo\":\"no\"}", "{\"nuevoEstado\":\"rechazada\",\"motivo\":\"no\"}"});
        for (String[] d : decisiones) {
            String id = d[1].substring(0, d[1].indexOf('/'));
            caso(casos, d[0], HttpMethod.PUT, v + d[1], d[2]);
            caso(casos, d[0], HttpMethod.PUT, n + id + "/estado", d[3]);
        }
        caso(casos, "funcionario", HttpMethod.PUT, v + "91/aprobar", null);

        // Se retiran: las solicitudes de un estudiante y los dos alias
        caso(casos, "est5", HttpMethod.GET, VIEJA + "/estudiante/5", null);
        caso(casos, "funcionario", HttpMethod.GET, VIEJA + "/estudiante/5", null);
        caso(casos, "est5", HttpMethod.GET, VIEJA + "/solicitudes/preinscripcion/estudiante/5", null);
        caso(casos, "est5", HttpMethod.POST, VIEJA + "/solicitudes/preinscripcion", valida);

        // Solo rutas nuevas: lo que la convención decide aparte
        caso(casos, "funcionario", HttpMethod.GET, n.substring(0, n.length() - 1), null);
        caso(casos, "funcionario", HttpMethod.GET, n.substring(0, n.length() - 1) + "?usuario=5&curso=71", null);
        caso(casos, "funcionario", HttpMethod.PUT, n + "91/estado", "{\"nuevoEstado\":\"otro\"}");
        caso(casos, "funcionario", HttpMethod.PUT, n + "91/estado", "{\"comentarios\":\"ok\"}");
        caso(casos, "funcionario", HttpMethod.PUT, n + "91/estado", null);
        caso(casos, "funcionario", HttpMethod.PUT, n + "abc/estado", "{\"nuevoEstado\":\"aprobada\"}");
        caso(casos, "funcionario", HttpMethod.PUT, n + "abc/observaciones", "{\"observaciones\":\"nota\"}");

        // EST-7: las rutas nuevas responden como las antiguas, que pasan a 404
        GrafoObjetos.compararConReferencia(casos, "rutas/preinscripciones-verano.json");
    }

    @Test
    void inscripciones() throws Exception {
        ObjectNode casos = json.createObjectNode();

        // Crear una inscripción
        String valida = "{\"idUsuario\":5,\"idCurso\":71,\"nombreSolicitud\":\"Ins\"}";
        String sinPreinscripcion = "{\"idUsuario\":5,\"idCurso\":72,\"nombreSolicitud\":\"Ins\"}";
        String deOtro = "{\"idUsuario\":6,\"idCurso\":71}";
        for (String ruta : List.of(VIEJA + "/cursos-verano/inscripciones", NUEVA + "/inscripciones")) {
            caso(casos, "est5", HttpMethod.POST, ruta, valida);
            caso(casos, "est5", HttpMethod.POST, ruta, sinPreinscripcion);
            caso(casos, "est5", HttpMethod.POST, ruta, deOtro);
            caso(casos, "funcionario", HttpMethod.POST, ruta, valida);
        }

        // Estudiantes elegibles de un curso
        for (String quien : List.of("funcionario", "est5")) {
            caso(casos, quien, HttpMethod.GET, VIEJA + "/inscripciones/estudiantes-elegibles/71", null);
            caso(casos, quien, HttpMethod.GET, NUEVA + "/cursos/71/estudiantes-elegibles", null);
        }

        // Aceptar (valida el pago) y rechazar
        String v = VIEJA + "/inscripciones/";
        String n = NUEVA + "/inscripciones/";
        List<String[]> decisiones = List.of(
                new String[] {"funcionario", "92/aceptar", "{\"observaciones\":\"pago ok\"}", "{\"nuevoEstado\":\"aceptada\",\"observaciones\":\"pago ok\"}"},
                new String[] {"admin", "92/aceptar", "{}", "{\"nuevoEstado\":\"aceptada\"}"},
                new String[] {"funcionario", "93/aceptar", "{}", "{\"nuevoEstado\":\"aceptada\"}"},
                new String[] {"funcionario", "99/aceptar", "{}", "{\"nuevoEstado\":\"aceptada\"}"},
                new String[] {"est5", "92/aceptar", "{}", "{\"nuevoEstado\":\"aceptada\"}"},
                new String[] {"funcionario", "92/rechazar", "{\"motivo\":\"  sin pago  \"}", "{\"nuevoEstado\":\"rechazada\",\"motivo\":\"  sin pago  \"}"},
                new String[] {"funcionario", "94/rechazar", "{}", "{\"nuevoEstado\":\"rechazada\"}"},
                new String[] {"funcionario", "95/rechazar", "{}", "{\"nuevoEstado\":\"rechazada\"}"},
                new String[] {"funcionario", "99/rechazar", "{}", "{\"nuevoEstado\":\"rechazada\"}"},
                new String[] {"est5", "92/rechazar", "{}", "{\"nuevoEstado\":\"rechazada\"}"});
        for (String[] d : decisiones) {
            String id = d[1].substring(0, d[1].indexOf('/'));
            caso(casos, d[0], HttpMethod.PUT, v + d[1], d[2]);
            caso(casos, d[0], HttpMethod.PUT, n + id + "/estado", d[3]);
        }

        // Se retiran: listado, cancelación, confirmación, información, estadísticas y comprobante
        caso(casos, "funcionario", HttpMethod.GET, VIEJA + "/inscripciones", null);
        caso(casos, "est5", HttpMethod.DELETE, VIEJA + "/inscripciones/92", null);
        caso(casos, "est5", HttpMethod.PUT, VIEJA + "/inscripciones/92/confirmar", null);
        caso(casos, "est5", HttpMethod.GET, VIEJA + "/inscripciones/92/info", null);
        caso(casos, "funcionario", HttpMethod.GET, VIEJA + "/inscripciones/curso/71/estadisticas", null);
        caso(casos, "est5", HttpMethod.GET, VIEJA + "/inscripciones/92/comprobante", null);

        // Solo rutas nuevas: lo que la convención decide aparte
        caso(casos, "funcionario", HttpMethod.PUT, n + "92/estado", "{\"nuevoEstado\":\"otro\"}");
        caso(casos, "funcionario", HttpMethod.PUT, n + "92/estado", "{\"observaciones\":\"pago ok\"}");
        caso(casos, "funcionario", HttpMethod.PUT, n + "abc/estado", "{\"nuevoEstado\":\"aceptada\"}");
        caso(casos, "funcionario", HttpMethod.GET, NUEVA + "/cursos/abc/estudiantes-elegibles", null);

        // EST-7: las rutas nuevas responden como las antiguas, que pasan a 404
        GrafoObjetos.compararConReferencia(casos, "rutas/inscripciones-verano.json");
    }

    private void caso(ObjectNode casos, String quien, HttpMethod metodo, String ruta, String cuerpo) throws Exception {
        MockHttpServletRequestBuilder peticion = request(metodo, ruta);
        if (cuerpo != null) {
            peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
        }
        pedir(casos, quien, peticion, metodo + " " + ruta + (cuerpo == null ? "" : " " + cuerpo) + " [" + quien + "]");
    }

    private void pedir(ObjectNode casos, String quien, MockHttpServletRequestBuilder peticion, String clave)
            throws Exception {
        llamadas.removeAll();
        String rol = switch (quien) {
            case "funcionario" -> "Funcionario";
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
        caso.set("cuerpo", sinFechasDeAhora(valor));
        caso.put("disposicion", respuesta.getHeader(HttpHeaders.CONTENT_DISPOSITION));
        caso.set("llamadas", llamadas.deepCopy());
        casos.set(clave, caso);
    }

    /** Las respuestas de aceptar, rechazar y las estadísticas llevan la fecha de la petición: se sustituye por un marcador. */
    private static JsonNode sinFechasDeAhora(JsonNode valor) {
        if (valor instanceof ObjectNode objeto) {
            for (String campo : List.of("fecha_aceptacion", "fecha_rechazo", "fechaConsulta", "fecha_consulta", "timestamp")) {
                if (objeto.has(campo) && !objeto.get(campo).isNull()) {
                    objeto.put(campo, "<ahora>");
                }
            }
        }
        return valor;
    }

    /** El período actual depende de la fecha: se sustituye por un marcador. */
    private static String conMarcador(String periodo) {
        PeriodoAcademicoEnum actual = PeriodoAcademicoEnum.getPeriodoActual();
        return actual != null && actual.getValor().equals(periodo) ? "<ACTUAL>" : periodo;
    }

    private static Usuario usuario(int id, String rol) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setNombreCompleto("Usuario " + id);
        usuario.setCodigo("10" + id);
        usuario.setCorreo("est" + id + "@unicauca.edu.co");
        Rol objRol = new Rol();
        objRol.setIdRol(1);
        objRol.setNombre(rol);
        usuario.setObjRol(objRol);
        return usuario;
    }

    private static Documento documento() {
        Documento d = new Documento();
        d.setIdDocumento(80);
        d.setNombre("comprobante.pdf");
        d.setRutaDocumento("verano/92/comprobante.pdf");
        d.setFechaDocumento(FECHA);
        d.setEsValido(true);
        return d;
    }

    private static SolicitudCursoVeranoInscripcion inscripcion(int id, Usuario usuario, CursoOfertadoVerano curso,
            String estado) {
        SolicitudCursoVeranoInscripcion s = new SolicitudCursoVeranoInscripcion();
        llenar(s, id, usuario, curso, estado);
        s.setCondicionSolicitud(CondicionSolicitudVerano.Primera_Vez);
        s.setDocumentos(new ArrayList<>());
        return s;
    }

    private static void llenar(Solicitud s, int id, Usuario usuario, CursoOfertadoVerano curso, String estado) {
        s.setIdSolicitud(id);
        s.setNombreSolicitud("Solicitud " + id);
        s.setPeriodoAcademico("2025-2");
        s.setFechaRegistroSolicitud(FECHA);
        s.setObjUsuario(usuario);
        if (s instanceof SolicitudCursoVeranoPreinscripcion p) {
            p.setObjCursoOfertadoVerano(curso);
        } else if (s instanceof SolicitudCursoVeranoInscripcion i) {
            i.setObjCursoOfertadoVerano(curso);
        }
        EstadoSolicitud e = new EstadoSolicitud();
        e.setIdEstado(id * 10);
        e.setEstadoActual(estado);
        e.setFechaRegistroEstado(FECHA);
        e.setComentario("Comentario " + id);
        s.setEstadosSolicitud(new ArrayList<>(List.of(e)));
    }
}
