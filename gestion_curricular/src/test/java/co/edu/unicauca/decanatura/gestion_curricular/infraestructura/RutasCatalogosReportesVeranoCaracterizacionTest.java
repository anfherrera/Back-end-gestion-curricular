package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GenerarReportesCursosVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocentesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarMateriasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSalonesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.CondicionSolicitudVerano;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;

/**
 * Caracterización previa a la migración de rutas de EST-7 (paso 8c): catálogos y reportes de cursos de verano. Cada
 * operación se pide por su ruta antigua y por la nueva, con la aplicación real (seguridad de método activa) y los casos
 * de uso simulados. Pide el estudiante 5, un funcionario o un administrador. Guarda el estado, el tipo, el cuerpo (el
 * texto, si no es JSON), el {@code Content-Disposition} (con la hora del nombre del Excel como marcador) y las llamadas
 * a los casos de uso, con las fechas de los filtros en texto.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class RutasCatalogosReportesVeranoCaracterizacionTest {

    private static final Date FECHA = new Date(1751371200000L);
    private static final String VIEJA = "/api/cursos-intersemestrales";
    private static final String NUEVA = "/api/cursos-verano";

    private final ObjectMapper json = new ObjectMapper();
    private final ArrayNode llamadas = json.createArrayNode();

    @Autowired MockMvc mvc;
    @MockBean GestionarMateriasCUIntPort materiaCU;
    @MockBean GestionarDocentesCUIntPort docenteCU;
    @MockBean GestionarSalonesCUIntPort salonCU;
    @MockBean GestionarCursoOfertadoVeranoCUIntPort cursoCU;
    @MockBean GestionarSolicitudCursoVeranoCUIntPort solicitudCU;
    @MockBean GenerarReportesCursosVeranoCUIntPort generadorExcel;
    @MockBean GestionarUsuarioGatewayIntPort usuarioGateway;
    /** El adaptador del gateway también es el UserDetailsService; al simular el gateway hay que darlo aparte. */
    @MockBean UserDetailsService userDetailsService;

    @BeforeEach
    void preparar() {
        Usuario estudiante5 = usuario(5, "Estudiante");
        when(usuarioGateway.buscarUsuarioPorCorreo("est5@prueba.test")).thenReturn(estudiante5);
        when(usuarioGateway.buscarUsuarioPorCorreo("funcionario@prueba.test")).thenReturn(usuario(1, "Funcionario"));
        when(usuarioGateway.buscarUsuarioPorCorreo("admin@prueba.test")).thenReturn(usuario(7, "Administrador"));

        // Catálogos
        when(materiaCU.listarMaterias()).thenReturn(List.of(materia(1, "MAT-1", "Calculo", 4), materia(2, "FIS-1", "Fisica", 3)));
        Docente docente = new Docente();
        docente.setIdDocente(3);
        docente.setCodigoDocente("D-3");
        docente.setNombreDocente("Docente Tres");
        when(docenteCU.listarDocentes()).thenReturn(List.of(docente));
        when(salonCU.listarSalonesActivos()).thenReturn(List.of(salon(1, "201", "FIET", true), salon(2, "105", null, null)));

        // Cursos: 71 en preinscripción, 72 cerrado y 73 sin estados
        CursoOfertadoVerano c71 = curso(71, materia(1, "MAT-1", "Calculo", 4), "Preinscripcion");
        CursoOfertadoVerano c72 = curso(72, materia(2, "FIS-1", "Fisica", 3), "Cerrado");
        CursoOfertadoVerano c73 = curso(73, materia(1, "MAT-1", "Calculo", 4), null);
        when(cursoCU.listarTodos()).thenReturn(List.of(c71, c72, c73));

        // Solicitudes: 91 preinscripción aprobada, 92 inscripción con el pago validado y 98 solicitud de curso nuevo
        SolicitudCursoVeranoPreinscripcion p91 = new SolicitudCursoVeranoPreinscripcion();
        llenar(p91, 91, estudiante5, "Aprobado");
        p91.setObjCursoOfertadoVerano(c71);
        p91.setCondicionSolicitud(CondicionSolicitudVerano.Primera_Vez);
        SolicitudCursoVeranoPreinscripcion p98 = new SolicitudCursoVeranoPreinscripcion();
        llenar(p98, 98, estudiante5, "Enviada");
        p98.setObservacion("Solicitud de apertura de curso: Redes");
        SolicitudCursoVeranoInscripcion i92 = new SolicitudCursoVeranoInscripcion();
        llenar(i92, 92, estudiante5, "Pago_Validado");
        i92.setObjCursoOfertadoVerano(c72);
        i92.setCondicionSolicitud(CondicionSolicitudVerano.Habilitación);
        i92.setDocumentos(new ArrayList<>());
        doAnswer(i -> {
            llamadas.addObject().put("listarSolicitudesParaReporte", i.getArgument(0) + ", " + i.getArgument(1) + ", "
                    + i.getArgument(2) + ", " + local(i.getArgument(3)) + ", " + local(i.getArgument(4)));
            return List.<Solicitud>of(p91, i92);
        }).when(solicitudCU).listarSolicitudesParaReporte(any(), any(), any(), any(), any());
        when(solicitudCU.buscarSolicitudesPorUsuario(anyInt())).thenAnswer(i ->
                i.<Integer>getArgument(0) == 5 ? List.of(p91, p98) : List.of());
        when(solicitudCU.buscarInscripcionesPorUsuario(anyInt())).thenAnswer(i ->
                i.<Integer>getArgument(0) == 5 ? List.of(i92) : List.of());
        doAnswer(i -> {
            llamadas.addObject().put("generarExcelSolicitudes", i.getArgument(0) + ", " + i.getArgument(1) + ", "
                    + i.getArgument(2) + ", " + local(i.getArgument(3)) + ", " + local(i.getArgument(4)));
            return "XLSX".getBytes(StandardCharsets.US_ASCII);
        }).when(generadorExcel).generarExcelSolicitudes(any(), any(), any(), any(), any());
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void catalogos() throws Exception {
        ObjectNode casos = json.createObjectNode();
        String c = NUEVA + "/catalogos";
        String[][] pares = {
            {"/materias-disponibles", "/materias-disponibles"},
            {"/condiciones-solicitud", "/condiciones"},
            {"/materias", "/materias"},
            {"/materias-filtro", "/materias-filtro"},
            {"/docentes", "/docentes"},
            {"/salones", "/salones"}};
        for (String quien : List.of("est5", "funcionario")) {
            for (String[] par : pares) {
                caso(casos, quien, HttpMethod.GET, VIEJA + par[0]);
                caso(casos, quien, HttpMethod.GET, c + par[1]);
            }
        }

        // Se retira, sin uso: los permisos por estado y rol
        caso(casos, "funcionario", HttpMethod.GET, VIEJA + "/permisos-estado/Publicado/FUNCIONARIO");
        caso(casos, "est5", HttpMethod.GET, VIEJA + "/permisos-estado/Rara/ESTUDIANTE");

        // EST-7: las rutas nuevas responden como las antiguas, que pasan a 404
        GrafoObjetos.compararConReferencia(casos, "rutas/catalogos-verano.json",
                "rutas/correcciones/catalogos-verano-est7.json");
    }

    @Test
    void reportes() throws Exception {
        ObjectNode casos = json.createObjectNode();
        String r = NUEVA + "/reportes";

        // Listado de solicitudes con sus filtros
        for (String filtros : List.of("", "?periodoAcademico=2025-2&idMateria=1", "?periodoAcademico=todos&idMateria=0",
                "?idCurso=71&estado=Aprobado&fechaInicio=2025-01-01&fechaFin=2025-12-31", "?fechaInicio=2025-13-01")) {
            caso(casos, "funcionario", HttpMethod.GET, VIEJA + "/solicitudes" + filtros);
            caso(casos, "funcionario", HttpMethod.GET, r + "/solicitudes" + filtros);
        }
        caso(casos, "est5", HttpMethod.GET, VIEJA + "/solicitudes");
        caso(casos, "est5", HttpMethod.GET, r + "/solicitudes");

        // Exportación a Excel
        for (String filtros : List.of("", "idCurso=71&estado=Aprobado&periodoAcademico=2025-2&fechaInicio=2025-01-01")) {
            caso(casos, "funcionario", HttpMethod.GET, VIEJA + "/solicitudes/export/excel" + (filtros.isEmpty() ? "" : "?" + filtros));
            caso(casos, "funcionario", HttpMethod.GET, r + "/solicitudes?formato=excel" + (filtros.isEmpty() ? "" : "&" + filtros));
        }
        caso(casos, "est5", HttpMethod.GET, VIEJA + "/solicitudes/export/excel");
        caso(casos, "est5", HttpMethod.GET, r + "/solicitudes?formato=excel");

        // Dashboard
        for (String quien : List.of("funcionario", "admin", "est5")) {
            caso(casos, quien, HttpMethod.GET, VIEJA + "/dashboard/estadisticas");
            caso(casos, quien, HttpMethod.GET, r + "/dashboard");
        }

        // Seguimiento de un estudiante
        for (String[] quienYId : List.of(new String[] {"est5", "5"}, new String[] {"est5", "6"},
                new String[] {"funcionario", "5"}, new String[] {"funcionario", "6"}, new String[] {"funcionario", "abc"})) {
            caso(casos, quienYId[0], HttpMethod.GET, VIEJA + "/seguimiento/" + quienYId[1]);
            caso(casos, quienYId[0], HttpMethod.GET, r + "/seguimiento?usuario=" + quienYId[1]);
        }

        // Solo rutas nuevas: lo que la convención decide aparte
        caso(casos, "funcionario", HttpMethod.GET, r + "/solicitudes?formato=pdf");
        caso(casos, "funcionario", HttpMethod.GET, r + "/seguimiento");

        // EST-7: las rutas nuevas responden como las antiguas, que pasan a 404
        GrafoObjetos.compararConReferencia(casos, "rutas/reportes-verano.json",
                "rutas/correcciones/reportes-verano-est7.json");
    }

    private void caso(ObjectNode casos, String quien, HttpMethod metodo, String ruta) throws Exception {
        llamadas.removeAll();
        String rol = switch (quien) {
            case "funcionario" -> "Funcionario";
            case "admin" -> "Administrador";
            default -> "Estudiante";
        };
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                quien + "@prueba.test", "x", List.of(new SimpleGrantedAuthority("ROLE_" + rol))));
        var respuesta = mvc.perform(request(metodo, ruta)).andReturn().getResponse();
        SecurityContextHolder.clearContext();
        ObjectNode caso = json.createObjectNode();
        caso.put("status", respuesta.getStatus());
        caso.put("tipo", respuesta.getContentType());
        String texto = respuesta.getContentAsString(StandardCharsets.UTF_8);
        String tipo = respuesta.getContentType();
        JsonNode valor = texto.isEmpty() ? json.nullNode()
                : tipo != null && tipo.contains("json") ? json.readTree(texto) : json.getNodeFactory().textNode(texto);
        caso.set("cuerpo", valor);
        String disposicion = respuesta.getHeader(HttpHeaders.CONTENT_DISPOSITION);
        caso.put("disposicion", disposicion == null ? null : disposicion.replaceAll("\\d{8}_\\d{6}", "<ahora>"));
        caso.set("llamadas", llamadas.deepCopy());
        casos.set(metodo + " " + ruta + " [" + quien + "]", caso);
    }

    /**
     * Las fechas de los filtros, en la zona de la JVM: el controlador las lee en esa zona, así que el texto no depende
     * de ella (el instante sí).
     */
    private static String local(Object fecha) {
        return fecha instanceof Date d ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(d) : String.valueOf(fecha);
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

    private static Materia materia(int id, String codigo, String nombre, int creditos) {
        Materia m = new Materia();
        m.setIdMateria(id);
        m.setCodigo(codigo);
        m.setNombre(nombre);
        m.setCreditos(creditos);
        return m;
    }

    private static Salon salon(int id, String numero, String edificio, Boolean activo) {
        Salon s = new Salon();
        s.setIdSalon(id);
        s.setNumeroSalon(numero);
        s.setEdificio(edificio);
        s.setActivo(activo);
        return s;
    }

    private static CursoOfertadoVerano curso(int id, Materia materia, String estado) {
        CursoOfertadoVerano curso = new CursoOfertadoVerano();
        curso.setIdCurso(id);
        curso.setObjMateria(materia);
        curso.setPeriodoAcademico("2025-2");
        curso.setFecha_inicio(FECHA);
        if (estado != null) {
            EstadoCursoOfertado e = new EstadoCursoOfertado();
            e.setIdEstado(id * 10);
            e.setEstadoActual(estado);
            e.setFechaRegistroEstado(FECHA);
            curso.getEstadosCursoOfertados().add(e);
        }
        return curso;
    }

    private static void llenar(Solicitud s, int id, Usuario usuario, String estado) {
        s.setIdSolicitud(id);
        s.setNombreSolicitud("Solicitud " + id);
        s.setPeriodoAcademico("2025-2");
        s.setFechaRegistroSolicitud(FECHA);
        s.setObjUsuario(usuario);
        EstadoSolicitud e = new EstadoSolicitud();
        e.setIdEstado(id * 10);
        e.setEstadoActual(estado);
        e.setFechaRegistroEstado(FECHA);
        e.setComentario("Comentario " + id);
        s.setEstadosSolicitud(new ArrayList<>(List.of(e)));
    }
}
