package co.edu.unicauca.decanatura.gestion_curricular.verano;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.SoporteMvc;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.CondicionSolicitudVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.GrupoCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.ReportesCursosVeranoRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.formateador.GeneradorExcelSolicitudesVerano;

/**
 * Fija el JSON de {@code /api/cursos-verano/reportes/solicitudes} y el contenido del Excel que comparte su
 * formato ({@code ReporteSolicitudesVeranoMapper}) antes de tiparlos (ARQ-10). El Excel se genera en UTC para
 * que las fechas no dependan de la zona de la máquina; la fecha de generación y el período actual se comprueban
 * y se sustituyen por marcadores.
 */
class SolicitudesVeranoReporteCaracterizacionTest {
    private static final long MARZO = 1710522000000L;

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void conservaElListadoYElExcel() throws Exception {
        var cursos = mock(GestionarCursoOfertadoVeranoCUIntPort.class);
        var solicitudes = mock(GestionarSolicitudCursoVeranoCUIntPort.class);
        when(solicitudes.listarSolicitudesParaReporte(any(), any(), any(), any(), any())).thenAnswer(i -> datos());
        MockMvc mvc = SoporteMvc.standaloneSetup(new ReportesCursosVeranoRestController(cursos, solicitudes, null))
            .setControllerAdvice(new RestApiExceptionHandler()).build();
        ObjectNode resultados = json.createObjectNode();

        String ruta = "/api/cursos-verano/reportes/solicitudes";
        registrar(resultados, "todas", mvc, get(ruta));
        registrar(resultados, "materia 101", mvc, get(ruta).param("idMateria", "101"));
        registrar(resultados, "materia 0", mvc, get(ruta).param("idMateria", "0"));
        registrar(resultados, "materia inexistente", mvc, get(ruta).param("idMateria", "999"));

        GeneradorExcelSolicitudesVerano generador = new GeneradorExcelSolicitudesVerano();
        TimeZone zona = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            excel(resultados, "excel sin filtros", generador.generarExcelSolicitudes(datos(), null, null, null,
                null, null));
            excel(resultados, "excel con filtros", generador.generarExcelSolicitudes(datos(), 5, "Enviada",
                "2024-1", new Date(MARZO), new Date(MARZO + 86_400_000L)));
            excel(resultados, "excel solo fin", generador.generarExcelSolicitudes(new ArrayList<>(), null, " ",
                "todos", null, new Date(MARZO)));
        } finally {
            TimeZone.setDefault(zona);
        }

        Path referencia = Path.of("src/test/resources/referencias/verano-endpoints/solicitudes-reporte.json");
        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(referencia)) {
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(resultados));
        }
        assertThat(resultados).isEqualTo(json.readTree(Files.readString(referencia)));
    }

    private void excel(ObjectNode resultados, String nombre, byte[] contenido) throws Exception {
        ArrayNode filas = resultados.putArray(nombre);
        try (XSSFWorkbook libro = new XSSFWorkbook(new ByteArrayInputStream(contenido))) {
            Sheet hoja = libro.getSheetAt(0);
            filas.add("hoja:" + hoja.getSheetName());
            for (Row fila : hoja) {
                List<String> celdas = new ArrayList<>();
                for (Cell celda : fila) {
                    celdas.add(celda.getColumnIndex() + "=" + normalizar(celda.getStringCellValue()));
                }
                filas.add(fila.getRowNum() + ": " + String.join(" | ", celdas));
            }
        }
    }

    private static String normalizar(String texto) {
        if (texto.startsWith("Fecha de generacion: ")) {
            assertThat(texto).matches("Fecha de generacion: \\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}");
            return "Fecha de generacion: <ahora>";
        }
        PeriodoAcademicoEnum actual = PeriodoAcademicoEnum.getPeriodoActual();
        String periodo = "Período actual: " + (actual == null ? "" : actual.getValor()) + ")";
        if (actual != null && texto.contains(periodo)) {
            return texto.replace(periodo, "Período actual: <periodo actual>)");
        }
        return texto;
    }

    private void registrar(ObjectNode resultados, String nombre, MockMvc mvc, MockHttpServletRequestBuilder peticion)
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

    private static List<Solicitud> datos() {
        List<Solicitud> lista = new ArrayList<>();
        SolicitudCursoVeranoPreinscripcion completa = solicitud(SolicitudCursoVeranoPreinscripcion::new, 1,
            "Preinscripción", "Aprobado");
        completa.setObservacion("Quiero ver la materia");
        completa.setCondicionSolicitud(CondicionSolicitudVerano.Habilitación);
        completa.setObjCursoOfertadoVerano(curso(1, GrupoCursoVerano.A, true));
        lista.add(completa);
        SolicitudCursoVeranoPreinscripcion nueva = solicitud(SolicitudCursoVeranoPreinscripcion::new, 2, null, null);
        nueva.setObservacion("Solicitud de apertura de curso: Cálculo III");
        lista.add(nueva);
        SolicitudCursoVeranoPreinscripcion sinDatos = solicitud(SolicitudCursoVeranoPreinscripcion::new, 3,
            "Solo nombre", null);
        sinDatos.setObservacion("  ");
        sinDatos.setObjUsuario(null);
        sinDatos.setObjCursoOfertadoVerano(curso(2, null, false));
        lista.add(sinDatos);
        lista.add(new SolicitudEcaes());
        SolicitudCursoVeranoInscripcion inscripcion = solicitud(SolicitudCursoVeranoInscripcion::new, 4,
            "Inscripción", "Pago_Validado");
        inscripcion.setCondicionSolicitud(CondicionSolicitudVerano.Primera_Vez);
        inscripcion.setObjCursoOfertadoVerano(curso(1, GrupoCursoVerano.B, true));
        lista.add(inscripcion);
        SolicitudCursoVeranoInscripcion sinCurso = solicitud(SolicitudCursoVeranoInscripcion::new, 5, "  ", null);
        sinCurso.setObservacion("observada");
        lista.add(sinCurso);
        return lista;
    }

    private static <T extends Solicitud> T solicitud(Supplier<T> nueva, int id, String nombre, String estado) {
        T solicitud = nueva.get();
        solicitud.setIdSolicitud(id);
        solicitud.setNombreSolicitud(nombre);
        solicitud.setFechaRegistroSolicitud(id % 2 == 0 ? null : new Date(MARZO + id * 60_000L));
        if (estado != null) {
            EstadoSolicitud registro = new EstadoSolicitud();
            registro.setEstadoActual(estado);
            solicitud.getEstadosSolicitud().add(registro);
        }
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto("Estudiante " + id);
        usuario.setCodigo("1046" + id);
        solicitud.setObjUsuario(usuario);
        return solicitud;
    }

    private static CursoOfertadoVerano curso(int id, GrupoCursoVerano grupo, boolean conMateria) {
        CursoOfertadoVerano curso = new CursoOfertadoVerano();
        curso.setIdCurso(id);
        curso.setGrupo(grupo);
        if (conMateria) {
            Materia materia = new Materia();
            materia.setIdMateria(100 + id);
            materia.setCodigo("MAT" + id);
            materia.setNombre("Materia " + id);
            curso.setObjMateria(materia);
        } else {
            curso.setObjMateria(null);
        }
        return curso;
    }
}
