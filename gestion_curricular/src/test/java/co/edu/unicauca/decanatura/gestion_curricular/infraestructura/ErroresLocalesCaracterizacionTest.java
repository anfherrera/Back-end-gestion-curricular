package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.Invocation;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import co.edu.unicauca.decanatura.gestion_curricular.Security.JwtUtil;
import co.edu.unicauca.decanatura.gestion_curricular.Security.LoginRateLimiter;
import co.edu.unicauca.decanatura.gestion_curricular.Security.SecurityAuditService;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarUsuarioCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.CupoAgotadoException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadYaExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.ReglaNegocioExcepcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.UsuarioDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladorExcepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.CursoOfertadoRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.InscripcionesVeranoRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.PreinscripcionesVeranoRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.UsuarioRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.CursosOfertadosMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.EstadoCursoOfertadoMapper;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudCursoDeVeranoPreinscripcionMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.UsuarioMapperDominio;

/**
 * Caracterización de los try/catch locales de Inscripciones, CursoOfertado, Preinscripciones y Usuario (ARQ-11):
 * para cada excepción que puede lanzar el caso de uso fija estado, cuerpo y llamadas, antes de mover la traducción.
 */
class ErroresLocalesCaracterizacionTest {

    private record Mocks(GestionarSolicitudCursoVeranoCUIntPort solicitud, GestionarCursoOfertadoVeranoCUIntPort curso,
                         GestionarUsuarioCUIntPort usuario, UsuarioMapperDominio usuarioMapper) {}

    private static final Map<String, Supplier<RuntimeException>> EXCEPCIONES = new LinkedHashMap<>();
    static {
        EXCEPCIONES.put("cupo", () -> new CupoAgotadoException(10, 10));
        EXCEPCIONES.put("regla", () -> new ReglaNegocioExcepcion("regla rota"));
        EXCEPCIONES.put("noExiste", () -> new EntidadNoExisteException("no existe"));
        EXCEPCIONES.put("yaExiste", () -> new EntidadYaExisteException("ya existe"));
        EXCEPCIONES.put("argumento", () -> new IllegalArgumentException("argumento malo"));
        EXCEPCIONES.put("runtime", () -> new RuntimeException("fallo interno"));
        EXCEPCIONES.put("npe", NullPointerException::new);
    }

    private final ObjectMapper json = new ObjectMapper();
    private final Map<String, Object> resultados = new LinkedHashMap<>();

