package co.edu.unicauca.decanatura.gestion_curricular.verano;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.ReportesCursosVeranoRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.Correcciones;

/**
 * Fija {@code /api/cursos-intersemestrales/dashboard/estadisticas} y {@code /seguimiento/{idUsuario}} antes de
 * tiparlos (ARQ-10). {@code claves} lista cada clave con su ruta y tipo JSON en orden de escritura.
 */
class SeguimientoDashboardVeranoCaracterizacionTest {
    private static final long MARZO = 1710522000000L;
    private static final int COMPLETO = 1;
    private static final int VACIO = 2;
    private static final int CURSO_SIN_MATERIA = 3;
    private static final int FALLA = 4;

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void conservaDashboardYSeguimiento() throws Exception {
        var cursos = mock(GestionarCursoOfertadoVeranoCUIntPort.class);
        var solicitudes = mock(GestionarSolicitudCursoVeranoCUIntPort.class);
        when(solicitudes.buscarSolicitudesPorUsuario(any())).thenAnswer(i -> preinscripciones(i.getArgument(0)));
        when(solicitudes.buscarInscripcionesPorUsuario(any())).thenAnswer(i -> inscripciones(i.getArgument(0)));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new ReportesCursosVeranoRestController(cursos, solicitudes, null))
            .setControllerAdvice(new RestApiExceptionHandler()).build();

        ObjectNode resultados = json.createObjectNode();
        when(cursos.listarTodos()).thenReturn(new ArrayList<>());
        when(solicitudes.listarSolicitudesParaReporte(any(), any(), any(), any(), any())).thenReturn(new ArrayList<>());
        registrar(resultados, "dashboard sin datos", mvc, "/api/cursos-intersemestrales/dashboard/estadisticas");

        when(cursos.listarTodos()).thenReturn(List.of(curso(1, "Publicado"), curso(2, "Preinscripcion"),
            curso(3, "Inscripcion"), curso(4, "Cerrado"), curso(5, null), curso(6, "Borrador")));
        List<Solicitud> reporte = new ArrayList<>();
        reporte.add(new SolicitudCursoVeranoPreinscripcion());
        reporte.add(new SolicitudCursoVeranoPreinscripcion());
        reporte.add(new SolicitudCursoVeranoInscripcion());
        reporte.add(new SolicitudEcaes());
        when(solicitudes.listarSolicitudesParaReporte(any(), any(), any(), any(), any())).thenReturn(reporte);
        registrar(resultados, "dashboard con datos", mvc, "/api/cursos-intersemestrales/dashboard/estadisticas");

        for (int id : new int[] {COMPLETO, VACIO, CURSO_SIN_MATERIA, FALLA}) {
            registrar(resultados, "seguimiento " + id, mvc, "/api/cursos-intersemestrales/seguimiento/" + id);
        }

