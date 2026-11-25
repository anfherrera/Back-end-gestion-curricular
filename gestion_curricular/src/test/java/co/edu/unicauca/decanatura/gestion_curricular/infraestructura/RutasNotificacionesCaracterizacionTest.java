package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import java.nio.charset.StandardCharsets;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Notificacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;

/**
 * Caracterización previa a la migración de rutas de EST-7 (paso 4): notificaciones. Cada operación se pide por su
 * ruta antigua y por la nueva, con la aplicación real (mapper, {@code Autorizacion} y seguridad de método activas) y
 * los casos de uso y el gateway de usuarios simulados. Cada caso indica quién pide: el estudiante 5, el estudiante 6,
 * un funcionario o el decano. Además del estado, el tipo y el cuerpo, guarda las llamadas que cambian datos.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class RutasNotificacionesCaracterizacionTest {

    private static final String CUERPO_CREAR = "{\"tipoSolicitud\":\"REINGRESO\",\"tipoNotificacion\":\"APROBADA\","
            + "\"titulo\":\"Aprobada\",\"mensaje\":\"Su solicitud fue aprobada\",\"idUsuario\":5,"
            + "\"fechaCreacion\":\"2025-03-04T10:00:00.000+00:00\",\"esUrgente\":true}";
    private static final String CUERPO_SIN_TITULO = CUERPO_CREAR.replace("\"titulo\":\"Aprobada\",", "");
    private static final String CUERPO_USUARIO_INEXISTENTE = CUERPO_CREAR.replace("\"idUsuario\":5", "\"idUsuario\":99");

    private final ObjectMapper json = new ObjectMapper();
    private final ArrayNode llamadas = json.createArrayNode();

    @Autowired MockMvc mvc;
    @MockBean GestionarNotificacionCUIntPort notificaciones;
    @MockBean GestionarSolicitudCUIntPort solicitudes;
    @MockBean GestionarUsuarioGatewayIntPort usuarioGateway;
    /** El adaptador del gateway también es el UserDetailsService; al simular el gateway hay que darlo aparte. */
    @MockBean UserDetailsService userDetailsService;

    @BeforeEach
    void preparar() {
        Usuario estudiante5 = usuario(5, "Estudiante");
        Usuario estudiante6 = usuario(6, "Estudiante");
        when(usuarioGateway.buscarUsuarioPorCorreo("est5@prueba.test")).thenReturn(estudiante5);
        when(usuarioGateway.buscarUsuarioPorCorreo("est6@prueba.test")).thenReturn(estudiante6);
        when(usuarioGateway.buscarUsuarioPorCorreo("funcionario@prueba.test")).thenReturn(usuario(1, "Funcionario"));
        when(usuarioGateway.buscarUsuarioPorCorreo("decano@prueba.test")).thenReturn(usuario(2, "Decano"));
        when(usuarioGateway.obtenerUsuarioPorId(5)).thenReturn(estudiante5);
        when(usuarioGateway.obtenerUsuarioPorId(99)).thenReturn(null);

        Notificacion n7 = notificacion(7, estudiante5, false, true);
        Notificacion n8 = notificacion(8, estudiante6, true, false);
        when(notificaciones.obtenerNotificacionPorId(7)).thenReturn(n7);
        when(notificaciones.obtenerNotificacionPorId(8)).thenReturn(n8);
        when(notificaciones.buscarPorUsuario(5)).thenReturn(List.of(n7));
        when(notificaciones.buscarNoLeidasPorUsuario(5)).thenReturn(List.of(n7));
        when(notificaciones.buscarUrgentesPorUsuario(5)).thenReturn(List.of(n7));
        when(notificaciones.contarNoLeidasPorUsuario(5)).thenReturn(3L);
        when(notificaciones.buscarPorTipoSolicitud("REINGRESO")).thenReturn(List.of(n7, n8));
        when(notificaciones.buscarPorSolicitud(12)).thenReturn(List.of(n7));
        when(notificaciones.buscarPorSolicitud(13)).thenReturn(List.of(n8));
        when(notificaciones.buscarPorSolicitud(14)).thenReturn(List.of());
        Solicitud solicitud = new Solicitud();
        solicitud.setIdSolicitud(12);
        when(solicitudes.obtenerSolicitudPorId(anyInt())).thenReturn(solicitud);

        doAnswer(i -> {
            Notificacion n = i.getArgument(0);
            llamadas.addObject().put("crearNotificacion", n.getTitulo() + " para " + n.getObjUsuario().getIdUsuario()
                    + ", leida=" + n.getLeida() + ", urgente=" + n.getEsUrgente());
            n.setIdNotificacion(50);
            return n;
        }).when(notificaciones).crearNotificacion(any());
        doAnswer(i -> {
            llamadas.addObject().put("marcarComoLeida", i.<Integer>getArgument(0));
            return null;
        }).when(notificaciones).marcarComoLeida(any());
        doAnswer(i -> {
            llamadas.addObject().put("marcarTodasComoLeidas", i.<Integer>getArgument(0));
            return null;
        }).when(notificaciones).marcarTodasComoLeidas(any());
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void notificaciones() throws Exception {
        String b = "/api/notificaciones";
        List<String> todos = List.of("est5", "est6", "funcionario", "decano");
        ObjectNode casos = json.createObjectNode();

        // Crear
        for (String ruta : List.of(b + "/crear", b)) {
            for (String quien : todos) {
                caso(casos, quien, HttpMethod.POST, ruta, CUERPO_CREAR);
            }
            caso(casos, "funcionario", HttpMethod.POST, ruta, CUERPO_SIN_TITULO);
            caso(casos, "funcionario", HttpMethod.POST, ruta, CUERPO_USUARIO_INEXISTENTE);
        }

        // Consultas por usuario: antiguas y nuevas
        List<String> porUsuario = List.of(
                b + "/usuario/5", b + "?usuario=5",
                b + "/usuario/5/no-leidas", b + "?usuario=5&leida=false",
                b + "/usuario/5/urgentes", b + "?usuario=5&urgente=true",
                b + "/usuario/5/contar-no-leidas", b + "/conteo?usuario=5&leida=false");
        for (String ruta : porUsuario) {
            for (String quien : todos) {
                caso(casos, quien, HttpMethod.GET, ruta, null);
            }
        }
        for (String ruta : List.of(b + "/usuario/0", b + "?usuario=0", b + "/usuario/abc", b + "?usuario=abc",
                b + "/usuario/0/contar-no-leidas", b + "/conteo?usuario=0&leida=false")) {
            caso(casos, "funcionario", HttpMethod.GET, ruta, null);
        }

        // Por tipo de solicitud y por solicitud
        for (String ruta : List.of(b + "/tipo-solicitud/REINGRESO", b + "?tipoSolicitud=REINGRESO")) {
            for (String quien : todos) {
                caso(casos, quien, HttpMethod.GET, ruta, null);
            }
        }
        for (String id : List.of("12", "13", "14", "0")) {
            caso(casos, "est5", HttpMethod.GET, b + "/solicitud/" + id, null);
            caso(casos, "est5", HttpMethod.GET, b + "?solicitud=" + id, null);
        }
        caso(casos, "decano", HttpMethod.GET, b + "/solicitud/12", null);
        caso(casos, "decano", HttpMethod.GET, b + "?solicitud=12", null);

        // Por id
        for (String id : List.of("7", "8", "9", "0", "abc")) {
            caso(casos, "est5", HttpMethod.GET, b + "/" + id, null);
        }
        caso(casos, "funcionario", HttpMethod.GET, b + "/8", null);
        caso(casos, "decano", HttpMethod.GET, b + "/7", null);

        // Marcar como leída
        for (String id : List.of("7", "8", "9", "0")) {
            caso(casos, "est5", HttpMethod.PUT, b + "/" + id + "/marcar-leida", null);
            caso(casos, "est5", HttpMethod.PUT, b + "/" + id + "/leida", null);
        }
        caso(casos, "funcionario", HttpMethod.PUT, b + "/8/marcar-leida", null);
        caso(casos, "funcionario", HttpMethod.PUT, b + "/8/leida", null);
        caso(casos, "decano", HttpMethod.PUT, b + "/7/marcar-leida", null);
        caso(casos, "decano", HttpMethod.PUT, b + "/7/leida", null);
        for (String quien : todos) {
            caso(casos, quien, HttpMethod.PUT, b + "/usuario/5/marcar-todas-leidas", null);
            caso(casos, quien, HttpMethod.PUT, b + "/leidas?usuario=5", null);
        }
        caso(casos, "funcionario", HttpMethod.PUT, b + "/usuario/0/marcar-todas-leidas", null);
        caso(casos, "funcionario", HttpMethod.PUT, b + "/leidas?usuario=0", null);

        // Solo rutas nuevas: filtros que faltan o no se pueden combinar
        for (String ruta : List.of(b, b + "?leida=false", b + "?usuario=5&leida=true", b + "?usuario=5&urgente=false",
                b + "?usuario=5&leida=false&urgente=true", b + "?usuario=5&solicitud=12",
                b + "?tipoSolicitud=REINGRESO&leida=false", b + "/conteo?usuario=5", b + "/conteo",
                b + "/conteo?usuario=5&leida=true")) {
            caso(casos, "funcionario", HttpMethod.GET, ruta, null);
        }
        caso(casos, "funcionario", HttpMethod.PUT, b + "/leidas", null);

        // EST-7: las rutas nuevas responden como las antiguas, que pasan a 404
        GrafoObjetos.compararConReferencia(casos, "rutas/notificaciones.json",
                "rutas/correcciones/notificaciones-est7.json");
    }

    private void caso(ObjectNode casos, String quien, HttpMethod metodo, String ruta, String cuerpo) throws Exception {
        llamadas.removeAll();
        String rol = switch (quien) {
            case "funcionario" -> "Funcionario";
            case "decano" -> "Decano";
            default -> "Estudiante";
        };
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                quien + "@prueba.test", "x", List.of(new SimpleGrantedAuthority("ROLE_" + rol))));
        var peticion = request(metodo, ruta);
        if (cuerpo != null) {
            peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
        }
        var respuesta = mvc.perform(peticion).andReturn().getResponse();
        SecurityContextHolder.clearContext();
        ObjectNode caso = json.createObjectNode();
        caso.put("status", respuesta.getStatus());
        caso.put("tipo", respuesta.getContentType());
        String texto = respuesta.getContentAsString(StandardCharsets.UTF_8);
        JsonNode valor = texto.isEmpty() ? json.nullNode() : json.readTree(texto);
        caso.set("cuerpo", valor);
        caso.set("llamadas", llamadas.deepCopy());
        casos.set(metodo + " " + ruta + (cuerpo == null ? "" : " " + cuerpo) + " [" + quien + "]", caso);
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

    private static Notificacion notificacion(int id, Usuario usuario, boolean leida, boolean urgente) {
        Notificacion n = new Notificacion();
        n.setIdNotificacion(id);
        n.setTipoSolicitud("REINGRESO");
        n.setTipoNotificacion("APROBADA");
        n.setTitulo("Título " + id);
        n.setMensaje("Mensaje " + id);
        n.setFechaCreacion(new Date(1740000000000L));
        n.setLeida(leida);
        n.setEsUrgente(urgente);
        n.setObjUsuario(usuario);
        return n;
    }
}
