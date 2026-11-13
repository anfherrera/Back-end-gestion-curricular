package co.edu.unicauca.decanatura.gestion_curricular.documentos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpServletResponse;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.*;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.*;

/** Referencias anteriores a ARQ-3/4. Solo normaliza las fechas calculadas al generar. */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@org.springframework.security.test.context.support.WithMockUser(roles = "Funcionario")
class GeneracionDocumentosCaracterizacionTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path REFERENCIA = Path.of("src/test/resources/referencias/documentos/generacion.json");
    @Autowired MockMvc mvc;
    @MockBean GestionarSolicitudHomologacionCUIntPort homologacion;
    @MockBean GestionarSolicitudReingresoCUIntPort reingreso;
    @MockBean GestionarSolicitudPazYSalvoCUIntPort paz;

    @Test void contenidoCabecerasYRespaldos() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setCedula("123456789");
        SolicitudHomologacion hom = new SolicitudHomologacion();
        hom.setObjUsuario(usuario);
        hom.setPrograma_origen("Origen de la solicitud");
        hom.setPrograma_destino("Destino de la solicitud");
        SolicitudReingreso rein = new SolicitudReingreso();
        rein.setObjUsuario(usuario);
        rein.setPeriodo_academico(" 2025-2 ");
        rein.setFecha_registro_solicitud(Date.from(Instant.parse("2025-02-03T12:00:00Z")));
        when(homologacion.buscarPorId(71)).thenReturn(hom);
        when(reingreso.obtenerSolicitudReingresoPorId(71)).thenReturn(rein);
        when(homologacion.buscarPorId(73)).thenThrow(new IllegalStateException("consulta fallida"));
        when(reingreso.obtenerSolicitudReingresoPorId(73)).thenThrow(new IllegalStateException("consulta fallida"));
        var datos = new DatosDocumento("Ana Pérez", "Ana Pérez", "1046",
                "Ingeniería de Sistemas", "2025-02-03", "123456789", "Título de prueba", "Director de prueba");
        when(paz.prepararDocumento(any(), any(), any(), any(), anyBoolean())).thenReturn(Optional.of(datos));

        ObjectNode respuestas = JSON.createObjectNode();
        for (String proceso : List.of("homologacion", "reingreso", "paz-salvo", "desconocido")) {
            var r = mvc.perform(get("/api/documentos/templates/" + proceso)).andReturn().getResponse();
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
                var r = mvc.perform(post("/api/documentos/generar").contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsBytes(cuerpo))).andReturn().getResponse();
                respuestas.set(tipo + "-" + id, documento(r, false));
            }
        }
        for (String formato : List.of("docx", "pdf")) {
            var r = mvc.perform(get("/api/solicitudes-pazysalvo/generarDocumentoPazYSalvo/71/" + formato))
                    .andReturn().getResponse();
            respuestas.set("paz-" + formato, documento(r, formato.equals("pdf")));
        }
        var r = mvc.perform(post("/api/solicitudes-pazysalvo/generar-documento/71")
                .param("numeroDocumento", "REF-2025").param("fechaDocumento", "2025-06-12T12:00:00"))
                .andReturn().getResponse();
        respuestas.set("paz-personalizado", documento(r, false));
        if (Boolean.getBoolean("grabarDocumentosReferencia") && !Files.exists(REFERENCIA)) {
            Files.createDirectories(REFERENCIA.getParent());
            Files.writeString(REFERENCIA, JSON.writerWithDefaultPrettyPrinter().writeValueAsString(respuestas));
        }
        assertThat(respuestas).isEqualTo(JSON.readTree(Files.readString(REFERENCIA)));
    }

    private static ObjectNode cabeceras(MockHttpServletResponse r) {
        assertThat(r.getStatus()).isEqualTo(200);
        ObjectNode salida = JSON.createObjectNode();
        salida.put("http", r.getStatus());
        salida.put("tipo", r.getContentType());
        salida.put("disposicion", r.getHeader("Content-Disposition"));
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
