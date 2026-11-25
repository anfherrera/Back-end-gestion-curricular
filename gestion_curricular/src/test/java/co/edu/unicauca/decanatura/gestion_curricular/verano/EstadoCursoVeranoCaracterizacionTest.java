package co.edu.unicauca.decanatura.gestion_curricular.verano;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSalonesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarCursoOfertadoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocenteGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarMateriasIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSalonesIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudCursoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarCursoOfertadoVeranoCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarSolicitudCursoVeranoCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.TransicionEstadoCurso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.CursosVeranoRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.ReportesCursosVeranoRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.CursosOfertadosMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.DocenteMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.ProgramaMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.RolMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.UsuarioMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.Correcciones;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.SoporteMvc;

/**
 * Fija cómo se lee el estado actual de un curso de verano en cada sitio que lo calcula (OPC-1): el flujo de estados
 * del curso, el listado de cursos visibles, la fase que exigen la preinscripción y la inscripción, el listado por
 * estado ({@code GET /api/cursos-verano/cursos?estado=}), el dashboard y el seguimiento de reportes. Cada historial
 * se prueba solo, con un único curso, incluidos los bordes: sin lista, lista vacía y último estado sin texto.
 */
class EstadoCursoVeranoCaracterizacionTest {

    private static final Path REFERENCIA = Path.of("src/test/resources/referencias/estados/curso-verano.json");

    /** Historiales del curso (null = sin lista; un elemento null = estado sin texto). */
    private static final String[][] HISTORIALES = {
        null,
        {},
        {null},
        {"Borrador"},
        {"Abierto"},
        {"Publicado"},
        {"Preinscripcion"},
        {"Inscripcion"},
        {"Cerrado"},
        {"preinscripcion"},
        {"Preinscripcion", "Inscripcion"},
        {"Inscripcion", "Preinscripcion"},
        {"Publicado", null},
        {null, "Publicado"},
    };

