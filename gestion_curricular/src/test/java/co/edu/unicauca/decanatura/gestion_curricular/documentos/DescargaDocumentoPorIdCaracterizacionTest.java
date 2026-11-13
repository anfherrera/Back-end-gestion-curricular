package co.edu.unicauca.decanatura.gestion_curricular.documentos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.*;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.DocumentoRestController;

class DescargaDocumentoPorIdCaracterizacionTest {
    @Test
    void conservaNombreUtf8TipoYBytes() throws Exception {
        byte[] contenido = "contenido fijo".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        var archivos = mock(GestionarArchivosCUIntPort.class);
        when(archivos.getFileByPath(anyString())).thenReturn(contenido);
        var documentos = mock(GestionarDocumentosCUIntPort.class);
        var mvc = MockMvcBuilders.standaloneSetup(new DocumentoRestController(documentos, null, archivos, null)).build();
        Map<String, Object> resultados = new LinkedHashMap<>();
        for (String nombre : new String[]{"oficio.pdf", "oficio á +\".pdf", "oficio.docx", null}) {
            var documento = new Documento();
            documento.setNombre(nombre);
            documento.setRuta_documento("carpeta/archivo");
            when(documentos.buscarDocumentoId(7)).thenReturn(documento);
            var respuesta = mvc.perform(get("/api/documentos/7/descargar")).andReturn().getResponse();
            assertThat(respuesta.getStatus()).isEqualTo(200);
            assertThat(respuesta.getContentAsByteArray()).isEqualTo(contenido);
            Map<String, List<String>> headers = new TreeMap<>();
            for (String clave : respuesta.getHeaderNames()) headers.put(clave, new ArrayList<>(respuesta.getHeaders(clave)));
            resultados.put(String.valueOf(nombre), Map.of("status", respuesta.getStatus(), "headers", headers,
                "body", Base64.getEncoder().encodeToString(respuesta.getContentAsByteArray())));
        }
        var json = new ObjectMapper();
        var actual = json.valueToTree(resultados);
        Path referencia = Path.of("src/test/resources/referencias/documentos/descarga-por-id.json");
        if (Boolean.getBoolean("grabarReferencia")) {
            assertThat(Files.exists(referencia)).isFalse();
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(actual));
        }
        assertThat(actual).isEqualTo(json.readTree(Files.readString(referencia)));
    }
}
