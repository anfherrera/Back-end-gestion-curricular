package co.edu.unicauca.decanatura.gestion_curricular.verano;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.SoporteMvc;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.CupoAgotadoException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.CursoOfertadoRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.CursosOfertadosMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.EstadoCursoOfertadoMapper;

/**
 * Fija las respuestas con mapa de {@code /api/cursos-verano} antes de tiparlas (ARQ-10). {@code claves} lista
 * cada clave con su ruta y tipo JSON en orden de escritura.
 */
class CursoOfertadoRespuestasCaracterizacionTest {
    private static final long MARZO = 1710522000000L;
    private static final int CON_DATOS = 1;
    private static final int VACIO = 2;
    private static final int NULO = 3;
    private static final int FALLA = 4;

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void conservaLasRespuestasDeCursosOfertados() throws Exception {
        var cursos = mock(GestionarCursoOfertadoVeranoCUIntPort.class);
        var cu = mock(GestionarSolicitudCursoVeranoCUIntPort.class);
        when(cu.buscarPreinscripcionesPorCurso(anyInt())).thenAnswer(i -> preinscripciones(i.getArgument(0)));
        when(cu.buscarInscripcionesPorCurso(anyInt())).thenAnswer(i -> inscripciones(i.getArgument(0)));
        when(cu.aprobarPreinscripcion(anyInt(), nullable(String.class)))
            .thenAnswer(i -> segun(i.getArgument(0), () -> preinscripcion(i.getArgument(0))));
        when(cu.rechazarPreinscripcion(anyInt(), nullable(String.class)))
            .thenAnswer(i -> segun(i.getArgument(0), () -> preinscripcion(i.getArgument(0))));
        when(cu.validarPago(anyInt(), anyBoolean(), nullable(String.class))).thenAnswer(i -> {
            if ("cupo".equals(i.getArgument(2))) {
                throw new CupoAgotadoException(3, 3);
            }
            return segun(i.getArgument(0), () -> inscripcion(i.getArgument(0)));
        });
        when(cu.completarInscripcion(anyInt()))
            .thenAnswer(i -> segun(i.getArgument(0), () -> inscripcion(i.getArgument(0))));
        MockMvc mvc = SoporteMvc.standaloneSetup(new CursoOfertadoRestController(cursos, cu,
                mock(CursosOfertadosMapperDominio.class), mock(EstadoCursoOfertadoMapper.class)))
            .setControllerAdvice(new RestApiExceptionHandler()).build();

        ObjectNode resultados = json.createObjectNode();
        String base = "/api/cursos-verano";
        for (int id : new int[] {CON_DATOS, VACIO, FALLA}) {
            registrar(resultados, "preinscripciones " + id, mvc, get(base + "/preinscripciones/" + id));
            registrar(resultados, "inscripciones " + id, mvc, get(base + "/inscripciones/" + id));
        }
        for (int id : new int[] {CON_DATOS, NULO, FALLA}) {
            registrar(resultados, "aprobar " + id, mvc, cuerpo(put(base + "/preinscripciones/" + id + "/aprobar"),
                "{\"comentarios\":\"  nota  \"}"));
            registrar(resultados, "rechazar " + id, mvc, cuerpo(put(base + "/preinscripciones/" + id + "/rechazar"),
                "{\"motivo\":\"  sin cupo  \"}"));
            registrar(resultados, "validar pago " + id, mvc,
                cuerpo(put(base + "/inscripciones/" + id + "/validar-pago"), "{\"esValido\":true}"));
            registrar(resultados, "rechazar pago " + id, mvc,
                cuerpo(put(base + "/inscripciones/" + id + "/validar-pago"), "{\"esValido\":false,\"observaciones\":\"x\"}"));
            registrar(resultados, "completar " + id, mvc, put(base + "/inscripciones/" + id + "/completar"));
        }
        registrar(resultados, "aprobar sin cuerpo", mvc, put(base + "/preinscripciones/1/aprobar"));
        registrar(resultados, "rechazar sin motivo", mvc,
            cuerpo(put(base + "/preinscripciones/1/rechazar"), "{\"motivo\":\"   \"}"));
        registrar(resultados, "validar pago sin decision", mvc,
            cuerpo(put(base + "/inscripciones/1/validar-pago"), "{\"observaciones\":\"x\"}"));
        registrar(resultados, "validar pago sin cupo", mvc,
            cuerpo(put(base + "/inscripciones/1/validar-pago"), "{\"esValido\":true,\"observaciones\":\"cupo\"}"));

        Path referencia = Path.of("src/test/resources/referencias/verano-endpoints/curso-ofertado-respuestas.json");
        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(referencia)) {
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(resultados));
        }
        assertThat(resultados).isEqualTo(json.readTree(Files.readString(referencia)));
    }

    private static RequestBuilder cuerpo(MockHttpServletRequestBuilder peticion, String contenido) {
        return peticion.contentType(MediaType.APPLICATION_JSON).content(contenido);
    }

    private void registrar(ObjectNode resultados, String nombre, MockMvc mvc, RequestBuilder peticion)
            throws Exception {
        var respuesta = mvc.perform(peticion).andReturn().getResponse();
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

    private static <T> T segun(int id, Supplier<T> valor) {
        if (id == FALLA) {
            throw new IllegalStateException("consulta");
        }
        return id == NULO ? null : valor.get();
    }

    private static List<SolicitudCursoVeranoPreinscripcion> preinscripciones(int idCurso) {
        if (idCurso == FALLA) {
            throw new IllegalStateException("consulta");
        }
        List<SolicitudCursoVeranoPreinscripcion> lista = new ArrayList<>();
        if (idCurso == CON_DATOS) {
            lista.add(preinscripcion(10));
            lista.add(preinscripcion(11));
        }
        return lista;
    }

    private static List<SolicitudCursoVeranoInscripcion> inscripciones(int idCurso) {
        if (idCurso == FALLA) {
            throw new IllegalStateException("consulta");
        }
        List<SolicitudCursoVeranoInscripcion> lista = new ArrayList<>();
        if (idCurso == CON_DATOS) {
            lista.add(inscripcion(20));
            lista.add(inscripcion(21));
        }
        return lista;
    }

    private static SolicitudCursoVeranoPreinscripcion preinscripcion(int id) {
        SolicitudCursoVeranoPreinscripcion preinscripcion = solicitud(SolicitudCursoVeranoPreinscripcion::new, id);
        preinscripcion.setNombre_estudiante("Estudiante " + id);
        preinscripcion.setCodigo_estudiante("1046" + id);
        preinscripcion.setObservacion(id % 2 == 0 ? "observacion" : null);
        return preinscripcion;
    }

    private static SolicitudCursoVeranoInscripcion inscripcion(int id) {
        SolicitudCursoVeranoInscripcion inscripcion = solicitud(SolicitudCursoVeranoInscripcion::new, id);
        inscripcion.setNombre_estudiante("Estudiante " + id);
        return inscripcion;
    }

    private static <T extends Solicitud> T solicitud(Supplier<T> nueva, int id) {
        T solicitud = nueva.get();
        solicitud.setIdSolicitud(id);
        solicitud.setNombreSolicitud("Solicitud " + id);
        solicitud.setPeriodoAcademico("2024-1");
        solicitud.setFechaRegistroSolicitud(new Date(MARZO));
        EstadoSolicitud estado = new EstadoSolicitud();
        estado.setIdEstado(id);
        estado.setEstadoActual("Enviada");
        estado.setFechaRegistroEstado(new Date(MARZO));
        solicitud.getEstadosSolicitud().add(estado);
        CursoOfertadoVerano curso = new CursoOfertadoVerano();
        curso.setId_curso(5);
        Materia materia = new Materia();
        materia.setIdMateria(105);
        materia.setNombre("Materia 5");
        curso.setObjMateria(materia);
        solicitud.setObjCursoOfertadoVerano(curso);
        return solicitud;
    }
}