    private static final String[] ESTADOS_CURSO = {
        "Borrador", "Abierto", "Publicado", "Preinscripcion", "Inscripcion", "Cerrado",
    };

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void leeElEstadoActualDelCurso() throws Exception {
        ObjectNode resultados = json.createObjectNode();
        for (String[] historial : HISTORIALES) {
            ObjectNode porHistorial = resultados.putObject(historial == null ? "sin lista" : Arrays.toString(historial));
            porHistorial.set("dominio", dominio(historial));
            porHistorial.set("casosDeUso", casosDeUso(historial));
            porHistorial.set("http", http(historial));
        }
        resultados.set("estaCompleto", estaCompleto());

        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(REFERENCIA)) {
            Files.createDirectories(REFERENCIA.getParent());
            Files.writeString(REFERENCIA, json.writerWithDefaultPrettyPrinter().writeValueAsString(resultados));
        }
        // BUG-53: un último estado sin texto es un estado desconocido (antes, NullPointerException)
        assertThat(resultados).isEqualTo(Correcciones.aplicar(json.readTree(Files.readString(REFERENCIA)),
                "estados/correcciones/curso-verano-bug53.json"));
    }

    private ObjectNode dominio(String[] historial) {
        ObjectNode nodo = json.createObjectNode();
        nodo.set("estadoActual", resultado(() -> TransicionEstadoCurso.estadoActual(curso(historial))));
        ObjectNode validar = nodo.putObject("validar");
        for (String nuevo : ESTADOS_CURSO) {
            validar.set(nuevo, resultado(() -> {
                TransicionEstadoCurso.validar(cursoCompleto(historial), nuevo);
                return "permitido";
            }));
        }
        return nodo;
    }

    private ObjectNode casosDeUso(String[] historial) {
        ObjectNode nodo = json.createObjectNode();

        var gatewayCursos = mock(GestionarCursoOfertadoVeranoGatewayIntPort.class);
        when(gatewayCursos.listarTodos()).thenAnswer(i -> new ArrayList<>(List.of(curso(historial))));
        var cursos = new GestionarCursoOfertadoVeranoCUAdapter(gatewayCursos, mock(GestionarSolicitudGatewayIntPort.class),
                mock(GestionarUsuarioGatewayIntPort.class), mock(GestionarMateriasIntPort.class),
                mock(GestionarDocenteGatewayIntPort.class), mock(GestionarSalonesIntPort.class),
                mock(GestionarNotificacionCUIntPort.class));
        nodo.set("listarVisibles", resultado(() -> cursos.listarPorFiltros(null, null, true).size()));
        nodo.set("periodosConCursosVisibles", resultado(cursos::listarPeriodosConCursosVisibles));

        var usuarios = mock(GestionarUsuarioGatewayIntPort.class);
        when(usuarios.obtenerUsuarioPorId(anyInt())).thenAnswer(i -> usuario());
        var cursoOfertado = mock(GestionarCursoOfertadoVeranoGatewayIntPort.class);
        when(cursoOfertado.obtenerCursoPorId(anyInt())).thenAnswer(i -> curso(historial));
        var solicitudes = new GestionarSolicitudCursoVeranoCUAdapter(mock(GestionarSolicitudCursoVeranoGatewayIntPort.class),
                cursoOfertado, usuarios, mock(GestionarDocumentosGatewayIntPort.class),
                mock(GestionarArchivosCUIntPort.class), mock(GestionarNotificacionCUIntPort.class));
        // Sin condición: si el curso pasa la fase exigida, la creación se detiene en la condición
        nodo.set("crearPreinscripcion", resultado(() -> {
            var preinscripcion = new SolicitudCursoVeranoPreinscripcion();
            preinscripcion.setObjUsuario(usuario());
            preinscripcion.setObjCursoOfertadoVerano(cursoConId());
            return solicitudes.crearSolicitudCursoVeranoPreinscripcion(preinscripcion);
        }));
        nodo.set("crearInscripcion", resultado(() -> {
            var inscripcion = new SolicitudCursoVeranoInscripcion();
            inscripcion.setObjUsuario(usuario());
            inscripcion.setObjCursoOfertadoVerano(cursoConId());
            return solicitudes.crearSolicitudCursoVeranoInscripcion(inscripcion);
        }));
        return nodo;
    }

    private ObjectNode http(String[] historial) throws Exception {
        ObjectNode nodo = json.createObjectNode();
        var cursoCU = mock(GestionarCursoOfertadoVeranoCUIntPort.class);
        when(cursoCU.listarTodos()).thenAnswer(i -> new ArrayList<>(List.of(curso(historial))));
        var solicitudCU = mock(GestionarSolicitudCursoVeranoCUIntPort.class);
        when(solicitudCU.listarSolicitudesParaReporte(any(), any(), any(), any(), any())).thenReturn(List.of());
        when(solicitudCU.buscarSolicitudesPorUsuario(anyInt())).thenAnswer(i -> {
            var preinscripcion = new SolicitudCursoVeranoPreinscripcion();
            preinscripcion.setIdSolicitud(40);
            preinscripcion.setObjCursoOfertadoVerano(curso(historial));
            return List.of(preinscripcion);
        });
        when(solicitudCU.buscarInscripcionesPorUsuario(anyInt())).thenReturn(List.of());
        MockMvc mvc = SoporteMvc.standaloneSetup(
                new CursosVeranoRestController(cursoCU, solicitudCU, mapper(), mock(GestionarSalonesCUIntPort.class), null),
                new ReportesCursosVeranoRestController(cursoCU, solicitudCU, null))
            .setControllerAdvice(new RestApiExceptionHandler()).build();

        nodo.set("cursosPreinscripcion", ids(mvc, "/api/cursos-verano/cursos?estado=preinscripcion"));
        nodo.set("cursosInscripcion", ids(mvc, "/api/cursos-verano/cursos?estado=inscripcion"));
        nodo.set("dashboard", cuerpo(mvc, "/api/cursos-verano/reportes/dashboard"));
        JsonNode seguimiento = cuerpo(mvc, "/api/cursos-verano/reportes/seguimiento?usuario=7");
        nodo.set("seguimientoPreinscripciones", seguimiento.path("body").path("preinscripciones"));
        return nodo;
    }

    private ArrayNode estaCompleto() {
        ArrayNode lista = json.createArrayNode();
        Object[][] variantes = {
            {true, true, 5, "221"}, {false, true, 5, "221"}, {true, false, 5, "221"}, {true, true, null, "221"},
            {true, true, 0, "221"}, {true, true, -1, "221"}, {true, true, 5, null}, {true, true, 5, "  "},
            {true, true, 1, " a "},
        };
        for (Object[] v : variantes) {
            CursoOfertadoVerano curso = new CursoOfertadoVerano();
            curso.setObjMateria((Boolean) v[0] ? new Materia() : null);
            curso.setObjDocente((Boolean) v[1] ? new Docente() : null);
            curso.setCupoEstimado((Integer) v[2]);
            curso.setSalon((String) v[3]);
            ObjectNode nodo = lista.addObject();
            nodo.put("variante", Arrays.toString(v));
            nodo.put("estaCompleto", curso.estaCompleto());
            nodo.set("aAbiertoDesdeBorrador", resultado(() -> {
                curso.setEstadosCursoOfertados(estados(new String[] {"Borrador"}));
                TransicionEstadoCurso.validar(curso, "Abierto");
                return "permitido";
            }));
        }
        return lista;
    }

    // ==================== Apoyo ====================

    private JsonNode resultado(Callable<Object> llamada) {
        try {
            return json.valueToTree(llamada.call());
        } catch (Exception e) {
            ObjectNode error = json.createObjectNode();
            error.put("excepcion", e.getClass().getSimpleName());
            error.put("mensaje", e.getMessage());
            return error;
        }
    }

    private JsonNode ids(MockMvc mvc, String ruta) throws Exception {
        JsonNode respuesta = cuerpo(mvc, ruta);
        ObjectNode nodo = json.createObjectNode();
        nodo.set("status", respuesta.get("status"));
        ArrayNode ids = nodo.putArray("ids");
        respuesta.path("body").forEach(curso -> ids.add(curso.path("id_curso")));
        return nodo;
    }

    private JsonNode cuerpo(MockMvc mvc, String ruta) throws Exception {
        var respuesta = mvc.perform(get(ruta)).andReturn().getResponse();
        byte[] contenido = respuesta.getContentAsByteArray();
        ObjectNode nodo = json.createObjectNode();
        nodo.put("status", respuesta.getStatus());
        nodo.set("body", contenido.length == 0 ? json.nullNode() : json.readTree(contenido));
        return nodo;
    }

    private static CursoOfertadoVerano curso(String[] historial) {
        CursoOfertadoVerano curso = new CursoOfertadoVerano();
        curso.setIdCurso(5);
        curso.setPeriodoAcademico("2025-1");
        curso.setEstadosCursoOfertados(historial == null ? null : estados(historial));
        return curso;
    }

    private static CursoOfertadoVerano cursoCompleto(String[] historial) {
        CursoOfertadoVerano curso = curso(historial);
        curso.setObjMateria(new Materia());
        curso.setObjDocente(new Docente());
        curso.setCupoEstimado(10);
        curso.setSalon("221");
        return curso;
    }

    private static List<EstadoCursoOfertado> estados(String[] historial) {
        List<EstadoCursoOfertado> estados = new ArrayList<>();
        for (int i = 0; i < historial.length; i++) {
            estados.add(new EstadoCursoOfertado(100 + i, historial[i], new Date(0), null));
        }
        return estados;
    }

    private static CursoOfertadoVerano cursoConId() {
        CursoOfertadoVerano curso = new CursoOfertadoVerano();
        curso.setIdCurso(5);
        return curso;
    }

    private static Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(7);
        usuario.setNombreCompleto("Ana Pérez");
        return usuario;
    }

    private static CursosOfertadosMapperDominioImpl mapper() {
        var usuarios = new UsuarioMapperDominioImpl();
        ReflectionTestUtils.setField(usuarios, "programaMapperDominio", new ProgramaMapperDominioImpl());
        ReflectionTestUtils.setField(usuarios, "rolMapperDominio", new RolMapperDominioImpl());
        var mapper = new CursosOfertadosMapperDominioImpl();
        ReflectionTestUtils.setField(mapper, "docenteMapperDominio", new DocenteMapperDominioImpl());
        ReflectionTestUtils.setField(mapper, "usuarioMapperDominio", usuarios);
        return mapper;
    }
}
