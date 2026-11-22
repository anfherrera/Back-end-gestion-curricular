package co.edu.unicauca.decanatura.gestion_curricular.verano;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.SoporteMvc;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.CondicionSolicitudVerano;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.PreinscripcionesVeranoRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.CursosOfertadosMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.DocenteMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.ProgramaMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.RolMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudCursoDeVeranoPreinscripcionMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.UsuarioMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.Correcciones;

/**
 * Fija las respuestas con mapa de {@link PreinscripcionesVeranoRestController} antes de tiparlas (ARQ-10).
 * {@code claves} lista cada clave con su ruta y tipo JSON en orden de escritura; incluye claves que el mapa omitía
 * y claves que escribía con null.
 */
class PreinscripcionesVeranoRespuestasCaracterizacionTest {
    private static final long MARZO = 1710522000000L;
    private static final String BASE = "/api/cursos-intersemestrales";

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void conservaLasRespuestasDePreinscripciones() throws Exception {
        var cu = mock(GestionarSolicitudCursoVeranoCUIntPort.class);
        when(cu.buscarSolicitudesPorUsuario(anyInt())).thenAnswer(i -> porUsuario(i.getArgument(0)));
        when(cu.buscarPreinscripcionesPorCurso(anyInt())).thenAnswer(i -> porCurso(i.getArgument(0)));
        when(cu.crearSolicitudCursoVeranoPreinscripcion(any())).thenAnswer(i -> {
            SolicitudCursoVeranoPreinscripcion recibida = i.getArgument(0);
            String observacion = recibida.getObservacion();
            if (observacion.endsWith("invalido")) {
                throw new IllegalArgumentException("curso invalido");
            }
            if (observacion.endsWith("falla")) {
                throw new IllegalStateException("consulta");
            }
            SolicitudCursoVeranoPreinscripcion guardada = new SolicitudCursoVeranoPreinscripcion();
            guardada.setId_solicitud(50);
            guardada.setFecha_registro_solicitud(new Date(MARZO));
            return guardada;
        });
        when(cu.buscarPreinscripcionPorUsuarioYCurso(anyInt(), anyInt()))
            .thenAnswer(i -> (int) i.getArgument(1) == 2 ? new SolicitudCursoVeranoPreinscripcion() : null);
        when(cu.buscarSolicitudPorId(anyInt())).thenAnswer(i -> {
            int id = i.getArgument(0);
            return id == 9 ? null : preinscripcion(id, "Aprobado", null, null, null);
        });
        when(cu.actualizarObservacionesPreinscripcion(any())).thenAnswer(i -> i.getArgument(0));
        when(cu.aprobarPreinscripcion(anyInt(), nullable(String.class)))
            .thenAnswer(i -> preinscripcion(i.getArgument(0), "Aprobado", null, null, null));
        when(cu.rechazarPreinscripcion(anyInt(), nullable(String.class)))
            .thenAnswer(i -> preinscripcion(i.getArgument(0), "Rechazado", null, null, null));
        MockMvc mvc = SoporteMvc.standaloneSetup(new PreinscripcionesVeranoRestController(cu, mapper(),
                mock(SolicitudCursoDeVeranoPreinscripcionMapperDominio.class)))
            .setControllerAdvice(new RestApiExceptionHandler()).build();
        ObjectNode resultados = json.createObjectNode();

        for (int id : new int[] {1, 2, 3, 4}) {
            registrar(resultados, "usuario " + id, mvc, get(BASE + "/preinscripciones/usuario/" + id));
        }
        registrar(resultados, "usuario alias", mvc, get(BASE + "/solicitudes/preinscripcion/estudiante/1"));

        String nuevo = "{\"nombreCompleto\":\"Ana Ruiz\",\"codigo\":\"1046\",\"condicion\":\"Primera_Vez\","
            + "\"idUsuario\":7,\"curso\":\"";
        ObjectNode creado = registrar(resultados, "curso nuevo", mvc,
            json(post(BASE + "/solicitudes-curso-nuevo"), nuevo + "Cálculo\"}"));
        JsonNode fecha = creado.get("body").get("fecha");
        if (fecha.isTextual() && fecha.asText().equals(new Date(MARZO).toString())) {
            ((ObjectNode) creado.get("body")).put("fecha", "<fecha de registro>");
        }
        registrar(resultados, "curso nuevo sin usuario", mvc, json(post(BASE + "/solicitudes-curso-nuevo"),
            "{\"nombreCompleto\":null,\"condicion\":\"Repitencia\",\"curso\":\"X\"}"));
        registrar(resultados, "curso nuevo condicion invalida", mvc, json(post(BASE + "/solicitudes-curso-nuevo"),
            nuevo.replace("Primera_Vez", "Otra") + "Cálculo\"}"));
        registrar(resultados, "curso nuevo invalido", mvc,
            json(post(BASE + "/solicitudes-curso-nuevo"), nuevo + "invalido\"}"));
        registrar(resultados, "curso nuevo falla", mvc,
            json(post(BASE + "/solicitudes-curso-nuevo"), nuevo + "falla\"}"));

        registrar(resultados, "preinscribir sin curso", mvc,
            json(post(BASE + "/cursos-verano/preinscripciones"), "{\"idUsuario\":7}"));
        registrar(resultados, "preinscribir duplicada", mvc,
            json(post(BASE + "/cursos-verano/preinscripciones"), "{\"idUsuario\":7,\"idCurso\":2}"));
        registrar(resultados, "preinscribir alias sin usuario", mvc,
            json(post(BASE + "/solicitudes/preinscripcion"), "{\"idCurso\":3}"));

        for (int id : new int[] {1, 2, 4}) {
            registrar(resultados, "curso " + id, mvc, get(BASE + "/preinscripciones/curso/" + id));
        }

        registrar(resultados, "observaciones", mvc,
            json(put(BASE + "/preinscripciones/1/observaciones"), "{\"observaciones\":\"  revisar  \"}"));
        registrar(resultados, "observaciones vacias", mvc,
            json(put(BASE + "/preinscripciones/1/observaciones"), "{\"observaciones\":\"  \"}"));
        registrar(resultados, "observaciones inexistente", mvc,
            json(put(BASE + "/preinscripciones/9/observaciones"), "{\"observaciones\":\"x\"}"));
        registrar(resultados, "aprobar", mvc, json(put(BASE + "/preinscripciones/5/aprobar"), "{}"));
        registrar(resultados, "aprobar sin cuerpo", mvc, put(BASE + "/preinscripciones/5/aprobar"));
        registrar(resultados, "rechazar", mvc,
            json(put(BASE + "/preinscripciones/5/rechazar"), "{\"motivo\":\"  cupo  \"}"));
        registrar(resultados, "rechazar sin motivo", mvc,
            json(put(BASE + "/preinscripciones/5/rechazar"), "{\"motivo\":\" \"}"));

        Path referencia = Path.of("src/test/resources/referencias/verano-endpoints/preinscripciones-respuestas.json");
        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(referencia)) {
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(resultados));
        }
        // BUG-32: la referencia fija el Date.toString() de "curso nuevo"; la corrección, la fecha como Date
        assertThat(resultados).isEqualTo(Correcciones.aplicar(json.readTree(Files.readString(referencia)),
            "verano-endpoints/correcciones/preinscripciones-respuestas-bug32.json"));
    }

    private static RequestBuilder json(MockHttpServletRequestBuilder peticion, String contenido) {
        return peticion.contentType(MediaType.APPLICATION_JSON).content(contenido);
    }

    private ObjectNode registrar(ObjectNode resultados, String nombre, MockMvc mvc, RequestBuilder peticion)
            throws Exception {
        var respuesta = mvc.perform(peticion).andReturn().getResponse();
        byte[] cuerpo = respuesta.getContentAsByteArray();
        JsonNode body = cuerpo.length == 0 ? json.nullNode() : json.readTree(cuerpo);
        ObjectNode resultado = resultados.putObject(nombre);
        resultado.put("status", respuesta.getStatus());
        resultado.set("body", body);
        ArrayNode claves = resultado.putArray("claves");
        recorrer(body, "", claves);
        return resultado;
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

    private static CursosOfertadosMapperDominioImpl mapper() {
        var usuarios = new UsuarioMapperDominioImpl();
        ReflectionTestUtils.setField(usuarios, "programaMapperDominio", new ProgramaMapperDominioImpl());
        ReflectionTestUtils.setField(usuarios, "rolMapperDominio", new RolMapperDominioImpl());
        var mapper = new CursosOfertadosMapperDominioImpl();
        ReflectionTestUtils.setField(mapper, "docenteMapperDominio", new DocenteMapperDominioImpl());
        ReflectionTestUtils.setField(mapper, "usuarioMapperDominio", usuarios);
        return mapper;
    }

    private static List<SolicitudCursoVeranoPreinscripcion> porUsuario(int idUsuario) {
        List<SolicitudCursoVeranoPreinscripcion> lista = new ArrayList<>();
        switch (idUsuario) {
            case 1 -> {
                lista.add(preinscripcion(10, "Aprobado", "  ok  ", curso(1, "Cálculo"), usuario(true)));
                lista.add(preinscripcion(11, "Rechazado", "   ", curso(2, null), usuario(false)));
                CursoOfertadoVerano sinMateria = curso(3, "x");
                sinMateria.setObjMateria(null);
                lista.add(preinscripcion(12, null, null, sinMateria, usuario(true)));
                lista.add(preinscripcion(13, "Enviada", null, null, usuario(true)));
                CursoOfertadoVerano sinId = curso(4, "Física");
                sinId.setId_curso(null);
                lista.add(preinscripcion(14, "Otro", "nota", sinId, usuario(true)));
            }
            case 3 -> lista.add(preinscripcion(15, "Aprobado", null, curso(1, "Cálculo"), null));
            case 4 -> throw new IllegalStateException("consulta");
            default -> { }
        }
        return lista;
    }

    private static List<SolicitudCursoVeranoPreinscripcion> porCurso(int idCurso) {
        List<SolicitudCursoVeranoPreinscripcion> lista = new ArrayList<>();
        switch (idCurso) {
            case 1 -> {
                SolicitudCursoVeranoPreinscripcion completa = preinscripcion(20, "Aprobado", null,
                    curso(1, "Cálculo"), usuario(true));
                completa.setObservacion("observada");
                completa.setCodigo_estudiante("1046");
                completa.setCodicion_solicitud(CondicionSolicitudVerano.Primera_Vez);
                lista.add(completa);
                lista.add(preinscripcion(21, null, null, null, usuario(false)));
                lista.add(preinscripcion(22, "Rechazado", null, curso(2, null), null));
            }
            case 4 -> throw new IllegalStateException("consulta");
            default -> { }
        }
        return lista;
    }

    private static SolicitudCursoVeranoPreinscripcion preinscripcion(int id, String estado, String comentario,
            CursoOfertadoVerano curso, Usuario usuario) {
        SolicitudCursoVeranoPreinscripcion preinscripcion = new SolicitudCursoVeranoPreinscripcion();
        preinscripcion.setId_solicitud(id);
        preinscripcion.setFecha_registro_solicitud(id % 2 == 0 ? new Date(MARZO) : null);
        if (estado != null) {
            EstadoSolicitud registro = new EstadoSolicitud();
            registro.setEstado_actual(estado);
            registro.setComentario(comentario);
            preinscripcion.getEstadosSolicitud().add(registro);
        }
        preinscripcion.setObjCursoOfertadoVerano(curso);
        preinscripcion.setObjUsuario(usuario);
        return preinscripcion;
    }

    private static Usuario usuario(boolean conRol) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(7);
        usuario.setNombreCompleto("Ana Ruiz");
        usuario.setCorreo("ana@unicauca.edu.co");
        usuario.setCodigo("1046");
        if (conRol) {
            Rol rol = new Rol();
            rol.setIdRol(2);
            rol.setNombre("Estudiante");
            usuario.setObjRol(rol);
        }
        return usuario;
    }

    private static CursoOfertadoVerano curso(int id, String nombreMateria) {
        CursoOfertadoVerano curso = new CursoOfertadoVerano();
        curso.setId_curso(id);
        Materia materia = new Materia();
        materia.setIdMateria(100 + id);
        materia.setCodigo("MAT" + id);
        materia.setNombre(nombreMateria);
        materia.setCreditos(3);
        curso.setObjMateria(materia);
        Docente docente = new Docente();
        docente.setIdDocente(200 + id);
        docente.setNombreDocente("Docente " + id);
        curso.setObjDocente(docente);
        curso.setCupo_estimado(20);
        curso.setSalon("221");
        curso.setPeriodo_academico("2024-1");
        curso.setFecha_inicio(new Date(MARZO));
        curso.setFecha_fin(new Date(MARZO + 86_400_000L * 40));
        EstadoCursoOfertado estado = new EstadoCursoOfertado();
        estado.setEstado_actual("Preinscripcion");
        estado.setFecha_registro_estado(new Date(MARZO));
        curso.getEstadosCursoOfertados().add(estado);
        return curso;
    }
}
