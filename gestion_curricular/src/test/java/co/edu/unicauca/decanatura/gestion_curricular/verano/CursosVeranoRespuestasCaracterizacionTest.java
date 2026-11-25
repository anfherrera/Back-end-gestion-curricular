package co.edu.unicauca.decanatura.gestion_curricular.verano;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
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

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.SoporteMvc;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort.DatosCreacion;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSalonesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.DatosCursoInvalidosException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.TransicionEstadoCurso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.CursosVeranoRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.CursosOfertadosMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.DocenteMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.ProgramaMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.RolMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.UsuarioMapperDominioImpl;

/**
 * Fija las respuestas con mapa de {@link CursosVeranoRestController} antes de tiparlas (ARQ-10): {@code salonInfo}
 * del listado, detalle, creación, edición y borrado. {@code claves} lista cada clave con su ruta y tipo JSON en
 * orden de escritura; en los cuerpos hechos con {@code Map.of} se ordena, porque su orden cambia entre arranques.
 */
class CursosVeranoRespuestasCaracterizacionTest {
    private static final long MARZO = 1710522000000L;
    private static final String BASE = "/api/cursos-verano/cursos";

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void conservaLasRespuestasDeCursos() throws Exception {
        var cursos = mock(GestionarCursoOfertadoVeranoCUIntPort.class);
        var solicitudes = mock(GestionarSolicitudCursoVeranoCUIntPort.class);
        var salones = mock(GestionarSalonesCUIntPort.class);
        when(salones.resolverSalonDeCurso(nullable(String.class))).thenAnswer(i -> salon(i.getArgument(0)));
        when(cursos.listarPorFiltros(isNull(), isNull(), anyBoolean()))
            .thenReturn(List.of(curso(1, "221"), curso(2, "305"), curso(3, null), curso(4, "999")));
        when(cursos.obtenerCursoPorId(anyInt())).thenAnswer(i -> {
            int id = i.getArgument(0);
            if (id == 9) {
                return null;
            }
            CursoOfertadoVerano curso = curso(id, id == 2 ? null : "221");
            if (id == 3) {
                curso.setObjMateria(null);
                curso.setObjDocente(null);
            }
            if (id == 4) {
                curso.getEstudiantesInscritos().add(new Usuario());
                curso.getEstudiantesInscritos().add(new Usuario());
            }
            return curso;
        });
        when(solicitudes.buscarPreinscripcionesPorCurso(anyInt())).thenAnswer(i -> {
            if ((int) i.getArgument(0) == 2) {
                throw new IllegalStateException("consulta");
            }
            return List.of(new SolicitudCursoVeranoPreinscripcion(), new SolicitudCursoVeranoPreinscripcion());
        });
        when(cursos.eliminarCurso(anyInt())).thenAnswer(i -> (int) i.getArgument(0) != 5);
        when(cursos.crearCurso(any(DatosCreacion.class))).thenAnswer(i -> {
            DatosCreacion datos = i.getArgument(0);
            if (datos.cupoEstimado() <= DatosCursoInvalidosException.Motivo.values().length) {
                var motivo = DatosCursoInvalidosException.Motivo.values()[datos.cupoEstimado() - 1];
                throw new DatosCursoInvalidosException(motivo, "motivo " + motivo);
            }
            CursoOfertadoVerano curso = curso(datos.cupoEstimado(), "221");
            if (datos.cupoEstimado() == 50) {
                curso.setObjMateria(null);
                curso.setObjDocente(null);
            }
            return curso;
        });
        when(cursos.resolverSalon(nullable(Integer.class), nullable(String.class))).thenAnswer(i -> {
            Integer idSalon = i.getArgument(0);
            if (idSalon != null && idSalon == 404) {
                throw new DatosCursoInvalidosException(DatosCursoInvalidosException.Motivo.SALON_NO_ENCONTRADO,
                    "Salón 404 no existe");
            }
            String espacio = i.getArgument(1);
            return idSalon != null ? "221" : espacio == null ? null : espacio.trim();
        });
        when(cursos.modificarCurso(anyInt(), nullable(Integer.class), nullable(String.class), nullable(String.class)))
            .thenAnswer(i -> {
                int id = i.getArgument(0);
                if ("Cerrado".equals(i.getArgument(3))) {
                    throw new TransicionEstadoCurso.TransicionInvalidaException("Transicion invalida", "No se puede");
                }
                if (id == 9) {
                    return null;
                }
                CursoOfertadoVerano curso = curso(id, i.getArgument(2));
                if (id == 3) {
                    curso.setObjMateria(null);
                }
                return curso;
            });

        MockMvc mvc = SoporteMvc.standaloneSetup(new CursosVeranoRestController(cursos, solicitudes, mapper(),
                salones, null))
            .setControllerAdvice(new RestApiExceptionHandler()).build();

        ObjectNode resultados = json.createObjectNode();
        registrar(resultados, "todos", mvc, get(BASE), false);
        for (int id : new int[] {1, 2, 3}) {
            registrar(resultados, "detalle " + id, mvc, get(BASE + "/" + id), false);
        }

        String valido = "\"id_materia\":7,\"id_docente\":8,\"fecha_inicio\":\"2025-07-01\","
            + "\"fecha_fin\":\"2025-08-15T10:00:00Z\",\"periodoAcademico\":\" 2025-2 \",\"id_salon\":81";
        registrar(resultados, "crear", mvc, json(post(BASE), "{" + valido + ",\"cupo_estimado\":30}"), false);
        registrar(resultados, "crear sin materia ni docente", mvc,
            json(post(BASE), "{" + valido + ",\"cupo_estimado\":50}"), false);
        for (int cupo = 1; cupo <= DatosCursoInvalidosException.Motivo.values().length; cupo++) {
            registrar(resultados, "crear motivo " + cupo, mvc, json(post(BASE), "{" + valido + ",\"cupo_estimado\":"
                + cupo + "}"), true);
        }
        registrar(resultados, "crear docente cero", mvc, json(post(BASE), "{\"id_docente\":0}"), true);
        registrar(resultados, "crear sin materia", mvc, json(post(BASE), "{\"id_docente\":3}"), true);
        registrar(resultados, "crear fecha invalida", mvc, json(post(BASE),
            "{" + valido.replace("2025-07-01", "01/07/2025") + ",\"cupo_estimado\":30}"), true);
        ObjectNode periodo = registrar(resultados, "crear periodo invalido", mvc, json(post(BASE),
            "{" + valido.replace(" 2025-2 ", "2025-3") + ",\"cupo_estimado\":30}"), true);
        ObjectNode cuerpoPeriodo = (ObjectNode) periodo.get("body");
        assertThat(json.convertValue(cuerpoPeriodo.get("periodosValidos"), String[].class))
            .containsExactly(PeriodoAcademicoEnum.getPeriodosRecientes());
        cuerpoPeriodo.put("periodosValidos", "<periodos recientes>");

        String[][] ediciones = {
            {"editar cupo", "1", "{\"cupo_estimado\":25}"},
            {"editar salon", "1", "{\"id_salon\":81,\"estado\":\"Publicado\"}"},
            {"editar sin materia ni salon", "3", "{\"espacio_asignado\":\"999 \"}"},
            {"editar inexistente", "9", "{\"cupo_estimado\":25}"},
            {"editar cupo invalido", "1", "{\"cupo_estimado\":0}"},
            {"editar espacio corto", "1", "{\"espacio_asignado\":\" a \"}"},
            {"editar sin cambios", "1", "{}"},
            {"editar transicion invalida", "1", "{\"estado\":\"Cerrado\"}"},
        };
        for (String[] edicion : ediciones) {
            registrar(resultados, edicion[0], mvc, json(put(BASE + "/" + edicion[1]), edicion[2]), false);
        }
        registrar(resultados, "editar estado invalido", mvc, json(put(BASE + "/1"), "{\"estado\":\"Otro\"}"), true);
        registrar(resultados, "editar salon inexistente", mvc, json(put(BASE + "/1"), "{\"id_salon\":404}"), true);

        for (int id : new int[] {1, 3, 4, 5, 9}) {
            registrar(resultados, "eliminar " + id, mvc, delete(BASE + "/" + id), false);
        }

        Path referencia = Path.of("src/test/resources/referencias/verano-endpoints/cursos-verano-respuestas.json");
        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(referencia)) {
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(resultados));
        }
        assertThat(resultados).isEqualTo(json.readTree(Files.readString(referencia)));
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

    private static Salon salon(String numero) {
        if (numero == null) {
            return null;
        }
        return switch (numero.trim()) {
            case "221" -> new Salon(81, "221", "FIET", true);
            case "305" -> new Salon(82, "305", null, null);
            default -> null;
        };
    }

    private static CursoOfertadoVerano curso(int id, String salon) {
        CursoOfertadoVerano curso = new CursoOfertadoVerano();
        curso.setIdCurso(id);
        Materia materia = new Materia();
        materia.setIdMateria(100 + id);
        materia.setCodigo("MAT" + id);
        materia.setNombre("Materia " + id);
        materia.setCreditos(3);
        curso.setObjMateria(materia);
        Docente docente = new Docente();
        docente.setIdDocente(200 + id);
        docente.setCodigoDocente("D" + id);
        docente.setNombreDocente("Docente " + id);
        curso.setObjDocente(docente);
        curso.setCupoEstimado(20 + id);
        curso.setSalon(salon);
        curso.setPeriodoAcademico("2024-1");
        curso.setFecha_inicio(new Date(MARZO));
        curso.setFecha_fin(new Date(MARZO + 86_400_000L * 40));
        EstadoCursoOfertado estado = new EstadoCursoOfertado();
        estado.setIdEstado(id);
        estado.setEstadoActual("Publicado");
        estado.setFechaRegistroEstado(new Date(MARZO));
        curso.setEstadosCursoOfertados(new ArrayList<>(Arrays.asList(estado)));
        return curso;
    }
}
