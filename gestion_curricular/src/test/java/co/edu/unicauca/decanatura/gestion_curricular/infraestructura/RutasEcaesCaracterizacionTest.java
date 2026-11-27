package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudEcaesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FechaEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.TipoDocumento;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;

/**
 * Caracterización previa a la migración de rutas de EST-7 (paso 6): solicitudes y fechas de ECAES. Cada operación
 * se pide por su ruta antigua y por la nueva, con la aplicación real (mappers, {@code Autorizacion}, validador de
 * cambios de estado y seguridad de método activos) y el caso de uso y los gateways de usuarios y solicitudes
 * simulados. Cada caso indica quién pide: el estudiante 5 (dueño de la solicitud 20), el estudiante 6 (dueño de la
 * 21), un funcionario o un administrador. Además del estado, el tipo y el cuerpo, guarda las llamadas al caso de
 * uso, con el período actual sustituido por {@code <ACTUAL>}.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class RutasEcaesCaracterizacionTest {

    private static final String CUERPO_CREAR = "{\"nombre_solicitud\":\"Solicitud de ECAES\","
            + "\"fecha_registro_solicitud\":\"2025-01-19T00:00:00.000Z\",\"tipoDocumento\":\"CC\","
            + "\"numero_documento\":\"1020101234\",\"fecha_expedicion\":\"2020-01-15T00:00:00.000Z\","
            + "\"fecha_nacimiento\":\"2000-05-20T00:00:00.000Z\",\"objUsuario\":{\"id_usuario\":6,"
            + "\"nombre_completo\":\"Usuario 6\",\"codigo\":\"102010\",\"correo\":\"est6@unicauca.edu.co\","
            + "\"id_rol\":1,\"id_programa\":1}}";
    /** Solo viola @Size (BUG-47). */
    private static final String CUERPO_DOCUMENTO_CORTO = CUERPO_CREAR.replace("1020101234", "123");
    private static final String CUERPO_FECHAS = "{\"periodoAcademico\":\"2025-1\","
            + "\"inscripcion_est_by_facultad\":\"2025-02-01\",\"registro_recaudo_ordinario\":\"2025-02-10\","
            + "\"registro_recaudo_extraordinario\":\"2025-02-20\",\"citacion\":\"2025-03-01\","
            + "\"aplicacion\":\"2025-03-15\",\"resultados_individuales\":\"2025-04-30\"}";

    private final ObjectMapper json = new ObjectMapper();
    private final ArrayNode llamadas = json.createArrayNode();

    @Autowired MockMvc mvc;
    @MockitoBean GestionarSolicitudEcaesCUIntPort ecaes;
    @MockitoBean GestionarSolicitudGatewayIntPort solicitudGateway;
    @MockitoBean GestionarUsuarioGatewayIntPort usuarioGateway;
    /** El adaptador del gateway también es el UserDetailsService; al simular el gateway hay que darlo aparte. */
    @MockitoBean UserDetailsService userDetailsService;

    @BeforeEach
    void preparar() {
        Usuario estudiante5 = usuario(5, "Estudiante");
        Usuario estudiante6 = usuario(6, "Estudiante");
        when(usuarioGateway.buscarUsuarioPorCorreo("est5@prueba.test")).thenReturn(estudiante5);
        when(usuarioGateway.buscarUsuarioPorCorreo("est6@prueba.test")).thenReturn(estudiante6);
        when(usuarioGateway.buscarUsuarioPorCorreo("funcionario@prueba.test")).thenReturn(usuario(1, "Funcionario"));
        when(usuarioGateway.buscarUsuarioPorCorreo("admin@prueba.test")).thenReturn(usuario(3, "Administrador"));
        when(solicitudGateway.idDuenoDeSolicitud(20)).thenReturn(5);
        when(solicitudGateway.idDuenoDeSolicitud(21)).thenReturn(6);

        SolicitudEcaes s20 = solicitud(20, estudiante5, "ENVIADA");
        SolicitudEcaes s21 = solicitud(21, estudiante6, "PRE_REGISTRADO");
        when(ecaes.buscarPorId(20)).thenReturn(s20);
        when(ecaes.buscarPorId(21)).thenReturn(s21);
        when(ecaes.buscarPorId(99)).thenThrow(new EntidadNoExisteException("Solicitud no encontrada con ID: 99"));
        when(ecaes.listarSolicitudes()).thenReturn(List.of(s20, s21));
        doAnswer(i -> {
            llamadas.addObject().put("listar", conMarcador(i.getArgument(0).toString()));
            return List.of(s20);
        }).when(ecaes).listar(any());
        doAnswer(i -> {
            llamadas.addObject().put("listarSolicitudesPorRol", i.getArgument(0) + ", " + i.getArgument(1));
            return List.of(s21);
        }).when(ecaes).listarSolicitudesPorRol(anyString(), any());
        doAnswer(i -> {
            SolicitudEcaes s = i.getArgument(0);
            llamadas.addObject().put("guardar", "usuario " + s.getObjUsuario().getIdUsuario() + ", documento "
                    + s.getNumeroDocumento() + ", tipo " + s.getTipoDocumento());
            s.setIdSolicitud(30);
            return s;
        }).when(ecaes).guardar(any());
        doAnswer(i -> {
            llamadas.addObject().put("cambiarEstadoSolicitud", i.getArgument(0) + " -> " + i.getArgument(1));
            return null;
        }).when(ecaes).cambiarEstadoSolicitud(any(), any());

        FechaEcaes f2025 = fechas(2, "2025-1");
        when(ecaes.listarFechasEcaes()).thenReturn(List.of(f2025));
        when(ecaes.buscarFechasPorPeriodo(anyString())).thenReturn(Optional.empty());
        when(ecaes.buscarFechasPorPeriodo("2025-1")).thenReturn(Optional.of(f2025));
        doAnswer(i -> {
            FechaEcaes f = i.getArgument(0);
            llamadas.addObject().put("publicarFechasEcaes", f.getPeriodoAcademico() + ", citacion " + f.getCitacion());
            f.setIdFechaEcaes(3);
            return f;
        }).when(ecaes).publicarFechasEcaes(any());
        doAnswer(i -> {
            FechaEcaes f = i.getArgument(0);
            llamadas.addObject().put("actualizarFechasEcaes", f.getPeriodoAcademico() + ", citacion " + f.getCitacion());
            f.setIdFechaEcaes(2);
            return f;
        }).when(ecaes).actualizarFechasEcaes(any());
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ecaes() throws Exception {
        String b = "/api/solicitudes-ecaes";
        ObjectNode casos = json.createObjectNode();

        // Crear
        for (String ruta : List.of(b + "/crearSolicitud-Ecaes", b)) {
            for (String quien : List.of("est5", "funcionario", "admin")) {
                caso(casos, quien, HttpMethod.POST, ruta, CUERPO_CREAR);
            }
            caso(casos, "est5", HttpMethod.POST, ruta, CUERPO_DOCUMENTO_CORTO);
        }

        // Todas
        for (String ruta : List.of(b + "/listarSolicitudes-Ecaes", b)) {
            caso(casos, "funcionario", HttpMethod.GET, ruta, null);
            caso(casos, "est5", HttpMethod.GET, ruta, null);
        }

        // Bandeja del funcionario
        for (String ruta : List.of(b + "/listarSolicitudes-Ecaes/Funcionario",
                b + "?rol=funcionario&bandeja=pendientes")) {
            caso(casos, "funcionario", HttpMethod.GET, ruta, null);
            caso(casos, "funcionario", HttpMethod.GET, ruta + (ruta.contains("?") ? "&" : "?")
                    + "periodoAcademico=2025-1", null);
            caso(casos, "est5", HttpMethod.GET, ruta, null);
        }

        // Por rol
        List<String[]> porRol = List.of(
                new String[] {"est5", "rol=FUNCIONARIO"},
                new String[] {"est5", "rol=ESTUDIANTE"},
                new String[] {"est5", "rol=ESTUDIANTE&periodoAcademico=2025-1"},
                new String[] {"funcionario", "rol=FUNCIONARIO"},
                new String[] {"funcionario", "rol=FUNCIONARIO&periodoAcademico=2025-1"},
                new String[] {"funcionario", "rol=ESTUDIANTE&idUsuario=6"},
                new String[] {"funcionario", "rol=COORDINADOR"},
                new String[] {"funcionario", "rol=OTRO"});
        for (String prefijo : List.of(b + "/listarSolicitud-ecaes/porRol?", b + "?")) {
            for (String[] c : porRol) {
                caso(casos, c[0], HttpMethod.GET, prefijo + c[1], null);
            }
        }
        caso(casos, "funcionario", HttpMethod.GET, b + "/listarSolicitud-ecaes/porRol", null);

        // Por id
        for (String prefijo : List.of(b + "/buscarSolicitud-Ecaes/", b + "/")) {
            caso(casos, "est5", HttpMethod.GET, prefijo + "20", null);
            caso(casos, "est5", HttpMethod.GET, prefijo + "21", null);
            caso(casos, "funcionario", HttpMethod.GET, prefijo + "21", null);
            caso(casos, "funcionario", HttpMethod.GET, prefijo + "99", null);
            caso(casos, "funcionario", HttpMethod.GET, prefijo + "abc", null);
        }

        // Cambio de estado
        String aPreRegistro = "{\"idSolicitud\":20,\"nuevoEstado\":\"PRE_REGISTRADO\"}";
        String aAprobada = "{\"idSolicitud\":20,\"nuevoEstado\":\"APROBADA\"}";
        for (String ruta : List.of(b + "/actualizarEstadoSolicitud", b + "/20/estado")) {
            caso(casos, "funcionario", HttpMethod.PUT, ruta, aPreRegistro);
            caso(casos, "funcionario", HttpMethod.PUT, ruta, aAprobada);
            caso(casos, "est5", HttpMethod.PUT, ruta, aPreRegistro);
        }

        // Fechas
        for (String ruta : List.of(b + "/publicarFechasEcaes", b + "/fechas")) {
            caso(casos, "funcionario", HttpMethod.POST, ruta, CUERPO_FECHAS);
            caso(casos, "est5", HttpMethod.POST, ruta, CUERPO_FECHAS);
        }
        for (String ruta : List.of(b + "/listarFechasEcaes", b + "/fechas")) {
            caso(casos, "est5", HttpMethod.GET, ruta, null);
        }
        for (String prefijo : List.of(b + "/buscarFechasPorPeriodo/", b + "/fechas/")) {
            caso(casos, "est5", HttpMethod.GET, prefijo + "2025-1", null);
            caso(casos, "est5", HttpMethod.GET, prefijo + "2030-1", null);
        }
        for (String ruta : List.of(b + "/actualizarFechasEcaes", b + "/fechas/2025-1")) {
            caso(casos, "funcionario", HttpMethod.PUT, ruta, CUERPO_FECHAS);
            caso(casos, "est5", HttpMethod.PUT, ruta, CUERPO_FECHAS);
        }

        // Solo rutas nuevas: lo que la convención decide aparte
        for (String ruta : List.of(b + "?bandeja=pendientes", b + "?rol=coordinador&bandeja=pendientes",
                b + "?rol=funcionario&bandeja=procesadas", b + "/fechas/abc")) {
            caso(casos, "funcionario", HttpMethod.GET, ruta, null);
        }
        caso(casos, "funcionario", HttpMethod.PUT, b + "/20/estado", "{\"idSolicitud\":21,\"nuevoEstado\":\"PRE_REGISTRADO\"}");
        caso(casos, "funcionario", HttpMethod.PUT, b + "/20/estado", "{\"nuevoEstado\":\"PRE_REGISTRADO\"}");
        caso(casos, "funcionario", HttpMethod.PUT, b + "/99/estado", "{\"nuevoEstado\":\"PRE_REGISTRADO\"}");
        caso(casos, "funcionario", HttpMethod.PUT, b + "/fechas/2025-2", CUERPO_FECHAS);

        // EST-7: las rutas nuevas responden como las antiguas, que pasan a 404
        GrafoObjetos.compararConReferencia(casos, "rutas/ecaes.json", "rutas/correcciones/ecaes-est7.json");
    }

    private void caso(ObjectNode casos, String quien, HttpMethod metodo, String ruta, String cuerpo) throws Exception {
        llamadas.removeAll();
        String rol = switch (quien) {
            case "funcionario" -> "Funcionario";
            case "admin" -> "Administrador";
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

    private static SolicitudEcaes solicitud(int id, Usuario usuario, String estado) {
        SolicitudEcaes s = new SolicitudEcaes();
        s.setIdSolicitud(id);
        s.setNombreSolicitud("Solicitud ECAES " + id);
        s.setPeriodoAcademico("2025-1");
        s.setFechaRegistroSolicitud(new Date(1740000000000L));
        s.setObjUsuario(usuario);
        s.setTipoDocumento(TipoDocumento.CC);
        s.setNumeroDocumento("10201012" + id);
        s.setFechaExpedicion(new Date(1580000000000L));
        s.setFechaNacimiento(new Date(950000000000L));
        EstadoSolicitud e = new EstadoSolicitud();
        e.setIdEstado(id * 10);
        e.setEstadoActual(estado);
        e.setFechaRegistroEstado(new Date(1740000000000L));
        s.setEstadosSolicitud(new ArrayList<>(List.of(e)));
        s.setDocumentos(new ArrayList<>());
        return s;
    }

    private static FechaEcaes fechas(int id, String periodo) {
        FechaEcaes f = new FechaEcaes();
        f.setIdFechaEcaes(id);
        f.setPeriodoAcademico(periodo);
        f.setInscripcionEstByFacultad(LocalDate.of(2025, 2, 1));
        f.setRegistroRecaudoOrdinario(LocalDate.of(2025, 2, 10));
        f.setRegistroRecaudoExtraordinario(LocalDate.of(2025, 2, 20));
        f.setCitacion(LocalDate.of(2025, 3, 1));
        f.setAplicacion(LocalDate.of(2025, 3, 15));
        f.setResultadosIndividuales(LocalDate.of(2025, 4, 30));
        return f;
    }
}