    @Test
    void conservaRespuestasDeErrorYPoliticasQueIgnoranErrores() throws Exception {
        // Inscripciones: crear inscripción
        Supplier<MockHttpServletRequestBuilder> crearInscripcion = () -> post("/api/cursos-intersemestrales/cursos-verano/inscripciones")
            .contentType(MediaType.APPLICATION_JSON).content("{\"idUsuario\":5,\"idCurso\":3,\"nombreSolicitud\":\"Ins\"}");
        caso("crearInscripcion ok", crearInscripcion, m -> {});
        caso("crearInscripcion asociar false", crearInscripcion, m -> when(m.curso().asociarEstudiante(3, 5)).thenReturn(false));
        caso("crearInscripcion asociar falla", crearInscripcion, m -> when(m.curso().asociarEstudiante(3, 5)).thenThrow(new RuntimeException("x")));
        for (var e : EXCEPCIONES.entrySet()) {
            caso("crearInscripcion verificarCupo " + e.getKey(), crearInscripcion,
                m -> doThrow(e.getValue().get()).when(m.solicitud()).verificarCupoDisponible(any()));
            caso("crearInscripcion crear " + e.getKey(), crearInscripcion,
                m -> when(m.solicitud().crearSolicitudCursoVeranoInscripcion(any())).thenThrow(e.getValue().get()));
        }

        // Inscripciones: aceptar
        Supplier<MockHttpServletRequestBuilder> aceptar = () -> put("/api/cursos-intersemestrales/inscripciones/7/aceptar")
            .contentType(MediaType.APPLICATION_JSON).content("{}");
        caso("aceptar ok", aceptar, m -> {});
        caso("aceptar inexistente", aceptar, m -> when(m.solicitud().buscarPorIdInscripcion(7)).thenReturn(null));
        caso("aceptar validarPago null", aceptar, m -> when(m.solicitud().validarPago(anyInt(), anyBoolean(), any())).thenReturn(null));
        caso("aceptar asociar falla", aceptar, m -> when(m.curso().asociarEstudiante(anyInt(), anyInt())).thenThrow(new RuntimeException("x")));
        caso("aceptar sin curso", aceptar, m -> when(m.solicitud().buscarPorIdInscripcion(7)).thenReturn(inscripcion(false)));
        for (var e : EXCEPCIONES.entrySet()) {
            caso("aceptar verificarCupo " + e.getKey(), aceptar,
                m -> doThrow(e.getValue().get()).when(m.solicitud()).verificarCupoParaAceptar(any()));
            caso("aceptar validarPago " + e.getKey(), aceptar,
                m -> when(m.solicitud().validarPago(anyInt(), anyBoolean(), any())).thenThrow(e.getValue().get()));
            caso("aceptar buscar " + e.getKey(), aceptar,
                m -> when(m.solicitud().buscarPorIdInscripcion(7)).thenThrow(e.getValue().get()));
        }

        // Inscripciones: rechazar
        Supplier<MockHttpServletRequestBuilder> rechazar = () -> put("/api/cursos-intersemestrales/inscripciones/7/rechazar")
            .contentType(MediaType.APPLICATION_JSON).content("{}");
        caso("rechazar ok", rechazar, m -> {});
        caso("rechazar validarPago null", rechazar, m -> when(m.solicitud().validarPago(anyInt(), anyBoolean(), any())).thenReturn(null));
        for (var e : EXCEPCIONES.entrySet()) {
            caso("rechazar validarPago " + e.getKey(), rechazar,
                m -> when(m.solicitud().validarPago(anyInt(), anyBoolean(), any())).thenThrow(e.getValue().get()));
        }

        // CursoOfertado: validar pago
        Supplier<MockHttpServletRequestBuilder> validarPago = () -> put("/api/cursos-verano/inscripciones/7/validar-pago")
            .contentType(MediaType.APPLICATION_JSON).content("{\"esValido\":true,\"observaciones\":\"ok\"}");
        caso("validarPago ok", validarPago, m -> {});
        for (var e : EXCEPCIONES.entrySet()) {
            caso("validarPago " + e.getKey(), validarPago,
                m -> when(m.solicitud().validarPago(anyInt(), anyBoolean(), any())).thenThrow(e.getValue().get()));
        }

        // Preinscripciones: solicitud de curso nuevo
        caso("cursoNuevo ok", cursoNuevo("\"Primera_Vez\""), m -> {});
        caso("cursoNuevo condicion invalida", cursoNuevo("\"Rara\""), m -> {});
        caso("cursoNuevo condicion null", cursoNuevo("null"), m -> {});
        caso("cursoNuevo sin fecha", cursoNuevo("\"Primera_Vez\""),
            m -> when(m.solicitud().crearSolicitudCursoVeranoPreinscripcion(any())).thenReturn(new SolicitudCursoVeranoPreinscripcion()));
        for (var e : EXCEPCIONES.entrySet()) {
            caso("cursoNuevo crear " + e.getKey(), cursoNuevo("\"Primera_Vez\""),
                m -> when(m.solicitud().crearSolicitudCursoVeranoPreinscripcion(any())).thenThrow(e.getValue().get()));
        }

        // Usuario: crear
        Supplier<MockHttpServletRequestBuilder> crearUsuario = () -> post("/api/usuarios/crearUsuario")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"nombre_completo\":\"Ana Ruiz\",\"id_rol\":2,\"codigo\":\"1045\",\"cedula\":\"123456\","
                + "\"correo\":\"ana@unicauca.edu.co\",\"password\":\"x\",\"estado_usuario\":true,\"id_programa\":1}");
        caso("crearUsuario ok", crearUsuario, m -> {});
        caso("crearUsuario devuelve null", crearUsuario, m -> when(m.usuario().crearUsuario(any())).thenReturn(null));
        caso("crearUsuario sin rol", crearUsuario, m -> when(m.usuario().crearUsuario(any())).thenAnswer(i -> {
            Usuario u = i.getArgument(0); u.setObjRol(null); return u; }));
        caso("crearUsuario sin programa", crearUsuario, m -> when(m.usuario().crearUsuario(any())).thenAnswer(i -> {
            Usuario u = i.getArgument(0); u.setObjPrograma(null); return u; }));
        caso("crearUsuario mapper null", crearUsuario, m -> when(m.usuarioMapper().mappearDeUsuarioAUsuarioDTORespuesta(any())).thenReturn(null));
        caso("crearUsuario mapper source null", crearUsuario, m -> when(m.usuarioMapper().mappearDeUsuarioAUsuarioDTORespuesta(any()))
            .thenThrow(new IllegalArgumentException("source cannot be null")));
        for (var e : EXCEPCIONES.entrySet()) {
            caso("crearUsuario crear " + e.getKey(), crearUsuario,
                m -> when(m.usuario().crearUsuario(any())).thenThrow(e.getValue().get()));
            caso("crearUsuario mapper " + e.getKey(), crearUsuario,
                m -> when(m.usuarioMapper().mappearDeUsuarioAUsuarioDTORespuesta(any())).thenThrow(e.getValue().get()));
        }

