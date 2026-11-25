package co.edu.unicauca.decanatura.gestion_curricular.verano;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.*;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.*;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.*;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.*;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.Correcciones;

/** HTTP y JSON completos antes de ARQ-6. Casos de uso reales, salidas con datos fijos.
 * No normaliza valores: los ids y las fechas de los datos son constantes.
 * Compara árboles JSON, por lo que el orden de campos de Map.of no interviene.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles({"test", "caracterizacion-verano"})
@org.springframework.security.test.context.support.WithMockUser(roles = "Funcionario")
class CursosVeranoEndpointsCaracterizacionTest {
    /** EST-7, pasos 8a y 8c: los subrecursos de cursos y de reportes. Las claves conservan las rutas antiguas. */
    private static final String CURSOS = "/api/cursos-verano/cursos";
    private static final String REPORTES = "/api/cursos-verano/reportes";
    private static final Path REFERENCIAS = Path.of("src/test/resources/referencias/verano-endpoints");
    private static final ObjectMapper JSON = new ObjectMapper();
    @Autowired MockMvc mvc;
    @Autowired Datos datos;

    static class Datos {
        final GestionarCursoOfertadoVeranoGatewayIntPort cursos = mock(GestionarCursoOfertadoVeranoGatewayIntPort.class);
        final GestionarSalonesIntPort salones = mock(GestionarSalonesIntPort.class);
        final GestionarSolicitudCursoVeranoGatewayIntPort solicitudes = mock(GestionarSolicitudCursoVeranoGatewayIntPort.class);

        void preparar(String numero, String modo) {
            reset(cursos, salones, solicitudes);
            CursoOfertadoVerano curso = new CursoOfertadoVerano();
            curso.setIdCurso(71);
            curso.setSalon(numero);
            curso.setCupoEstimado(20);
            curso.setPeriodoAcademico("2025-2");
            curso.setFecha_inicio(Date.from(Instant.parse("2025-07-01T12:00:00Z")));
            when(cursos.listarTodos()).thenReturn(List.of(curso));
            when(cursos.listarPorPeriodo(anyString())).thenReturn(List.of(curso));
            when(cursos.obtenerCursoPorId(71)).thenReturn(curso);
            when(cursos.guardarCambiosCurso(eq(71), any(), any(), any())).thenReturn(curso);
            SolicitudCursoVeranoPreinscripcion solicitud = new SolicitudCursoVeranoPreinscripcion();
            solicitud.setIdSolicitud(91);
            solicitud.setNombreSolicitud("Curso de prueba");
            solicitud.setFechaRegistroSolicitud(curso.getFecha_inicio());
            solicitud.setPeriodoAcademico("2025-2");
            when(solicitudes.listarSolicitudesDeVerano(null)).thenReturn(List.of(solicitud));
            Salon salon = new Salon(81, "221", "FIET", true);
            when(salones.listarSalonesActivos()).thenReturn(List.of(salon));
            if ("error-activos".equals(modo)) {
                when(salones.listarSalonesActivos()).thenThrow(new IllegalStateException("sin catálogo"));
            }
            if ("error".equals(modo) || "error-activos".equals(modo)) {
                when(salones.obtenerSalonPorNumero(anyString())).thenThrow(new IllegalStateException("sin consulta"));
            } else if ("encontrado".equals(modo)) {
                when(salones.obtenerSalonPorNumero("221")).thenReturn(salon);
            }
        }
    }

    @TestConfiguration
    // Hasta OPC-4, el @ComponentScan explícito de la aplicación también descubría @TestConfiguration, y este perfil
    // evitaba que los datos fijos sustituyeran casos de uso en las demás pruebas. Se conserva.
    @org.springframework.context.annotation.Profile("caracterizacion-verano")
    static class Configuracion {
        @Bean Datos datosVerano() { return new Datos(); }
        @Bean @Primary GestionarSalonesCUIntPort salonesFijos(Datos d) {
            return new GestionarSalonesCUAdapter(d.salones);
        }
        @Bean @Primary GestionarCursoOfertadoVeranoCUIntPort cursosFijos(Datos d) {
            return new GestionarCursoOfertadoVeranoCUAdapter(d.cursos, mock(GestionarSolicitudGatewayIntPort.class),
                    mock(GestionarUsuarioGatewayIntPort.class), mock(GestionarMateriasIntPort.class),
                    mock(GestionarDocenteGatewayIntPort.class), d.salones, mock(GestionarNotificacionCUIntPort.class));
        }
        @Bean @Primary GestionarSolicitudCursoVeranoCUIntPort solicitudesFijas(Datos d) {
            return new GestionarSolicitudCursoVeranoCUAdapter(d.solicitudes, d.cursos,
                    mock(GestionarUsuarioGatewayIntPort.class), mock(GestionarDocumentosGatewayIntPort.class),
                    mock(GestionarArchivosCUIntPort.class), mock(GestionarNotificacionCUIntPort.class));
        }
    }

    @Test void salones() throws Exception {
        ObjectNode respuestas = JSON.createObjectNode();
        String[][] casos = {{"normal", "221", "encontrado"}, {"espacios", " 221 ", "encontrado"},
                {"respaldo", "221", "error"}, {"error-total", "221", "error-activos"},
                {"inexistente", "999", "vacio"}, {"blanco", "   ", "encontrado"},
                {"sin-salon", null, "encontrado"}};
        for (String[] caso : casos) {
            datos.preparar(caso[1], caso[2]);
            consultar(respuestas, caso[0] + "/todos", get(CURSOS));
            consultar(respuestas, caso[0] + "/detalle", get(CURSOS + "/71"));
            consultar(respuestas, caso[0] + "/editar", put(CURSOS + "/71")
                    .contentType(MediaType.APPLICATION_JSON).content("{\"cupo_estimado\":20}"));
        }
        // BUG-14: detalle y edición recortan el número y buscan entre activos, como el listado
        comparar("salones", respuestas, "verano-endpoints/correcciones/salones-bug14.json");
    }

    @Test void periodosDelListado() throws Exception {
        ObjectNode respuestas = JSON.createObjectNode();
        datos.preparar("221", "encontrado");
        String[] periodos = {null, "", "   ", "todos", "2025-1", " 2025-2 ",
                "2025 - Primer Semestre", "Segundo Período 2025", "primer semestre 1999", "2025-3", "sin año"};
        for (int i = 0; i < periodos.length; i++) {
            var peticion = get(CURSOS);
            if (periodos[i] != null) peticion.param("periodoAcademico", periodos[i]);
            consultar(respuestas, "caso-" + i, peticion);
        }
        // BUG-12: el año ya no cuenta como indicador del período
        comparar("periodos-listado", respuestas, "verano-endpoints/correcciones/periodos-listado-bug12.json");
    }

    @Test void periodosDelReporte() throws Exception {
        ObjectNode respuestas = JSON.createObjectNode();
        datos.preparar("221", "encontrado");
        String[] periodos = {null, "", "   ", "todos", "2025-1", " 2025-2 ",
                "2025 - Primer Semestre", "Segundo Período 2025", "primer semestre 1999", "2025-3", "sin año"};
        for (int i = 0; i < periodos.length; i++) {
            var peticion = get(REPORTES + "/solicitudes");
            if (periodos[i] != null) peticion.param("periodoAcademico", periodos[i]);
            consultar(respuestas, "caso-" + i, peticion);
        }
        // BUG-12: el año ya no cuenta como indicador del período
        comparar("periodos-reporte", respuestas, "verano-endpoints/correcciones/periodos-reporte-bug12.json");
    }

    private void consultar(ObjectNode r, String clave, MockHttpServletRequestBuilder peticion) throws Exception {
        var respuesta = mvc.perform(peticion).andReturn().getResponse();
        assertThat(respuesta.getStatus()).isEqualTo(200);
        ObjectNode valor = r.putObject(clave);
        valor.put("http", respuesta.getStatus());
        valor.set("json", JSON.readTree(respuesta.getContentAsByteArray()));
    }

    @Test void exportacionesPdfExcelYEstudiantes() throws Exception {
        // La referencia se grabó en la zona de Colombia y el Excel escribe las fechas en la zona de la JVM
        TimeZone anterior = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
        try {
            exportaciones();
        } finally {
            TimeZone.setDefault(anterior);
        }
    }

    private void exportaciones() throws Exception {
        datos.preparar(" 221 ", "encontrado");
        CursoOfertadoVerano curso = datos.cursos.obtenerCursoPorId(71);
        Materia materia = new Materia();
        materia.setCodigo("MAT-71");
        materia.setNombre("Materia de prueba");
        curso.setObjMateria(materia);
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(81);
        usuario.setNombreCompleto("Ana Pérez");
        usuario.setCodigo("1046");
        usuario.setCorreo("ana@unicauca.edu.co");
        SolicitudCursoVeranoPreinscripcion pre = new SolicitudCursoVeranoPreinscripcion();
        pre.setIdSolicitud(91);
        pre.setObjUsuario(usuario);
        pre.setObjCursoOfertadoVerano(curso);
        pre.setFechaRegistroSolicitud(curso.getFecha_inicio());
        pre.setObservacion("Motivo de prueba");
        when(datos.solicitudes.buscarPreinscripcionesPorCurso(71)).thenReturn(List.of(pre));
        when(datos.solicitudes.listarSolicitudesDeVerano(null)).thenReturn(List.of(pre));
        ObjectNode respuestas = JSON.createObjectNode();
        consultar(respuestas, "estudiantes", get(CURSOS + "/71/estudiantes"));
        for (boolean vacio : new boolean[]{false, true}) {
            if (vacio) {
                when(datos.cursos.listarTodos()).thenReturn(List.of());
                when(datos.cursos.listarPorPeriodo(anyString())).thenReturn(List.of());
                when(datos.solicitudes.buscarPreinscripcionesPorCurso(71)).thenReturn(List.of());
                when(datos.solicitudes.listarSolicitudesDeVerano(null)).thenReturn(List.of());
            }
            for (String periodo : List.of("todos", "2025-2", "2025 - Primer Semestre")) {
                for (String ruta : List.of("/cursos-verano/todos/export/pdf", "/cursos-verano/71/estudiantes/export/pdf", "/solicitudes/export/excel")) {
                    // EST-7, pasos 8a y 8c: la clave conserva la ruta antigua y se pide la nueva
                    String pedida = switch (ruta) {
                        case "/cursos-verano/todos/export/pdf" -> CURSOS + "?formato=pdf";
                        case "/cursos-verano/71/estudiantes/export/pdf" -> CURSOS + "/71/estudiantes?formato=pdf";
                        default -> REPORTES + "/solicitudes?formato=excel";
                    };
                    var respuesta = mvc.perform(get(pedida).param("periodoAcademico", periodo))
                            .andReturn().getResponse();
                    assertThat(respuesta.getStatus()).isEqualTo(200);
                    ObjectNode valor = respuestas.putObject(vacio + periodo + ruta);
                    valor.put("http", respuesta.getStatus());
                    valor.put("tipo", respuesta.getContentType());
                    valor.put("disposicion", respuesta.getHeader("Content-Disposition")
                            .replaceAll("\\d{8}_\\d{6}", "<ahora>").replaceAll("\\d{4}-\\d{2}-\\d{2}", "<hoy>"));
                    if (ruta.endsWith("pdf")) {
                        var lector = new com.itextpdf.text.pdf.PdfReader(respuesta.getContentAsByteArray());
                        var paginas = valor.putArray("paginas");
                        try {
                            for (int pagina = 1; pagina <= lector.getNumberOfPages(); pagina++) {
                                paginas.add(com.itextpdf.text.pdf.parser.PdfTextExtractor.getTextFromPage(lector, pagina)
                                        .replaceAll("Fecha de generación: [^\\n]*", "Fecha de generación: <ahora>")
                                        .replaceAll("Período actual: [^)]+", "Período actual: <actual>"));
                            }
                        } finally { lector.close(); }
                    } else {
                        var hojas = valor.putObject("hojas");
                        try (var libro = new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(respuesta.getContentAsByteArray()))) {
                            var formato = new org.apache.poi.ss.usermodel.DataFormatter();
                            for (var hoja : libro) {
                                var filas = hojas.putArray(hoja.getSheetName());
                                for (var fila : hoja) {
                                    var celdas = filas.addArray();
                                    for (var celda : fila) celdas.add(formato.formatCellValue(celda)
                                            .replaceAll("Fecha de generacion: .*", "Fecha de generacion: <ahora>")
                                            .replaceAll("Período actual: [^)]+", "Período actual: <actual>"));
                                }
                            }
                        }
                    }
                }
            }
        }
        Path archivo = REFERENCIAS.resolve("exportaciones.json");
        if (Boolean.getBoolean("grabarExportacionesVerano") && !Files.exists(archivo)) {
            Files.writeString(archivo, JSON.writerWithDefaultPrettyPrinter().writeValueAsString(respuestas));
        }
        // BUG-12: el año ya no cuenta como indicador del período
        assertThat(respuestas).isEqualTo(Correcciones.aplicar(Correcciones.aplicar(
                JSON.readTree(Files.readString(archivo)),
                "verano-endpoints/correcciones/exportaciones-bug18.json"),
                "verano-endpoints/correcciones/exportaciones-bug12.json"));
    }

    /** Compara con la referencia {@code nombre}, superponiendo en orden las correcciones indicadas. */
    private void comparar(String nombre, JsonNode actual, String... rutasCorreccion) throws Exception {
        Path archivo = REFERENCIAS.resolve(nombre + ".json");
        if (Boolean.getBoolean("grabarReferencia")) {
            Files.createDirectories(REFERENCIAS);
            Files.writeString(archivo, JSON.writerWithDefaultPrettyPrinter().writeValueAsString(actual) + "\n");
        }
        JsonNode esperado = JSON.readTree(Files.readString(archivo));
        for (String rutaCorreccion : rutasCorreccion) {
            esperado = Correcciones.aplicar(esperado, rutaCorreccion);
        }
        assertThat(actual).isEqualTo(esperado);
    }
}
