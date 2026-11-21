package co.edu.unicauca.decanatura.gestion_curricular.verano;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocentesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarMateriasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSalonesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Salon;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.CatalogosCursosVeranoRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.Correcciones;

/**
 * Fija los catálogos de {@link CatalogosCursosVeranoRestController} antes de tiparlos (ARQ-10): materias, salones,
 * docentes y permisos por estado. {@code claves} lista cada clave con su ruta y tipo JSON en orden de escritura.
 */
class CatalogosVeranoRespuestasCaracterizacionTest {
    private static final String BASE = "/api/cursos-intersemestrales";

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void conservaLosCatalogos() throws Exception {
        var materias = mock(GestionarMateriasCUIntPort.class);
        var docentes = mock(GestionarDocentesCUIntPort.class);
        var salones = mock(GestionarSalonesCUIntPort.class);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new CatalogosCursosVeranoRestController(materias, docentes,
                salones))
            .setControllerAdvice(new RestApiExceptionHandler()).build();
        ObjectNode resultados = json.createObjectNode();

        Materia sinDatos = new Materia();
        sinDatos.setId_materia(3);
        when(materias.listarMaterias()).thenReturn(List.of(materia(1, "MAT1", "Cálculo", 4),
            materia(2, "FIS2", "Física", 3), sinDatos));
        when(salones.listarSalonesActivos()).thenReturn(List.of(new Salon(1, "305", "FIET", true),
            new Salon(2, "aula b", null, null), new Salon(3, "221", "  ", false), new Salon(4, null, "Ing", true),
            new Salon(5, "Aula A", "Salud", null)));
        Docente sinNombre = new Docente();
        sinNombre.setId_docente(9);
        when(docentes.listarDocentes()).thenReturn(List.of(docente(1, "D01", "Ana"), sinNombre));
        for (String ruta : new String[] {"/materias-disponibles", "/materias", "/materias-filtro", "/salones",
            "/docentes"}) {
            registrar(resultados, ruta + " con datos", mvc, BASE + ruta);
        }

        when(materias.listarMaterias()).thenReturn(new ArrayList<>());
        when(salones.listarSalonesActivos()).thenReturn(new ArrayList<>());
        when(docentes.listarDocentes()).thenReturn(new ArrayList<>());
        for (String ruta : new String[] {"/materias-disponibles", "/materias", "/materias-filtro", "/salones",
            "/docentes"}) {
            registrar(resultados, ruta + " vacio", mvc, BASE + ruta);
        }
        when(docentes.listarDocentes()).thenReturn(null);
        registrar(resultados, "/docentes nulo", mvc, BASE + "/docentes");

        when(materias.listarMaterias()).thenThrow(new IllegalStateException("consulta"));
        when(salones.listarSalonesActivos()).thenThrow(new IllegalStateException("consulta"));
        when(docentes.listarDocentes()).thenThrow(new IllegalStateException("consulta"));
        for (String ruta : new String[] {"/materias-disponibles", "/materias", "/materias-filtro", "/salones",
            "/docentes"}) {
            registrar(resultados, ruta + " falla", mvc, BASE + ruta);
        }

        String[][] permisos = {{"Borrador", "FUNCIONARIO"}, {"Borrador", "ESTUDIANTE"}, {"Cerrado", "ESTUDIANTE"},
            {"Publicado", "ADMIN"}, {"Desconocido", "FUNCIONARIO"}, {"Inscripcion", "COORDINADOR"}};
        for (String[] permiso : permisos) {
            registrar(resultados, "permisos " + permiso[0] + " " + permiso[1], mvc,
                BASE + "/permisos-estado/" + permiso[0] + "/" + permiso[1]);
        }

        Path referencia = Path.of("src/test/resources/referencias/verano-endpoints/catalogos-respuestas.json");
        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(referencia)) {
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(resultados));
        }
        // BUG-15: la consulta de permisos devuelve las operaciones del rol
        assertThat(resultados).isEqualTo(Correcciones.aplicar(json.readTree(Files.readString(referencia)),
            "verano-endpoints/correcciones/catalogos-respuestas-bug15.json"));
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

    private static Materia materia(int id, String codigo, String nombre, int creditos) {
        Materia materia = new Materia();
        materia.setId_materia(id);
        materia.setCodigo(codigo);
        materia.setNombre(nombre);
        materia.setCreditos(creditos);
        return materia;
    }

    private static Docente docente(int id, String codigo, String nombre) {
        Docente docente = new Docente();
        docente.setId_docente(id);
        docente.setCodigo_docente(codigo);
        docente.setNombre_docente(nombre);
        return docente;
    }
}
