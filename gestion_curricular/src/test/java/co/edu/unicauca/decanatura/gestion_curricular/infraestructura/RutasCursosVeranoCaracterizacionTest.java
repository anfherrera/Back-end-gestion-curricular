package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
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

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GenerarReportesCursosVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSalonesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.ListadoEstudiantes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.TransicionEstadoCurso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;

/**
 * Caracterización previa a la migración de rutas de EST-7 (paso 8a): cursos de verano
 * ({@code CursosVeranoRestController} y {@code CursoOfertadoRestController}, que se retira). Cada operación se pide
 * por su ruta antigua y por la nueva, con la aplicación real (mappers y seguridad de método activas) y los casos de
 * uso de cursos, solicitudes, salones y reportes simulados. Pide un estudiante, un funcionario, un coordinador o un
 * administrador. Guarda el estado, el tipo, el cuerpo (el texto, si no es JSON), el {@code Content-Disposition}
 * (con la fecha como marcador) y las llamadas a los casos de uso.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class RutasCursosVeranoCaracterizacionTest {

    private static final Date INICIO = new Date(1751371200000L);
    private static final Date FIN = new Date(1754049600000L);
    private static final String VIEJA = "/api/cursos-intersemestrales";
    private static final String OFERTADO = "/api/cursos-verano";
    private static final String NUEVA = "/api/cursos-verano/cursos";

    private final ObjectMapper json = new ObjectMapper();
    private final ArrayNode llamadas = json.createArrayNode();

    @Autowired MockMvc mvc;
    @MockBean GestionarCursoOfertadoVeranoCUIntPort cursoCU;
    @MockBean GestionarSolicitudCursoVeranoCUIntPort solicitudCU;
    @MockBean GestionarSalonesCUIntPort salonCU;
    @MockBean GenerarReportesCursosVeranoCUIntPort reportes;
    @MockBean GestionarSolicitudGatewayIntPort solicitudGateway;
    @MockBean GestionarUsuarioGatewayIntPort usuarioGateway;
    /** El adaptador del gateway también es el UserDetailsService; al simular el gateway hay que darlo aparte. */
    @MockBean UserDetailsService userDetailsService;

    @BeforeEach
    void preparar() {
        when(usuarioGateway.buscarUsuarioPorCorreo("est5@prueba.test")).thenReturn(usuario(5, "Estudiante"));
        when(usuarioGateway.buscarUsuarioPorCorreo("funcionario@prueba.test")).thenReturn(usuario(1, "Funcionario"));
        when(usuarioGateway.buscarUsuarioPorCorreo("coordinador@prueba.test")).thenReturn(usuario(2, "Coordinador"));
        when(usuarioGateway.buscarUsuarioPorCorreo("admin@prueba.test")).thenReturn(usuario(7, "Administrador"));

        CursoOfertadoVerano c71 = curso(71, "Preinscripcion", "2025-2", "221");
        CursoOfertadoVerano c72 = curso(72, "Inscripcion", "2025-1", null);
        CursoOfertadoVerano c73 = curso(73, "Borrador", "2025-2", "222");
        c73.getEstudiantesInscritos().add(usuario(5, "Estudiante"));
        CursoOfertadoVerano c74 = curso(74, "Publicado", "2025-2", "221");
        when(cursoCU.listarTodos()).thenReturn(List.of(c71, c72, c73, c74));
        doAnswer(i -> {
            llamadas.addObject().put("listarPorFiltros", conMarcador(i.getArgument(0)) + ", " + i.getArgument(1) + ", "
                    + i.getArgument(2));
            return i.<Boolean>getArgument(2) ? List.of(c71, c72, c74) : List.of(c71, c72, c73, c74);
        }).when(cursoCU).listarPorFiltros(any(), any(), anyBoolean());
        when(cursoCU.obtenerCursoPorId(any())).thenAnswer(i -> switch (i.<Integer>getArgument(0)) {
            case 71 -> c71;
            case 72 -> c72;
            case 73 -> c73;
            default -> throw new EntidadNoExisteException("No se encuentra el curso");
        });
        doAnswer(i -> {
            GestionarCursoOfertadoVeranoCUIntPort.DatosCreacion d = i.getArgument(0);
            // Las fechas en UTC: el toString del record depende de la zona de la JVM
            llamadas.addObject().put("crearCurso", d.idMateria() + ", " + d.idDocente() + ", " + d.cupoEstimado() + ", "
                    + d.fechaInicio().toInstant() + ", " + d.fechaFin().toInstant() + ", " + d.periodo() + ", "
                    + d.idSalon() + ", " + d.espacioAsignado() + ", " + d.grupo() + ", " + d.estado());
            return c74;
        }).when(cursoCU).crearCurso(any(GestionarCursoOfertadoVeranoCUIntPort.DatosCreacion.class));
        doAnswer(i -> {
            llamadas.addObject().put("crearCurso(curso)", String.valueOf(i.<CursoOfertadoVerano>getArgument(0).getCupoEstimado()));
            return c74;
        }).when(cursoCU).crearCurso(any(CursoOfertadoVerano.class));
        doAnswer(i -> {
            llamadas.addObject().put("actualizarCurso", String.valueOf(i.<CursoOfertadoVerano>getArgument(0).getIdCurso()));
            return c71;
        }).when(cursoCU).actualizarCurso(any(), any());
        doAnswer(i -> {
            llamadas.addObject().put("resolverSalon", i.getArgument(0) + ", " + i.getArgument(1));
            return i.getArgument(1);
        }).when(cursoCU).resolverSalon(any(), any());
        doAnswer(i -> {
            llamadas.addObject().put("modificarCurso", i.getArgument(0) + ", " + i.getArgument(1) + ", "
                    + i.getArgument(2) + ", " + i.getArgument(3));
            if ("Cerrado".equals(i.getArgument(3))) {
                throw new TransicionEstadoCurso.TransicionInvalidaException("Transicion invalida", "No se puede cerrar");
            }
            return i.<Integer>getArgument(0) == 71 ? c71 : null;
        }).when(cursoCU).modificarCurso(any(), any(), any(), any());
        doAnswer(i -> {
            llamadas.addObject().put("eliminarCurso", String.valueOf(i.<Integer>getArgument(0)));
            return true;
        }).when(cursoCU).eliminarCurso(any());

        SolicitudCursoVeranoPreinscripcion pre = new SolicitudCursoVeranoPreinscripcion();
        pre.setIdSolicitud(91);
        pre.setNombreSolicitud("Preinscripcion 91");
        pre.setPeriodoAcademico("2025-2");
        pre.setFechaRegistroSolicitud(INICIO);
        when(solicitudCU.buscarPreinscripcionesPorCurso(anyInt())).thenAnswer(i ->
                i.<Integer>getArgument(0) == 71 ? List.of(pre) : List.of());
        SolicitudCursoVeranoInscripcion ins = new SolicitudCursoVeranoInscripcion();
        ins.setIdSolicitud(92);
        ins.setNombreSolicitud("Inscripcion 92");
        ins.setPeriodoAcademico("2025-2");
        ins.setFechaRegistroSolicitud(INICIO);
        when(solicitudCU.buscarInscripcionesPorCurso(anyInt())).thenReturn(List.of(ins));
        when(solicitudCU.listarEstudiantesPorCurso(anyInt())).thenAnswer(i -> {
            llamadas.addObject().put("listarEstudiantesPorCurso", String.valueOf(i.<Integer>getArgument(0)));
            return new ListadoEstudiantes(List.of(), 0, 0);
        });
        doAnswer(i -> {
            llamadas.addObject().put("aprobarPreinscripcion", i.getArgument(0) + ", " + i.getArgument(1));
            return pre;
        }).when(solicitudCU).aprobarPreinscripcion(any(), any());
        doAnswer(i -> {
            llamadas.addObject().put("rechazarPreinscripcion", i.getArgument(0) + ", " + i.getArgument(1));
            return pre;
        }).when(solicitudCU).rechazarPreinscripcion(any(), any());
        doAnswer(i -> {
            llamadas.addObject().put("validarPago", i.getArgument(0) + ", " + i.getArgument(1) + ", " + i.getArgument(2));
            return ins;
        }).when(solicitudCU).validarPago(any(), anyBoolean(), any());
        doAnswer(i -> {
            llamadas.addObject().put("completarInscripcion", String.valueOf(i.<Integer>getArgument(0)));
            return ins;
        }).when(solicitudCU).completarInscripcion(any());

        Salon salon = new Salon(81, "221", "FIET", true);
        when(salonCU.resolverSalonDeCurso("221")).thenReturn(salon);

        doAnswer(i -> {
            llamadas.addObject().put("generarPdfCursos", i.getArgument(0) + ", " + i.getArgument(1));
            return "%PDF cursos".getBytes(StandardCharsets.US_ASCII);
        }).when(reportes).generarPdfCursos(any(), any());
        doAnswer(i -> {
            llamadas.addObject().put("generarPdfEstudiantes", String.valueOf(i.<Integer>getArgument(0)));
            return i.<Integer>getArgument(0) == 71 ? new GenerarReportesCursosVeranoCUIntPort.DocumentoEstudiantes(
                    "MAT-1", "%PDF estudiantes".getBytes(StandardCharsets.US_ASCII)) : null;
        }).when(reportes).generarPdfEstudiantes(any());
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void cursos() throws Exception {
        ObjectNode casos = json.createObjectNode();

        // Oferta para estudiantes
        for (String filtro : List.of("", "periodoAcademico=2025-1", "todosLosPeriodos=true",
                "periodoAcademico=todos&idPrograma=3")) {
            for (String quien : List.of("est5", "funcionario")) {
                caso(casos, quien, HttpMethod.GET, VIEJA + "/cursos-verano/disponibles" + (filtro.isEmpty() ? "" : "?" + filtro), null);
                caso(casos, quien, HttpMethod.GET, NUEVA + "?disponibles=true" + (filtro.isEmpty() ? "" : "&" + filtro), null);
            }
        }

        // Todos los cursos (staff)
        for (String filtro : List.of("", "periodoAcademico=2025-2", "periodoAcademico=todos", "idPrograma=3")) {
            for (String quien : List.of("funcionario", "coordinador", "est5")) {
                caso(casos, quien, HttpMethod.GET, VIEJA + "/cursos-verano/todos" + (filtro.isEmpty() ? "" : "?" + filtro), null);
                caso(casos, quien, HttpMethod.GET, NUEVA + (filtro.isEmpty() ? "" : "?" + filtro), null);
            }
        }

        // Cursos en preinscripción o en inscripción
        for (String[] estado : List.of(new String[] {"/cursos/preinscripcion", "preinscripcion"},
                new String[] {"/cursos/inscripcion", "inscripcion"})) {
            for (String filtro : List.of("", "periodoAcademico=2025-2", "periodoAcademico=todos")) {
                for (String quien : List.of("est5", "funcionario")) {
                    caso(casos, quien, HttpMethod.GET, VIEJA + estado[0] + (filtro.isEmpty() ? "" : "?" + filtro), null);
                    caso(casos, quien, HttpMethod.GET, NUEVA + "?estado=" + estado[1] + (filtro.isEmpty() ? "" : "&" + filtro), null);
                }
            }
        }

        // Exportar los cursos a PDF
        for (String filtro : List.of("", "periodoAcademico=2025-2&idPrograma=3")) {
            for (String quien : List.of("funcionario", "est5")) {
                caso(casos, quien, HttpMethod.GET, VIEJA + "/cursos-verano/todos/export/pdf" + (filtro.isEmpty() ? "" : "?" + filtro), null);
                caso(casos, quien, HttpMethod.GET, NUEVA + "?formato=pdf" + (filtro.isEmpty() ? "" : "&" + filtro), null);
            }
        }

        // Crear
        String valido = "{\"id_materia\":1,\"id_docente\":4,\"cupo_estimado\":25,\"fecha_inicio\":\"2025-07-01\","
                + "\"fecha_fin\":\"2025-08-01\",\"periodoAcademico\":\"2025-2\",\"espacio_asignado\":\"221\"}";
        String sinMateria = "{\"id_docente\":4,\"cupo_estimado\":25}";
        for (String ruta : List.of(VIEJA + "/cursos-verano", NUEVA)) {
            caso(casos, "funcionario", HttpMethod.POST, ruta, valido);
            caso(casos, "admin", HttpMethod.POST, ruta, valido);
            caso(casos, "funcionario", HttpMethod.POST, ruta, sinMateria);
            caso(casos, "est5", HttpMethod.POST, ruta, valido);
        }

        // Un curso: detalle, edición y borrado
        for (String ruta : List.of(VIEJA + "/cursos-verano/", NUEVA + "/")) {
            for (int id : new int[] {71, 72, 99}) {
                caso(casos, "est5", HttpMethod.GET, ruta + id, null);
            }
            caso(casos, "funcionario", HttpMethod.GET, ruta + 71, null);
            caso(casos, "funcionario", HttpMethod.PUT, ruta + 71, "{\"cupo_estimado\":25}");
            caso(casos, "funcionario", HttpMethod.PUT, ruta + 71, "{\"estado\":\"Cerrado\"}");
            caso(casos, "funcionario", HttpMethod.PUT, ruta + 71, "{\"estado\":\"otro\"}");
            caso(casos, "funcionario", HttpMethod.PUT, ruta + 71, "{}");
            caso(casos, "funcionario", HttpMethod.PUT, ruta + 72, "{\"cupo_estimado\":25}");
            caso(casos, "est5", HttpMethod.PUT, ruta + 71, "{\"cupo_estimado\":25}");
            caso(casos, "funcionario", HttpMethod.DELETE, ruta + 71, null);
            caso(casos, "funcionario", HttpMethod.DELETE, ruta + 73, null);
            caso(casos, "funcionario", HttpMethod.DELETE, ruta + 99, null);
            caso(casos, "est5", HttpMethod.DELETE, ruta + 71, null);
        }

        // Estudiantes del curso y su PDF
        for (String[] rutas : List.of(new String[] {"/estudiantes", "/estudiantes"},
                new String[] {"/estudiantes/export/pdf", "/estudiantes?formato=pdf"})) {
            for (String[] quienYId : List.of(new String[] {"funcionario", "71"}, new String[] {"funcionario", "99"},
                    new String[] {"est5", "71"})) {
                caso(casos, quienYId[0], HttpMethod.GET, VIEJA + "/cursos-verano/" + quienYId[1] + rutas[0], null);
                caso(casos, quienYId[0], HttpMethod.GET, NUEVA + "/" + quienYId[1] + rutas[1], null);
            }
        }

        // Se retiran: el listado sin uso, los filtros por período y por materia
        caso(casos, "funcionario", HttpMethod.GET, VIEJA + "/cursos-verano", null);
        caso(casos, "funcionario", HttpMethod.GET, VIEJA + "/cursos-verano?periodoAcademico=2025-1&idPrograma=3", null);
        caso(casos, "funcionario", HttpMethod.GET, VIEJA + "/cursos-verano/periodo/2025-1", null);
        caso(casos, "funcionario", HttpMethod.GET, VIEJA + "/cursos-verano/materia/1", null);

        // Se retira CursoOfertadoRestController entero
        String peticion = "{\"idCurso\":71,\"cupoEstimado\":30,\"salon\":\"221\"}";
        caso(casos, "funcionario", HttpMethod.POST, OFERTADO + "/crearCurso", peticion);
        caso(casos, "funcionario", HttpMethod.PUT, OFERTADO + "/actualizarCurso", peticion);
        caso(casos, "funcionario", HttpMethod.GET, OFERTADO + "/disponibles", null);
        caso(casos, "funcionario", HttpMethod.GET, OFERTADO + "/todos", null);
        caso(casos, "funcionario", HttpMethod.GET, OFERTADO + "/preinscripciones/71", null);
        caso(casos, "funcionario", HttpMethod.GET, OFERTADO + "/inscripciones/71", null);
        caso(casos, "funcionario", HttpMethod.PUT, OFERTADO + "/preinscripciones/91/aprobar", "{\"comentarios\":\"ok\"}");
        caso(casos, "funcionario", HttpMethod.PUT, OFERTADO + "/preinscripciones/91/rechazar", "{\"motivo\":\"no\"}");
        caso(casos, "funcionario", HttpMethod.PUT, OFERTADO + "/inscripciones/92/validar-pago", "{\"esValido\":true}");
        caso(casos, "funcionario", HttpMethod.PUT, OFERTADO + "/inscripciones/92/completar", null);

        // Solo rutas nuevas: lo que la convención decide aparte
        caso(casos, "funcionario", HttpMethod.GET, NUEVA + "?disponibles=true&estado=preinscripcion", null);
        caso(casos, "funcionario", HttpMethod.GET, NUEVA + "?disponibles=false", null);
        caso(casos, "funcionario", HttpMethod.GET, NUEVA + "?estado=otro", null);
        caso(casos, "funcionario", HttpMethod.GET, NUEVA + "?estado=Preinscripcion", null);
        caso(casos, "funcionario", HttpMethod.GET, NUEVA + "?formato=xml", null);
        caso(casos, "funcionario", HttpMethod.GET, NUEVA + "/abc", null);
        caso(casos, "funcionario", HttpMethod.GET, NUEVA + "/abc/estudiantes", null);
        caso(casos, "funcionario", HttpMethod.GET, NUEVA + "/71/estudiantes?formato=xml", null);

        // EST-7: las rutas nuevas responden como las antiguas, que pasan a 404
        GrafoObjetos.compararConReferencia(casos, "rutas/cursos-verano.json", "rutas/correcciones/cursos-verano-est7.json");
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
            case "coordinador" -> "Coordinador";
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
        String disposicion = respuesta.getHeader(HttpHeaders.CONTENT_DISPOSITION);
        caso.put("disposicion", disposicion == null ? null : disposicion.replaceAll("\\d{4}-\\d{2}-\\d{2}", "<hoy>"));
        caso.set("llamadas", llamadas.deepCopy());
        casos.set(clave, caso);
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
        Rol objRol = new Rol();
        objRol.setNombre(rol);
        usuario.setObjRol(objRol);
        return usuario;
    }

    private static CursoOfertadoVerano curso(int id, String estado, String periodo, String salon) {
        Materia materia = new Materia();
        materia.setIdMateria(1);
        materia.setCodigo("MAT-1");
        materia.setNombre("Calculo");
        materia.setCreditos(4);
        Docente docente = new Docente();
        docente.setIdDocente(4);
        docente.setCodigoDocente("D-4");
        docente.setNombreDocente("Docente Cuatro");
        CursoOfertadoVerano curso = new CursoOfertadoVerano();
        curso.setIdCurso(id);
        curso.setObjMateria(materia);
        curso.setObjDocente(docente);
        curso.setCupoEstimado(20);
        curso.setSalon(salon);
        curso.setFecha_inicio(INICIO);
        curso.setFecha_fin(FIN);
        curso.setPeriodoAcademico(periodo);
        EstadoCursoOfertado e = new EstadoCursoOfertado();
        e.setIdEstado(id * 10);
        e.setEstadoActual(estado);
        e.setFechaRegistroEstado(INICIO);
        curso.setEstadosCursoOfertados(new ArrayList<>(List.of(e)));
        return curso;
    }
}
