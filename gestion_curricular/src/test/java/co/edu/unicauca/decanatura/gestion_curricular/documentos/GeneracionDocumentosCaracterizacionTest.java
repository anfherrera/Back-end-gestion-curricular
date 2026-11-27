package co.edu.unicauca.decanatura.gestion_curricular.documentos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.*;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.*;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.Correcciones;

/** Referencias anteriores a ARQ-3/4. Solo normaliza las fechas calculadas al generar. */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@org.springframework.security.test.context.support.WithMockUser(roles = "Funcionario")
class GeneracionDocumentosCaracterizacionTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path REFERENCIA = Path.of("src/test/resources/referencias/documentos/generacion.json");
    private static final String[] DIAS_EN_LETRAS = { "", "un", "dos", "tres", "cuatro", "cinco", "seis", "siete",
            "ocho", "nueve", "diez", "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete",
            "dieciocho", "diecinueve", "veinte", "veintiún", "veintidós", "veintitrés", "veinticuatro",
            "veinticinco", "veintiséis", "veintisiete", "veintiocho", "veintinueve", "treinta", "treinta y un" };
    @Autowired MockMvc mvc;
    @MockitoBean GestionarSolicitudHomologacionCUIntPort homologacion;
    @MockitoBean GestionarSolicitudReingresoCUIntPort reingreso;
    @MockitoBean GestionarSolicitudPazYSalvoCUIntPort paz;

    @Test void contenidoCabecerasYRespaldos() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setCedula("123456789");
        SolicitudHomologacion hom = new SolicitudHomologacion();
        hom.setObjUsuario(usuario);
        hom.setProgramaOrigen("Origen de la solicitud");
        hom.setProgramaDestino("Destino de la solicitud");
        SolicitudReingreso rein = new SolicitudReingreso();
        rein.setObjUsuario(usuario);
        rein.setPeriodoAcademico(" 2025-2 ");
        rein.setFechaRegistroSolicitud(Date.from(Instant.parse("2025-02-03T12:00:00Z")));
        when(homologacion.buscarPorId(71)).thenReturn(hom);
        when(reingreso.obtenerSolicitudReingresoPorId(71)).thenReturn(rein);
        when(homologacion.buscarPorId(73)).thenThrow(new IllegalStateException("consulta fallida"));
        when(reingreso.obtenerSolicitudReingresoPorId(73)).thenThrow(new IllegalStateException("consulta fallida"));
        var datos = new DatosDocumento("Ana Pérez", "Ana Pérez", "1046",
                "Ingeniería de Sistemas", "2025-02-03", "123456789", "Título de prueba", "Director de prueba");
        when(paz.prepararDocumento(any(), any(), any(), any())).thenReturn(Optional.of(datos));

        ObjectNode respuestas = JSON.createObjectNode();
        for (String proceso : List.of("homologacion", "reingreso", "paz-salvo", "desconocido")) {
            var r = mvc.perform(get("/api/documentos-generados/plantillas/" + proceso)).andReturn().getResponse();
            ObjectNode salida = cabeceras(r);
            salida.set("json", JSON.readTree(r.getContentAsByteArray()));
            respuestas.set("plantillas-" + proceso, salida);
        }
        for (String tipo : List.of("OFICIO_HOMOLOGACION", "RESOLUCION_REINGRESO", "PAZ_SALVO")) {
            for (int id : new int[]{0, 71, 72, 73}) {
                Map<String, Object> cuerpo = Map.of("idSolicitud", id, "tipoDocumento", tipo,
                        "datosDocumento", Map.of("numeroDocumento", "REF-2025", "fechaDocumento", "2025-06-12", "observaciones", "Observaciones de prueba"),
                        "datosSolicitud", Map.of("nombreEstudiante", "Ana Pérez", "codigoEstudiante", "1046",
                                "programa", "Ingeniería de Sistemas", "fechaSolicitud", "2025-02-03", "cedulaEstudiante", "CEDULA-MAPA",
                                "programaOrigen", "Origen del mapa", "programaDestino", "Destino del mapa", "periodoAcademico", "2024-1"));
                var r = mvc.perform(post("/api/documentos-generados").contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsBytes(cuerpo))).andReturn().getResponse();
                respuestas.set(tipo + "-" + id, documento(r, false));
            }
        }
        // EST-7, paso 7a: el documento por defecto se retiró (404, corrección generacion-est7.json)
        for (String formato : List.of("docx", "pdf")) {
            var r = mvc.perform(get("/api/solicitudes-pazysalvo/generarDocumentoPazYSalvo/71/" + formato))
                    .andReturn().getResponse();
            respuestas.set("paz-" + formato, r.getStatus() == 200 ? documento(r, formato.equals("pdf")) : error(r));
        }
        var r = mvc.perform(post("/api/solicitudes-pazysalvo/71/documento-generado")
                .param("numeroDocumento", "REF-2025").param("fechaDocumento", "2025-06-12T12:00:00"))
                .andReturn().getResponse();
        respuestas.set("paz-personalizado", documento(r, false));
        if (Boolean.getBoolean("grabarDocumentosReferencia") && !Files.exists(REFERENCIA)) {
            Files.createDirectories(REFERENCIA.getParent());
            Files.writeString(REFERENCIA, JSON.writerWithDefaultPrettyPrinter().writeValueAsString(respuestas));
        }
        assertThat(normalizarFechasPartidas(respuestas, LocalDate.now()))
                .isEqualTo(normalizarFechasPartidas(Correcciones.aplicar(Correcciones.aplicar(
                        JSON.readTree(Files.readString(REFERENCIA)), "documentos/correcciones/generacion-est7.json"),
                        // El periodo de la resolución de reingreso y el día de firma en letras
                        "documentos/correcciones/generacion-oficios.json"),
                        LocalDate.of(2026, 11, 13))); // Fecha en que se obtuvo la referencia previa al refactor.
    }

    private static ObjectNode normalizarFechasPartidas(ObjectNode datos, LocalDate fechaGeneracion) {
        ObjectNode copia = datos.deepCopy();
        String diaMes = fechaGeneracion.getDayOfMonth() + " de "
                + fechaGeneracion.format(DateTimeFormatter.ofPattern("MMMM", new Locale("es", "ES")));
        // El día de firma en letras, solo delante de la fecha de generación completa
        String firma = "a los " + DIAS_EN_LETRAS[fechaGeneracion.getDayOfMonth()] + " ("
                + fechaGeneracion.getDayOfMonth() + ") días del mes de "
                + fechaGeneracion.format(DateTimeFormatter.ofPattern("MMMM", new Locale("es", "ES")));
        String firmaNormalizada = "a los <día-letras> (" + fechaGeneracion.getDayOfMonth() + ") días del mes de "
                + fechaGeneracion.format(DateTimeFormatter.ofPattern("MMMM", new Locale("es", "ES")));
        copia.elements().forEachRemaining(valor -> {
            if (valor.has("texto")) {
                String texto = valor.get("texto").asText()
                        .replace(firma + " de " + fechaGeneracion.getYear(),
                                firmaNormalizada + " de " + fechaGeneracion.getYear())
                        .replace(firma + " del año " + fechaGeneracion.getYear(),
                                firmaNormalizada + " del año " + fechaGeneracion.getYear())
                        .replace("día " + diaMes + " del año en curso", "día <fecha-generacion> del año en curso")
                        .replace("del " + fechaGeneracion.getYear() + ", se notificó", "del <año-generacion>, se notificó")
                        .replace("(" + fechaGeneracion.getDayOfMonth() + ") días del mes de "
                                        + fechaGeneracion.format(DateTimeFormatter.ofPattern("MMMM", new Locale("es", "ES")))
                                        + " de " + fechaGeneracion.getYear(), "(<día>) días del mes de <mes> de <año>")
                        .replace("(" + fechaGeneracion.getDayOfMonth() + ") días del mes de "
                                        + fechaGeneracion.format(DateTimeFormatter.ofPattern("MMMM", new Locale("es", "ES")))
                                        + " del año " + fechaGeneracion.getYear(), "(<día>) días del mes de <mes> del año <año>");
                ((ObjectNode) valor).put("texto", texto);
            }
        });
        return copia;
    }

    private static ObjectNode cabeceras(MockHttpServletResponse r) {
        assertThat(r.getStatus()).isEqualTo(200);
        ObjectNode salida = JSON.createObjectNode();
        salida.put("http", r.getStatus());
        salida.put("tipo", r.getContentType());
        salida.put("disposicion", r.getHeader("Content-Disposition"));
        return salida;
    }

    /** Una ruta que ya no existe: el código HTTP y el código de error. */
    private static ObjectNode error(MockHttpServletResponse r) throws Exception {
        ObjectNode salida = JSON.createObjectNode();
        salida.put("http", r.getStatus());
        salida.put("tipo", r.getContentType());
        salida.set("codigoError", JSON.readTree(r.getContentAsByteArray()).get("codigoError"));
        return salida;
    }

    private static ObjectNode documento(MockHttpServletResponse r, boolean pdf) throws Exception {
        String texto;
        if (pdf) {
            PdfReader lector = new PdfReader(r.getContentAsByteArray());
            try {
                StringBuilder contenido = new StringBuilder();
                for (int pagina = 1; pagina <= lector.getNumberOfPages(); pagina++) {
                    contenido.append(PdfTextExtractor.getTextFromPage(lector, pagina)).append('\n');
                }
                texto = contenido.toString();
            } finally { lector.close(); }
        } else {
            try (var doc = new XWPFDocument(new ByteArrayInputStream(r.getContentAsByteArray()));
                 var extractor = new XWPFWordExtractor(doc)) {
                texto = extractor.getText();
            }
        }
        for (int dias : new int[]{0, -3, -7}) {
            var fecha = LocalDate.now().plusDays(dias);
            texto = texto.replace(fecha.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"))), "<fecha-generacion" + dias + ">");
        }
        ObjectNode salida = cabeceras(r);
        salida.put("texto", texto);
        return salida;
    }
}
