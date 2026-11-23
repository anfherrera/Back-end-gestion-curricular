package co.edu.unicauca.decanatura.gestion_curricular.verano;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import co.edu.unicauca.decanatura.gestion_curricular.soporte.SoporteMvc;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.CupoAgotadoException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.CondicionSolicitudVerano;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.CursosVeranoRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.InscripcionesVeranoRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.Correcciones;

/**
 * Fija las respuestas con mapa de {@link InscripcionesVeranoRestController} antes de tiparlas (ARQ-10).
 * {@code claves} lista cada clave con su ruta y tipo JSON en orden de escritura. Las marcas de la hora actual se
 * comprueban y se sustituyen por marcadores, y las claves de {@code archivo_pago} (hecho con {@code Map.of}, cuyo
 * orden cambia entre arranques) se ordenan.
 */
class InscripcionesVeranoRespuestasCaracterizacionTest {
    private static final long MARZO = 1710522000000L;
    private static final String BASE = "/api/cursos-intersemestrales";
    /** EST-7, paso 8b: las claves conservan las operaciones; lo retirado sigue pidiendo BASE y responde 404. */
    private static final String NUEVA = "/api/cursos-verano";

    private final ObjectMapper json = new ObjectMapper();
    private long inicio;

    @Test
    void conservaLasRespuestasDeInscripciones() throws Exception {
        var cursos = mock(GestionarCursoOfertadoVeranoCUIntPort.class);
        var cu = mock(GestionarSolicitudCursoVeranoCUIntPort.class);
        var general = mock(GestionarSolicitudCUIntPort.class);
        when(cu.buscarPreinscripcionAprobada(anyInt(), anyInt())).thenAnswer(i -> {
            if ((int) i.getArgument(1) == 1) {
                return null;
            }
            SolicitudCursoVeranoPreinscripcion aprobada = new SolicitudCursoVeranoPreinscripcion();
            aprobada.setObjCursoOfertadoVerano(curso(i.getArgument(1), "Cálculo"));
            aprobada.setObjUsuario(usuario());
            return aprobada;
        });
        when(cu.tieneInscripcionActiva(anyInt(), anyInt())).thenAnswer(i -> (int) i.getArgument(1) == 2);
        doAnswer(i -> {
            CursoOfertadoVerano curso = i.getArgument(0);
            if (curso.getIdCurso() == 3) {
                throw new CupoAgotadoException(3, 3);
            }
            return null;
        }).when(cu).verificarCupoDisponible(any());
        when(cu.crearSolicitudCursoVeranoInscripcion(any())).thenAnswer(i -> {
            SolicitudCursoVeranoInscripcion nueva = i.getArgument(0);
            if (nueva.getObjCursoOfertadoVerano().getIdCurso() == 4) {
                return null;
            }
            SolicitudCursoVeranoInscripcion guardada = inscripcion(60, "Enviada");
            return guardada;
        });
        when(cursos.asociarEstudiante(anyInt(), anyInt())).thenReturn(true);
        when(general.obtenerSolicitudPorId(anyInt())).thenAnswer(i -> switch ((int) i.getArgument(0)) {
            case 1 -> null;
            case 2 -> inscripcion(2, "Aprobado");
            default -> inscripcion(i.getArgument(0), "Enviada");
        });
        when(cu.buscarInscripcionesPorCurso(anyInt())).thenAnswer(i -> elegibles(i.getArgument(0)));
        when(cu.buscarPorIdInscripcion(anyInt())).thenAnswer(i -> porId(i.getArgument(0)));
        doAnswer(i -> {
            SolicitudCursoVeranoInscripcion inscripcion = i.getArgument(0);
            if (inscripcion.getIdSolicitud() == 4) {
                throw new CupoAgotadoException(5, 5);
            }
            return null;
        }).when(cu).verificarCupoParaAceptar(any());
        when(cu.validarPago(anyInt(), anyBoolean(), nullable(String.class))).thenAnswer(i -> {
            int id = i.getArgument(0);
            boolean valido = i.getArgument(1);
            if (id == 2) {
                return null;
            }
            if (id == 7 && !valido) {
                throw new IllegalStateException("no se pudo rechazar");
            }
            return inscripcion(id, valido ? "Pago_Validado" : "Pago_Rechazado");
        });
        when(cu.contarInscripcionesPorEstado(anyInt(), anyString())).thenAnswer(i -> {
            if ((int) i.getArgument(0) == 2) {
                return null;
            }
            return switch ((String) i.getArgument(1)) {
                case "Enviada" -> 2;
                case "Pago_Validado" -> 3;
                default -> 1;
            };
        });
        // EST-7 (paso 8b): los elegibles pasan a CursosVeranoRestController; lo retirado responde 404
        MockMvc mvc = SoporteMvc.standaloneSetup(new InscripcionesVeranoRestController(cursos, cu),
                new CursosVeranoRestController(cursos, cu, null, null, null))
            .setControllerAdvice(new RestApiExceptionHandler()).build();
        ObjectNode resultados = json.createObjectNode();
        inicio = System.currentTimeMillis();

        for (int idCurso = 1; idCurso <= 5; idCurso++) {
            ObjectNode creada = registrar(resultados, "crear curso " + idCurso, mvc,
                json(post(NUEVA + "/inscripciones"),
                    "{\"idUsuario\":7,\"idCurso\":" + idCurso + ",\"nombreSolicitud\":\"Inscripción\"}"),
                idCurso == 2);
            assertThat(creada.get("status").asInt()).isPositive();
        }

        for (int id : new int[] {1, 2, 3}) {
            ObjectNode cancelada = registrar(resultados, "cancelar " + id, mvc, delete(BASE + "/inscripciones/" + id),
                false);
            marcarHora(cancelada, "timestamp");
        }

        for (int idCurso : new int[] {1, 2}) {
            registrar(resultados, "elegibles " + idCurso, mvc,
                get(NUEVA + "/cursos/" + idCurso + "/estudiantes-elegibles"), false);
        }

        for (int id : new int[] {1, 2, 3, 4, 5}) {
            ObjectNode aceptada = registrar(resultados, "aceptar " + id, mvc,
                json(put(NUEVA + "/inscripciones/" + id + "/estado"), "{\"nuevoEstado\":\"aceptada\",\"observaciones\":\"  \"}"), false);
            marcarFecha(aceptada, "fecha_aceptacion");
        }
        ObjectNode conObservacion = registrar(resultados, "aceptar con observaciones", mvc,
            json(put(NUEVA + "/inscripciones/3/estado"), "{\"nuevoEstado\":\"aceptada\",\"observaciones\":\" pago ok \"}"), false);
        marcarFecha(conObservacion, "fecha_aceptacion");

        for (int id : new int[] {1, 2, 3, 6, 7}) {
            ObjectNode rechazada = registrar(resultados, "rechazar " + id, mvc,
                json(put(NUEVA + "/inscripciones/" + id + "/estado"), "{\"nuevoEstado\":\"rechazada\",\"motivo\":\"  sin pago  \"}"), false);
            marcarFecha(rechazada, "fecha_rechazo");
        }
        ObjectNode sinMotivo = registrar(resultados, "rechazar sin motivo", mvc,
            json(put(NUEVA + "/inscripciones/3/estado"), "{\"nuevoEstado\":\"rechazada\"}"), false);
        marcarFecha(sinMotivo, "fecha_rechazo");

        registrar(resultados, "confirmar", mvc, put(BASE + "/inscripciones/9/confirmar"), false);

        for (int id : new int[] {1, 3, 8, 10, 11}) {
            registrar(resultados, "info " + id, mvc, get(BASE + "/inscripciones/" + id + "/info"), false);
        }

        for (int idCurso : new int[] {1, 2}) {
            ObjectNode estadisticas = registrar(resultados, "estadisticas " + idCurso, mvc,
                get(BASE + "/inscripciones/curso/" + idCurso + "/estadisticas"), false);
            marcarFecha(estadisticas, "fecha_consulta");
        }

        Path referencia = Path.of("src/test/resources/referencias/verano-endpoints/inscripciones-respuestas.json");
        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(referencia)) {
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(resultados));
        }
        // BUG-23: el error inesperado al rechazar es un 500 genérico y ya no expone su mensaje
        JsonNode esperado = Correcciones.aplicar(json.readTree(Files.readString(referencia)),
            "verano-endpoints/correcciones/inscripciones-respuestas-bug23.json");
        // BUG-34: documentos_count siempre numérico y archivo_pago admite valores null
        esperado = Correcciones.aplicar(esperado, "verano-endpoints/correcciones/inscripciones-respuestas-bug34.json");
        // BUG-35: los elegibles llevan su comprobante real (o null) y los incompletos aparecen con datos null
        esperado = Correcciones.aplicar(esperado, "verano-endpoints/correcciones/inscripciones-respuestas-bug35.json");
        // BUG-33: confirmar no está implementado y responde 501 sin cuerpo
        esperado = Correcciones.aplicar(esperado, "verano-endpoints/correcciones/inscripciones-respuestas-bug33.json");
        // EST-7 (paso 8b): cancelar, confirmar, la información y las estadísticas se retiran (404)
        esperado = Correcciones.aplicar(esperado, "verano-endpoints/correcciones/inscripciones-respuestas-est7-paso8b.json");
        assertThat(resultados).isEqualTo(esperado);
    }

    /** Sustituye una fecha {@code Date} (milisegundos) de la hora actual, tras comprobar que es de esta ejecución. */
    private void marcarFecha(ObjectNode resultado, String clave) {
        JsonNode body = resultado.get("body");
        if (body.isObject() && body.has(clave)) {
            assertThat(body.get(clave).asLong()).isBetween(inicio, System.currentTimeMillis());
            ((ObjectNode) body).put(clave, "<ahora>");
        }
    }

    /** Sustituye un {@code LocalDateTime.now().toString()}, tras comprobar que es de esta ejecución. */
    private void marcarHora(ObjectNode resultado, String clave) {
        JsonNode body = resultado.get("body");
        if (body.isObject() && body.has(clave)) {
            LocalDateTime hora = LocalDateTime.parse(body.get(clave).asText());
            assertThat(hora).isAfter(LocalDateTime.now().minusMinutes(10)).isBefore(LocalDateTime.now().plusSeconds(1));
            ((ObjectNode) body).put(clave, "<ahora>");
        }
    }

    private static RequestBuilder json(MockHttpServletRequestBuilder peticion, String contenido) {
        return peticion.contentType(MediaType.APPLICATION_JSON).content(contenido);
    }

    private ObjectNode registrar(ObjectNode resultados, String nombre, MockMvc mvc, RequestBuilder peticion,
            boolean ordenarClaves) throws Exception {
        var respuesta = mvc.perform(peticion).andReturn().getResponse();
        byte[] cuerpo = respuesta.getContentAsByteArray();
        JsonNode body = cuerpo.length == 0 ? json.nullNode() : json.readTree(cuerpo);
        ObjectNode resultado = resultados.putObject(nombre);
        resultado.put("status", respuesta.getStatus());
        resultado.set("body", body);
        List<String> claves = new ArrayList<>();
        recorrer(body, "", claves);
        if (ordenarClaves) {
            claves.sort(null);
        }
        ArrayNode lista = resultado.putArray("claves");
        claves.forEach(lista::add);
        return resultado;
    }

    private static void recorrer(JsonNode nodo, String ruta, List<String> claves) {
        if (nodo.isObject()) {
            Map<String, JsonNode> campos = new LinkedHashMap<>();
            if (ruta.endsWith(".archivo_pago")) {
                campos = new TreeMap<>();
            }
            Map<String, JsonNode> destino = campos;
            nodo.fields().forEachRemaining(campo -> destino.put(campo.getKey(), campo.getValue()));
            destino.forEach((clave, valor) -> {
                String hija = ruta + "." + clave;
                claves.add(hija + ":" + valor.getNodeType());
                recorrer(valor, hija, claves);
            });
        } else if (nodo.isArray()) {
            for (int i = 0; i < nodo.size(); i++) {
                recorrer(nodo.get(i), ruta + "[" + i + "]", claves);
            }
        }
    }

    private static List<SolicitudCursoVeranoInscripcion> elegibles(int idCurso) {
        List<SolicitudCursoVeranoInscripcion> lista = new ArrayList<>();
        if (idCurso != 1) {
            return lista;
        }
        SolicitudCursoVeranoInscripcion normal = inscripcion(30, "Enviada");
        normal.setCondicionSolicitud(CondicionSolicitudVerano.Primera_Vez);
        normal.getDocumentos().add(documento(70, "comprobante.pdf", "/r/comprobante.pdf", true));
        lista.add(normal);
        lista.add(inscripcion(31, "Pago_Validado"));
        lista.add(inscripcion(32, null));
        SolicitudCursoVeranoInscripcion sinCurso = inscripcion(33, "Pago_Rechazado");
        sinCurso.setObjCursoOfertadoVerano(null);
        lista.add(sinCurso);
        SolicitudCursoVeranoInscripcion cursoSinDatos = inscripcion(34, "Enviada");
        cursoSinDatos.setObjCursoOfertadoVerano(curso(null, null));
        lista.add(cursoSinDatos);
        SolicitudCursoVeranoInscripcion sinUsuario = inscripcion(35, "Enviada");
        sinUsuario.setObjUsuario(null);
        lista.add(sinUsuario);
        SolicitudCursoVeranoInscripcion sinMateria = inscripcion(36, "Enviada");
        sinMateria.getObjCursoOfertadoVerano().setObjMateria(null);
        lista.add(sinMateria);
        return lista;
    }

    private static SolicitudCursoVeranoInscripcion porId(int id) {
        switch (id) {
            case 1:
                return null;
            case 5: {
                SolicitudCursoVeranoInscripcion sinCurso = inscripcion(5, "Enviada");
                sinCurso.setObjCursoOfertadoVerano(null);
                return sinCurso;
            }
            case 6:
                return inscripcion(6, "Aprobado");
            case 7:
                return inscripcion(7, "Pago_Validado");
            case 8: {
                SolicitudCursoVeranoInscripcion conDocumentos = inscripcion(8, "Enviada");
                conDocumentos.getDocumentos().add(documento(80, "pago.pdf", "/r/pago.pdf", true));
                conDocumentos.getDocumentos().add(documento(81, null, null, false));
                return conDocumentos;
            }
            case 10: {
                SolicitudCursoVeranoInscripcion sinLista = inscripcion(10, "Enviada");
                sinLista.setDocumentos(null);
                return sinLista;
            }
            case 11: {
                SolicitudCursoVeranoInscripcion sinRuta = inscripcion(11, "Enviada");
                sinRuta.getDocumentos().add(documento(90, "pago.pdf", null, true));
                return sinRuta;
            }
            default:
                return inscripcion(id, "Enviada");
        }
    }

    private static Documento documento(int id, String nombre, String ruta, boolean valido) {
        Documento documento = new Documento();
        documento.setIdDocumento(id);
        documento.setNombre(nombre);
        documento.setRutaDocumento(ruta);
        documento.setEsValido(valido);
        documento.setFechaDocumento(new Date(MARZO));
        return documento;
    }

    private static SolicitudCursoVeranoInscripcion inscripcion(int id, String estado) {
        SolicitudCursoVeranoInscripcion inscripcion = new SolicitudCursoVeranoInscripcion();
        inscripcion.setIdSolicitud(id);
        inscripcion.setNombreSolicitud("Inscripción " + id);
        inscripcion.setFechaRegistroSolicitud(id % 2 == 0 ? new Date(MARZO) : null);
        if (estado != null) {
            EstadoSolicitud registro = new EstadoSolicitud();
            registro.setEstadoActual(estado);
            inscripcion.getEstadosSolicitud().add(registro);
        }
        inscripcion.setObjUsuario(usuario());
        inscripcion.setObjCursoOfertadoVerano(curso(5, "Cálculo"));
        return inscripcion;
    }

    private static Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(7);
        usuario.setNombreCompleto("Ana Ruiz");
        usuario.setCorreo("ana@unicauca.edu.co");
        usuario.setCodigo("1046");
        return usuario;
    }

    private static CursoOfertadoVerano curso(Integer id, String nombreMateria) {
        CursoOfertadoVerano curso = new CursoOfertadoVerano();
        curso.setIdCurso(id);
        Materia materia = new Materia();
        materia.setNombre(nombreMateria);
        curso.setObjMateria(materia);
        return curso;
    }
}
