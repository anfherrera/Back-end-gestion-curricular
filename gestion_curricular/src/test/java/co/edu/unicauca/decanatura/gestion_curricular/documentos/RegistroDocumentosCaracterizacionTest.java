package co.edu.unicauca.decanatura.gestion_curricular.documentos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.mock.web.MockMultipartFile;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.*;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.SoporteMvc;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.*;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso.GestionarDocumentosCUAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.*;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.*;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.*;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.Autorizacion;

class RegistroDocumentosCaracterizacionTest {
    @Test
    void conservaNombreAutorAsociacionYOrdenDeGuardado() throws Exception {
        var archivos = mock(GestionarArchivosCUIntPort.class);
        var gateway = mock(GestionarDocumentosGatewayIntPort.class);
        var documentos = new GestionarDocumentosCUAdapter(gateway, archivos);
        var pazCU = mock(GestionarSolicitudPazYSalvoCUIntPort.class);
        var homoCU = mock(GestionarSolicitudHomologacionCUIntPort.class);
        var auth = new Autorizacion(null, null, null, null) {
            @Override public Integer idUsuarioActual() { return 31; }
        };
        var json = new ObjectMapper();
        var paz = new SolicitudPazYSalvoRestController(pazCU, mock(SolicitudPazYSalvoMapperDominio.class), archivos,
            documentos, Mappers.getMapper(DocumentosMapperDominio.class), mock(GenerarDocumentosCUIntPort.class), json, auth);
        var homo = new SolicitudHomologacionRestController(homoCU, mock(SolicitudHomologacionMapperDominio.class),
            mock(SolicitudMapperDominio.class), archivos, documentos, auth, null);
        var mvc = SoporteMvc.standaloneSetup(paz, homo).setControllerAdvice(new RestApiExceptionHandler()).build();
        String[] rutas = {"/api/solicitudes-pazysalvo/subir-documento",
            "/api/solicitudes-pazysalvo/subir-oficio-pdf/7",
            "/api/solicitudes-pazysalvo/guardarOficio",
            "/api/solicitudes-homologacion/7/subir-resolucion"};
        Map<String, Object> resultados = new LinkedHashMap<>();
        for (String ruta : rutas) {
            for (boolean existe : new boolean[]{true, false}) {
                reset(archivos, gateway, pazCU, homoCU);
                var eventos = new java.util.ArrayList<String>();
                var guardados = new java.util.ArrayList<Documento>();
                when(archivos.saveFile(any(), anyString(), anyString())).thenAnswer(i -> {eventos.add("archivo"); return "servidor/guardado.pdf";});
                when(archivos.saveFile(any(), anyString(), anyString(), anyString(), anyInt())).thenAnswer(i -> {eventos.add("archivo"); return "servidor/guardado.pdf";});
                when(pazCU.buscarPorId(7)).thenAnswer(i -> {eventos.add("solicitud"); return existe ? new SolicitudPazYSalvo() : null;});
                when(homoCU.buscarPorId(7)).thenAnswer(i -> {eventos.add("solicitud"); var s = new SolicitudHomologacion(); s.setId_solicitud(7); return existe ? s : null;});
                when(gateway.crearDocumento(any())).thenAnswer(i -> {eventos.add("registro"); Documento d=i.getArgument(0); d.setId_documento(11); guardados.add(d); return d;});
                boolean word = ruta.endsWith("guardarOficio");
                var request = multipart(ruta).file(new MockMultipartFile("file", word ? "original.docx" : "original.pdf", "application/octet-stream", "%PDF-archivo".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                if (word) request.param("idSolicitud", "7").param("tipoDocumento", "oficio").param("numeroDocumento", "1").param("fechaDocumento", "2025-01-01");
                var respuesta = mvc.perform(request).andReturn().getResponse();
                JsonNode body = respuesta.getContentAsByteArray().length == 0 ? json.nullNode() : json.readTree(respuesta.getContentAsByteArray());
                var snapshot = json.valueToTree(Map.of("status", respuesta.getStatus(), "body", body,
                    "documentos", guardados, "eventos", eventos));
                normalizar(snapshot);
                resultados.put(ruta + " existe=" + existe, snapshot);
            }
        }
        var actual = json.valueToTree(resultados);
        Path referencia = Path.of("src/test/resources/referencias/documentos/registro.json");
        if (Boolean.getBoolean("grabarReferencia")) {
            assertThat(Files.exists(referencia)).isFalse();
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(actual));
        }
        var esperado = (ObjectNode) json.readTree(Files.readString(referencia));
        // BUG-21: el oficio Word comprueba la solicitud antes de guardar el archivo. Esas entradas salen de
        // registro-bug21.json y solo pueden diferir de la referencia anterior en el orden de los eventos.
        var corregidos = json.readTree(Files.readString(
            Path.of("src/test/resources/referencias/documentos/registro-bug21.json")));
        assertThat(corregidos.size()).isEqualTo(2);
        corregidos.fields().forEachRemaining(e -> {
            assertThat(esperado.has(e.getKey())).as(e.getKey()).isTrue();
            var anterior = (ObjectNode) esperado.get(e.getKey()).deepCopy();
            anterior.set("eventos", e.getValue().get("eventos"));
            assertThat(e.getValue()).as(e.getKey()).isEqualTo(anterior);
            esperado.set(e.getKey(), e.getValue());
        });
        assertThat(actual).isEqualTo(esperado);
    }

    private static void normalizar(JsonNode nodo) {
        if (nodo.isObject()) {
            for (String clave : new String[]{"fecha", "fecha_documento"}) {
                if (nodo.hasNonNull(clave)) ((ObjectNode)nodo).put(clave, "<ahora>");
            }
        }
        nodo.elements().forEachRemaining(RegistroDocumentosCaracterizacionTest::normalizar);
    }
}