        JsonNode actual = json.valueToTree(resultados);
        Path referencia = Path.of("src/test/resources/referencias/errores/errores-locales.json");
        if (Boolean.getBoolean("grabarReferencia")) {
            assertThat(Files.exists(referencia)).isFalse();
            Files.createDirectories(referencia.getParent());
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(actual));
        }
        assertThat(actual).isEqualTo(json.readTree(Files.readString(referencia)));
    }

    private static Supplier<MockHttpServletRequestBuilder> cursoNuevo(String condicion) {
        return () -> post("/api/cursos-intersemestrales/solicitudes-curso-nuevo").contentType(MediaType.APPLICATION_JSON)
            .content("{\"nombreCompleto\":\"Ana Ruiz\",\"codigo\":\"104512\",\"curso\":\"Calculo\",\"condicion\":" + condicion
                + ",\"idUsuario\":5}");
    }

    private void caso(String nombre, Supplier<MockHttpServletRequestBuilder> peticion, Consumer<Mocks> configurar) throws Exception {
        Mocks m = new Mocks(mock(GestionarSolicitudCursoVeranoCUIntPort.class), mock(GestionarCursoOfertadoVeranoCUIntPort.class),
            mock(GestionarUsuarioCUIntPort.class), mock(UsuarioMapperDominio.class));
        configurarBase(m);
        configurar.accept(m);

        MockMvc mvc = MockMvcBuilders.standaloneSetup(
                new CursoOfertadoRestController(m.curso(), m.solicitud(), mock(CursosOfertadosMapperDominio.class), mock(EstadoCursoOfertadoMapper.class)),
                new PreinscripcionesVeranoRestController(m.solicitud(), mock(CursosOfertadosMapperDominio.class),
                    mock(SolicitudCursoDeVeranoPreinscripcionMapperDominio.class)),
                new InscripcionesVeranoRestController(m.curso(), m.solicitud(), mock(GestionarArchivosCUIntPort.class),
                    mock(GestionarSolicitudCUIntPort.class)),
                // El login no se ejecuta en estos casos: sus dependencias quedan sin crear
                new UsuarioRestController(m.usuario(), m.usuarioMapper(), (AuthenticationManager) null, (JwtUtil) null,
                    (UserDetailsService) null, (SecurityAuditService) null, (LoginRateLimiter) null))
            .setControllerAdvice(new RestApiExceptionHandler()).build();

        var respuesta = mvc.perform(peticion.get()).andReturn().getResponse();
        byte[] cuerpo = respuesta.getContentAsByteArray();
        JsonNode body;
        try {
            body = cuerpo.length == 0 ? json.nullNode() : json.readTree(cuerpo);
        } catch (Exception e) {
            body = json.getNodeFactory().textNode(respuesta.getContentAsString());
        }
        normalizar(body);

        List<String> llamadas = new ArrayList<>();
        Stream.of(m.solicitud(), m.curso(), m.usuario(), m.usuarioMapper())
            .flatMap(mk -> Mockito.mockingDetails(mk).getInvocations().stream())
            .sorted(Comparator.comparingInt(Invocation::getSequenceNumber))
            .forEach(i -> llamadas.add(i.getMethod().getName()));

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("status", respuesta.getStatus());
        snapshot.put("body", body);
        snapshot.put("llamadas", llamadas);
        resultados.put(nombre, snapshot);
    }

    private static void configurarBase(Mocks m) {
        var cu = m.solicitud();
        var pre = new SolicitudCursoVeranoPreinscripcion();
        pre.setId_solicitud(4);
        pre.setObjCursoOfertadoVerano(curso());
        pre.setObjUsuario(usuario());
        when(cu.buscarPreinscripcionAprobada(5, 3)).thenReturn(pre);
        when(cu.tieneInscripcionActiva(5, 3)).thenReturn(false);
        when(cu.crearSolicitudCursoVeranoInscripcion(any())).thenAnswer(i -> {
            SolicitudCursoVeranoIncripcion s = i.getArgument(0);
            if (s == null) return null;
            s.setId_solicitud(9);
            return s;
        });
        when(cu.buscarPorIdInscripcion(7)).thenAnswer(i -> inscripcion(true));
        when(cu.validarPago(anyInt(), anyBoolean(), any())).thenAnswer(i -> inscripcion(true));
        when(cu.crearSolicitudCursoVeranoPreinscripcion(any())).thenAnswer(i -> {
            SolicitudCursoVeranoPreinscripcion s = i.getArgument(0);
            if (s == null) return null;
            s.setId_solicitud(12);
            return s;
        });
        when(m.curso().asociarEstudiante(anyInt(), anyInt())).thenReturn(true);
        when(m.usuario().crearUsuario(any())).thenAnswer(i -> {
            Usuario u = i.getArgument(0);
            if (u == null) return null;
            u.setId_usuario(21);
            return u;
        });
        when(m.usuarioMapper().mappearDeUsuarioAUsuarioDTORespuesta(any())).thenAnswer(i -> {
            Usuario u = i.getArgument(0);
            if (u == null) return null;
            var dto = new UsuarioDTORespuesta();
            dto.setId_usuario(u.getId_usuario());
            dto.setNombre_completo(u.getNombre_completo());
            return dto;
        });
    }

    private static CursoOfertadoVerano curso() {
        var materia = new Materia();
        materia.setNombre("Calculo");
        var curso = new CursoOfertadoVerano();
        curso.setId_curso(3);
        curso.setObjMateria(materia);
        return curso;
    }

    private static Usuario usuario() {
        var usuario = new Usuario();
        usuario.setId_usuario(5);
        usuario.setNombre_completo("Ana Ruiz");
        usuario.setObjRol(new Rol());
        usuario.setObjPrograma(new Programa());
        return usuario;
    }

    private static SolicitudCursoVeranoIncripcion inscripcion(boolean conCurso) {
        var s = new SolicitudCursoVeranoIncripcion();
        s.setId_solicitud(7);
        s.setObjUsuario(usuario());
        if (conCurso) s.setObjCursoOfertadoVerano(curso());
        var estado = new EstadoSolicitud();
        estado.setEstado_actual("Enviada");
        estado.setFecha_registro_estado(new Date(0));
        s.setEstadosSolicitud(new ArrayList<>(List.of(estado)));
        s.setFecha_registro_solicitud(new Date(0));
        return s;
    }

    private static void normalizar(JsonNode nodo) {
        if (nodo.isObject()) {
            ObjectNode objeto = (ObjectNode) nodo;
            List<String> claves = new ArrayList<>();
            objeto.fieldNames().forEachRemaining(claves::add);
            for (String clave : claves) {
                if ((clave.startsWith("fecha") || clave.equals("timestamp")) && objeto.hasNonNull(clave)) {
                    objeto.put(clave, "<fecha>");
                }
            }
        }
        nodo.elements().forEachRemaining(ErroresLocalesCaracterizacionTest::normalizar);
    }
}