        Path referencia = Path.of("src/test/resources/referencias/verano-endpoints/seguimiento-dashboard.json");
        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(referencia)) {
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(resultados));
        }
        // BUG-31: una preinscripción con curso sin materia ya no vacía la lista del usuario 3
        // BUG-8: una preinscripción rechazada con curso ofrece "revisar_motivo_rechazo"
        assertThat(resultados).isEqualTo(Correcciones.aplicar(Correcciones.aplicar(
            json.readTree(Files.readString(referencia)),
            "verano-endpoints/correcciones/seguimiento-dashboard-bug31.json"),
            "verano-endpoints/correcciones/seguimiento-dashboard-bug8.json"));
    }

    private void registrar(ObjectNode resultados, String nombre, MockMvc mvc, String ruta) throws Exception {
        var respuesta = mvc.perform(get(ruta)).andReturn().getResponse();
        byte[] cuerpo = respuesta.getContentAsByteArray();
        JsonNode body = cuerpo.length == 0 ? json.nullNode() : json.readTree(cuerpo);
        ObjectNode resultado = resultados.putObject(nombre);
        resultado.put("status", respuesta.getStatus());
        resultado.set("body", body);
        ArrayNode claves = resultado.putArray("claves");
        recorrer(body, "", claves);
    }

    private static void recorrer(JsonNode nodo, String ruta, ArrayNode claves) {
        if (nodo.isObject()) {
            nodo.fields().forEachRemaining(campo -> {
                String hija = ruta + "." + campo.getKey();
                claves.add(hija + ":" + campo.getValue().getNodeType());
                recorrer(campo.getValue(), hija, claves);
            });
        } else if (nodo.isArray()) {
            for (int i = 0; i < nodo.size(); i++) {
                recorrer(nodo.get(i), ruta + "[" + i + "]", claves);
            }
        }
    }

    private static List<SolicitudCursoVeranoPreinscripcion> preinscripciones(Integer idUsuario) {
        List<SolicitudCursoVeranoPreinscripcion> lista = new ArrayList<>();
        switch (idUsuario) {
            case COMPLETO -> {
                lista.add(preinscripcion(10, "Preinscripción", null, curso(1, "Inscripcion"), "Aprobado"));
                lista.add(preinscripcion(11, "Preinscripción", null, curso(2, "Preinscripcion"), "Aprobado"));
                lista.add(preinscripcion(12, "Preinscripción", null, curso(3, null), "Enviada"));
                lista.add(preinscripcion(13, "Preinscripción", null, curso(4, "Publicado"), "Rechazado"));
                lista.add(preinscripcion(14, "Preinscripción", null, curso(5, "Publicado"), null));
                lista.add(preinscripcion(15, "Solicitud", "Solicitud de apertura: Cálculo III", null, "Enviada"));
                lista.add(preinscripcion(16, "Curso: Física II", null, null, "Aprobado"));
                lista.add(preinscripcion(17, "Otra", "sin separador", null, "Rechazado"));
                lista.add(preinscripcion(18, null, null, null, null));
            }
            case CURSO_SIN_MATERIA -> {
                lista.add(preinscripcion(20, "Preinscripción", null, curso(1, "Publicado"), "Enviada"));
                CursoOfertadoVerano sinMateria = curso(2, "Publicado");
                sinMateria.setObjMateria(null);
                lista.add(preinscripcion(21, "Preinscripción", null, sinMateria, "Enviada"));
            }
            case FALLA -> throw new IllegalStateException("consulta");
            default -> { }
        }
        return lista;
    }

    private static List<SolicitudCursoVeranoInscripcion> inscripciones(Integer idUsuario) {
        List<SolicitudCursoVeranoInscripcion> lista = new ArrayList<>();
        switch (idUsuario) {
            case COMPLETO -> {
                String[] estados = {"Enviada", "Pago_Validado", "Inscripcion_Completada", "Curso_Aprobado",
                    "Curso_Finalizado", "Pago_Rechazado", "Otro", null};
                for (int i = 0; i < estados.length; i++) {
                    SolicitudCursoVeranoInscripcion inscripcion = solicitud(SolicitudCursoVeranoInscripcion::new,
                        30 + i, "Inscripción " + i, estados[i]);
                    inscripcion.setObjCursoOfertadoVerano(i == 6 ? null : curso(i + 1, "Inscripcion"));
                    lista.add(inscripcion);
                }
                SolicitudCursoVeranoInscripcion sinMateria = solicitud(SolicitudCursoVeranoInscripcion::new, 40,
                    null, "Enviada");
                CursoOfertadoVerano curso = curso(9, "Inscripcion");
                curso.setObjMateria(null);
                sinMateria.setObjCursoOfertadoVerano(curso);
                lista.add(sinMateria);
            }
            case FALLA -> throw new IllegalStateException("consulta");
            default -> { }
        }
        return lista;
    }

    private static SolicitudCursoVeranoPreinscripcion preinscripcion(int id, String nombre, String observacion,
            CursoOfertadoVerano curso, String estado) {
        SolicitudCursoVeranoPreinscripcion preinscripcion = solicitud(SolicitudCursoVeranoPreinscripcion::new, id,
            nombre, estado);
        preinscripcion.setObservacion(observacion);
        preinscripcion.setObjCursoOfertadoVerano(curso);
        return preinscripcion;
    }

    private static <T extends Solicitud> T solicitud(Supplier<T> nueva, int id, String nombre, String estado) {
        T solicitud = nueva.get();
        solicitud.setId_solicitud(id);
        solicitud.setNombre_solicitud(nombre);
        solicitud.setFecha_registro_solicitud(id % 2 == 0 ? new Date(MARZO) : null);
        List<EstadoSolicitud> estados = new ArrayList<>();
        if (estado != null) {
            EstadoSolicitud registro = new EstadoSolicitud();
            registro.setEstado_actual(estado);
            estados.add(registro);
        }
        solicitud.setEstadosSolicitud(estados);
        return solicitud;
    }

    private static CursoOfertadoVerano curso(int id, String estado) {
        CursoOfertadoVerano curso = new CursoOfertadoVerano();
        curso.setId_curso(id);
        Materia materia = new Materia();
        materia.setId_materia(100 + id);
        materia.setNombre("Materia " + id);
        curso.setObjMateria(materia);
        List<EstadoCursoOfertado> estados = new ArrayList<>();
        if (estado != null) {
            EstadoCursoOfertado registro = new EstadoCursoOfertado();
            registro.setEstado_actual("Borrador");
            estados.add(registro);
            EstadoCursoOfertado ultimo = new EstadoCursoOfertado();
            ultimo.setEstado_actual(estado);
            estados.add(ultimo);
        }
        curso.setEstadosCursoOfertados(estados);
        return curso;
    }
}
