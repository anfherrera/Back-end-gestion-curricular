package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.SolicitudRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.DocumentosMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.EstadoSolicitudMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.ProgramaMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.RolMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.SolicitudMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.UsuarioMapperDominioImpl;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.Correcciones;

/**
 * Fija {@code GET /api/solicitudes/historial} y su exportación a PDF antes de tipar la respuesta (ARQ-10):
 * estado, cuerpo, orden y tipo de las claves del sobre, y texto del PDF con los totales. La fecha de
 * generación, el período actual y la fecha del nombre del archivo se sustituyen por marcadores.
 */
class HistorialSolicitudesCaracterizacionTest {
    /** 2024-03-15T17:00Z: el mismo día en cualquier zona entre UTC-12 y UTC+6. */
    private static final long MARZO = 1710522000000L;

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void conservaElHistorialYSuExportacion() throws Exception {
        MockMvc mvc = mvc();

        ObjectNode resultados = json.createObjectNode();
        String[] tipos = {null, "  ", "Paz y Salvo", "reingreso", "Homologación", "homologacion",
            "ECAES", "Curso de verano", "intersemestral", "Académico raro", "ninguno"};
        ObjectNode historial = resultados.putObject("historial");
        for (String tipo : tipos) {
            MockHttpServletRequestBuilder peticion = get("/api/solicitudes/historial");
            if (tipo != null) {
                peticion.param("tipoSolicitud", tipo);
            }
            var respuesta = mvc.perform(peticion).andReturn().getResponse();
            JsonNode body = json.readTree(respuesta.getContentAsByteArray());
            ObjectNode resultado = historial.putObject(String.valueOf(tipo));
            resultado.put("status", respuesta.getStatus());
            resultado.set("body", body);
            ObjectNode claves = resultado.putObject("claves");
            body.fields().forEachRemaining(c -> claves.put(c.getKey(), c.getValue().getNodeType().name()));
        }

        ObjectNode pdf = resultados.putObject("pdf");
        String[][] filtros = {
            {null, null, null},
            {"2024-1", "Paz y Salvo", "APROBADA"},
            {null, "ninguno", null},
        };
        for (String[] filtro : filtros) {
            MockHttpServletRequestBuilder peticion = get("/api/solicitudes/historial/export/pdf");
            if (filtro[0] != null) peticion.param("periodoAcademico", filtro[0]);
            if (filtro[1] != null) peticion.param("tipoSolicitud", filtro[1]);
            if (filtro[2] != null) peticion.param("estadoActual", filtro[2]);
            MockHttpServletResponse respuesta = mvc.perform(peticion).andReturn().getResponse();
            ObjectNode resultado = pdf.putObject(String.join("|", String.valueOf(filtro[0]),
                String.valueOf(filtro[1]), String.valueOf(filtro[2])));
            resultado.put("status", respuesta.getStatus());
            resultado.put("contentType", respuesta.getContentType());
            String hoy = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            resultado.put("disposicion", String.valueOf(respuesta.getHeader(HttpHeaders.CONTENT_DISPOSITION))
                .replace(hoy, "<HOY>"));
            resultado.put("texto", texto(respuesta.getContentAsByteArray()));
        }

        Path referencia = Path.of("src/test/resources/referencias/listados/historial.json");
        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(referencia)) {
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(resultados));
        }
        JsonNode grabada = Correcciones.aplicar(json.readTree(Files.readString(referencia)),
            "listados/correcciones/historial-bug29.json");
        assertThat(resultados).isEqualTo(grabada);
        // ObjectNode no compara el orden de las claves: se comprueba aparte
        historial.fields().forEachRemaining(caso -> assertThat(nombres(caso.getValue().get("claves")))
            .as(caso.getKey()).isEqualTo(nombres(grabada.get("historial").get(caso.getKey()).get("claves"))));
    }

    /**
     * Fija el filtro {@code tipoSolicitud} con la categoría y el tipo que el historial asigna a cada solicitud
     * (BUG-29): qué solicitudes devuelve el historial y el texto del PDF exportado, que filtra con el mismo código.
     */
    @Test
    void conservaElFiltroPorTipo() throws Exception {
        MockMvc mvc = mvc();

        ObjectNode resultados = json.createObjectNode();
        ObjectNode historial = resultados.putObject("historial");
        for (String tipo : new String[] {"Cursos de Verano", "Curso Nuevo", "Preinscripcion", "Inscripción", "Otro",
            "Homologacion"}) {
            var respuesta = mvc.perform(get("/api/solicitudes/historial").param("tipoSolicitud", tipo))
                .andReturn().getResponse();
            JsonNode body = json.readTree(respuesta.getContentAsByteArray());
            ObjectNode resultado = historial.putObject(tipo);
            resultado.put("status", respuesta.getStatus());
            resultado.set("total", body.get("total"));
            var ids = resultado.putArray("ids");
            body.get("solicitudes").forEach(s -> ids.add(s.get("id_solicitud")));
        }

        ObjectNode pdf = resultados.putObject("pdf");
        for (String tipo : new String[] {"Homologación", "Curso de verano", "Otro"}) {
            MockHttpServletResponse respuesta = mvc.perform(get("/api/solicitudes/historial/export/pdf")
                .param("tipoSolicitud", tipo)).andReturn().getResponse();
            ObjectNode resultado = pdf.putObject(tipo);
            resultado.put("status", respuesta.getStatus());
            resultado.put("texto", texto(respuesta.getContentAsByteArray()));
        }

        Path referencia = Path.of("src/test/resources/referencias/listados/historial-tipos.json");
        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(referencia)) {
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(resultados));
        }
        assertThat(resultados).isEqualTo(Correcciones.aplicar(json.readTree(Files.readString(referencia)),
            "listados/correcciones/historial-tipos-bug29.json"));
    }

    private MockMvc mvc() {
        var cu = mock(GestionarSolicitudCUIntPort.class);
        when(cu.buscarHistorial(any(), any(), any())).thenAnswer(i -> solicitudes());
        when(cu.contarHistorial()).thenReturn(25L);
        when(cu.contarSolicitudesProcesadas()).thenReturn(10L);
        return MockMvcBuilders.standaloneSetup(new SolicitudRestController(cu, mapper()))
            .setControllerAdvice(new RestApiExceptionHandler()).build();
    }

    private String texto(byte[] contenido) throws Exception {
        PdfReader lector = new PdfReader(contenido);
        StringBuilder texto = new StringBuilder();
        for (int pagina = 1; pagina <= lector.getNumberOfPages(); pagina++) {
            texto.append(PdfTextExtractor.getTextFromPage(lector, pagina)).append('\n');
        }
        lector.close();
        PeriodoAcademicoEnum actual = PeriodoAcademicoEnum.getPeriodoActual();
        String resultado = texto.toString().replaceAll("Fecha de generación: [^\\n]*", "Fecha de generación: <HOY>");
        if (actual != null) {
            resultado = resultado.replace("(Período actual: " + actual.getValor() + ")", "(Período actual: <ACTUAL>)");
        }
        return resultado;
    }

    private static SolicitudMapperDominioImpl mapper() {
        var usuarios = new UsuarioMapperDominioImpl();
        ReflectionTestUtils.setField(usuarios, "programaMapperDominio", new ProgramaMapperDominioImpl());
        ReflectionTestUtils.setField(usuarios, "rolMapperDominio", new RolMapperDominioImpl());
        var mapper = new SolicitudMapperDominioImpl();
        ReflectionTestUtils.setField(mapper, "estadoSolicitudMapperDominio", new EstadoSolicitudMapperDominioImpl());
        ReflectionTestUtils.setField(mapper, "usuarioMapperDominio", usuarios);
        ReflectionTestUtils.setField(mapper, "documentosMapperDominio", new DocumentosMapperDominioImpl());
        return mapper;
    }

    private static List<Solicitud> solicitudes() {
        List<Solicitud> lista = new ArrayList<>();
        Solicitud paz = solicitud(SolicitudPazYSalvo::new, 1, "Solicitud de Paz y Salvo");
        paz.setPeriodo_academico("2024-1");
        Usuario usuario = new Usuario();
        usuario.setId_usuario(7);
        usuario.setNombre_completo("María Fernanda Ortiz Castellanos");
        Programa programa = new Programa();
        programa.setId_programa(2);
        programa.setNombre_programa("Ingeniería Electrónica y Telecomunicaciones");
        usuario.setObjPrograma(programa);
        paz.setObjUsuario(usuario);
        paz.getEstadosSolicitud().add(estado("ENVIADA", MARZO));
        paz.getEstadosSolicitud().add(estado("APROBADA", MARZO + 86_400_000L));
        paz.getEstadosSolicitud().add(estado("SIN_FECHA", null));
        lista.add(paz);
        lista.add(solicitud(SolicitudReingreso::new, 2, "Solicitud de Reingreso"));
        lista.add(solicitud(SolicitudHomologacion::new, 3, null));
        lista.add(solicitud(SolicitudEcaes::new, 4, "Inscripción ECAES"));
        lista.add(solicitud(SolicitudCursoVeranoPreinscripcion::new, 5, "Preinscripción curso de verano"));
        lista.add(solicitud(SolicitudCursoVeranoInscripcion::new, 6, "Inscripción curso verano"));
        lista.add(solicitud(SolicitudCursoVerano::new, 7, "Curso nuevo"));
        lista.add(solicitud(Solicitud::new, 8, "Curso intersemestral de verano"));
        lista.add(solicitud(Solicitud::new, 9, "Otra paz y salvo"));
        lista.add(solicitud(Solicitud::new, 10, "Reingreso extemporáneo"));
        lista.add(solicitud(Solicitud::new, 11, "Homologación externa"));
        lista.add(solicitud(Solicitud::new, 12, "Prueba ecaes"));
        Solicitud usuarioSinPrograma = solicitud(Solicitud::new, 13,
            "Solicitud con un nombre bastante largo que supera los cuarenta caracteres raro");
        Usuario sinPrograma = new Usuario();
        sinPrograma.setNombre_completo("Ana");
        usuarioSinPrograma.setObjUsuario(sinPrograma);
        lista.add(usuarioSinPrograma);
        return lista;
    }

    private static <T extends Solicitud> T solicitud(Supplier<T> nueva, int id, String nombre) {
        T solicitud = nueva.get();
        solicitud.setId_solicitud(id);
        solicitud.setNombre_solicitud(nombre);
        solicitud.setFecha_registro_solicitud(id % 2 == 0 ? null : new Date(MARZO));
        return solicitud;
    }

    private static EstadoSolicitud estado(String nombre, Long fecha) {
        EstadoSolicitud estado = new EstadoSolicitud();
        estado.setEstado_actual(nombre);
        estado.setFecha_registro_estado(fecha == null ? null : new Date(fecha));
        return estado;
    }

    private static List<String> nombres(JsonNode objeto) {
        List<String> nombres = new ArrayList<>();
        objeto.fieldNames().forEachRemaining(nombres::add);
        return nombres;
    }
}
