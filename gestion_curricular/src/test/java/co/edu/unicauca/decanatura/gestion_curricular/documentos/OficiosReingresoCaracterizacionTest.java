package co.edu.unicauca.decanatura.gestion_curricular.documentos;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudReingresoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.SolicitudReingresoRestController;
import co.edu.unicauca.decanatura.gestion_curricular.seguridad.Autorizacion;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.Correcciones;

/**
 * Fija qué documentos de una solicitud de Reingreso salen como oficio en {@code obtenerOficios} y cuál descarga
 * {@code descargarOficio} cuando la carta de solicitud del estudiante está junto a los oficios (BUG-30). El
 * contenido de cada archivo es su ruta, para saber cuál se descargó.
 */
class OficiosReingresoCaracterizacionTest {
    private final ObjectMapper json = new ObjectMapper();

    @Test
    void conservaOficiosListadosYDescargados() throws Exception {
        Map<Integer, List<Documento>> casos = new LinkedHashMap<>();
        casos.put(1, List.of(documento("carta_solicitud_reingreso.pdf", "r/carta"),
            documento("oficio_reingreso.pdf", "r/oficio")));
        casos.put(2, List.of(documento("carta_solicitud_reingreso.pdf", "r/carta")));
        casos.put(3, List.of(documento("certificado_notas.pdf", "r/notas"),
            documento("Resolucion_Reingreso.PDF", "r/resolucion")));
        casos.put(4, List.of(documento("Carta_Solicitud_Reingreso_Firmada.pdf", "r/carta-firmada"),
            documento("aprobacion.pdf", "r/aprobacion")));
        casos.put(5, List.of(documento("reingreso_aprobado.pdf", "r/aprobado")));

        var archivos = mock(GestionarArchivosCUIntPort.class);
        when(archivos.getFileByPath(anyString())).thenAnswer(i -> i.<String>getArgument(0).getBytes(UTF_8));
        var reingreso = mock(GestionarSolicitudReingresoCUIntPort.class);
        when(reingreso.obtenerSolicitudReingresoPorId(anyInt())).thenAnswer(i -> {
            int id = i.getArgument(0);
            SolicitudReingreso solicitud = new SolicitudReingreso();
            solicitud.setId_solicitud(id);
            solicitud.setDocumentos(new ArrayList<>(casos.get(id)));
            return solicitud;
        });
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new SolicitudReingresoRestController(
            null, reingreso, null, archivos, new Autorizacion(null, null, null, null), null)).build();

        ObjectNode resultados = json.createObjectNode();
        for (int id : casos.keySet()) {
            String ruta = "/api/solicitudes-reingreso/obtenerOficios/" + id;
            MockHttpServletResponse respuesta = mvc.perform(get(ruta)).andReturn().getResponse();
            ObjectNode resultado = resultados.putObject(ruta);
            resultado.put("status", respuesta.getStatus());
            resultado.set("body", json.readTree(respuesta.getContentAsString(UTF_8)));
        }
        for (int id : casos.keySet()) {
            String ruta = "/api/solicitudes-reingreso/descargarOficio/" + id;
            MockHttpServletResponse respuesta = mvc.perform(get(ruta)).andReturn().getResponse();
            ObjectNode resultado = resultados.putObject(ruta);
            resultado.put("status", respuesta.getStatus());
            resultado.put("contentDisposition", respuesta.getHeader(HttpHeaders.CONTENT_DISPOSITION));
            resultado.put("body", respuesta.getContentAsString(UTF_8));
        }

        Path referencia = Path.of("src/test/resources/referencias/documentos/oficios-reingreso.json");
        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(referencia)) {
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(resultados));
        }
        // BUG-30: oficios-reingreso.json fija la carta del estudiante como oficio; la corrección, sin ella
        assertThat(resultados).isEqualTo(Correcciones.aplicar(json.readTree(Files.readString(referencia)),
            "documentos/correcciones/oficios-reingreso-bug30.json"));
    }

    private static Documento documento(String nombre, String ruta) {
        Documento documento = new Documento();
        documento.setNombre(nombre);
        documento.setRuta_documento(ruta);
        return documento;
    }
}
