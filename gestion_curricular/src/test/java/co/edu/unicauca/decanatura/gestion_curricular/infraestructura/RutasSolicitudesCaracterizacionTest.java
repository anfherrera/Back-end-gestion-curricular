package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.SolicitudRestController;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.SoporteMvc;

/**
 * Caracterización previa a la migración de rutas de EST-7 (paso 5): solicitudes. Cada operación se pide por su ruta
 * antigua y por la nueva, con el caso de uso simulado y el mapper real. El PDF del historial se guarda como texto,
 * con la fecha de generación y el período actual sustituidos por marcadores. La seguridad la fija
 * {@code AutorizacionPorRolTest}.
 */
class RutasSolicitudesCaracterizacionTest {

    private final ObjectMapper json = new ObjectMapper();
    private final ArrayNode llamadas = json.createArrayNode();

    @Test
    void solicitudes() throws Exception {
        List<Solicitud> todas = HistorialSolicitudesCaracterizacionTest.solicitudes();
        var cu = mock(GestionarSolicitudCUIntPort.class);
        when(cu.listarSolicitudes()).thenReturn(todas);
        when(cu.obtenerSolicitudPorId(1)).thenReturn(todas.get(0));
        when(cu.obtenerSolicitudesPorNombre("Paz")).thenReturn(List.of(todas.get(0)));
        when(cu.obtenerSolicitudesPorNombre("nada")).thenReturn(List.of());
        doAnswer(i -> {
            llamadas.addObject().put("eliminarSolicitud", i.<Integer>getArgument(0));
            return i.<Integer>getArgument(0) == 1;
        }).when(cu).eliminarSolicitud(any());
        when(cu.buscarHistorial(any(), any(), any())).thenReturn(todas);
        when(cu.contarHistorial()).thenReturn(25L);
        when(cu.contarSolicitudesProcesadas()).thenReturn(10L);
        MockMvc mvc = SoporteMvc.standaloneSetup(
                new SolicitudRestController(cu, HistorialSolicitudesCaracterizacionTest.mapper()))
                .setControllerAdvice(new RestApiExceptionHandler()).build();

        String b = "/api/solicitudes";
        ObjectNode casos = json.createObjectNode();
        caso(casos, mvc, HttpMethod.GET, b + "/listar");
        caso(casos, mvc, HttpMethod.GET, b);
        for (String nombre : List.of("Paz", "nada")) {
            caso(casos, mvc, HttpMethod.GET, b + "/buscarPorNombre?nombre=" + nombre);
            caso(casos, mvc, HttpMethod.GET, b + "?nombre=" + nombre);
        }
        caso(casos, mvc, HttpMethod.GET, b + "/buscarPorNombre");
        for (String id : List.of("1", "99", "0", "abc")) {
            caso(casos, mvc, HttpMethod.GET, b + "/buscarPorId/" + id);
            caso(casos, mvc, HttpMethod.GET, b + "/" + id);
            caso(casos, mvc, HttpMethod.DELETE, b + "/eliminar/" + id);
            caso(casos, mvc, HttpMethod.DELETE, b + "/" + id);
        }
        // Historial: el JSON no cambia de ruta; el PDF pasa a ?formato=pdf
        caso(casos, mvc, HttpMethod.GET, b + "/historial?periodoAcademico=2024-1");
        for (String filtros : List.of("", "periodoAcademico=2024-1&tipoSolicitud=Paz y Salvo&estadoActual=APROBADA")) {
            caso(casos, mvc, HttpMethod.GET, b + "/historial/export/pdf" + (filtros.isEmpty() ? "" : "?" + filtros));
            caso(casos, mvc, HttpMethod.GET, b + "/historial?formato=pdf" + (filtros.isEmpty() ? "" : "&" + filtros));
        }
        caso(casos, mvc, HttpMethod.GET, b + "/historial?formato=xml");
        caso(casos, mvc, HttpMethod.GET, b + "/historial?formato=PDF");

        // EST-7: las rutas nuevas responden como las antiguas, que pasan a 404
        GrafoObjetos.compararConReferencia(casos, "rutas/solicitudes.json", "rutas/correcciones/solicitudes-est7.json");
    }

    private void caso(ObjectNode casos, MockMvc mvc, HttpMethod metodo, String ruta) throws Exception {
        llamadas.removeAll();
        var peticion = request(metodo, ruta.replace(" ", "%20"));
        var respuesta = mvc.perform(peticion).andReturn().getResponse();
        ObjectNode caso = json.createObjectNode();
        caso.put("status", respuesta.getStatus());
        caso.put("tipo", respuesta.getContentType());
        String disposicion = respuesta.getHeader(HttpHeaders.CONTENT_DISPOSITION);
        if (disposicion != null) {
            caso.put("disposicion", disposicion.replace(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), "<HOY>"));
        }
        byte[] bytes = respuesta.getContentAsByteArray();
        JsonNode cuerpo;
        if (bytes.length == 0) {
            cuerpo = json.nullNode();
        } else if ("application/pdf".equals(respuesta.getContentType())) {
            cuerpo = json.getNodeFactory().textNode(textoPdf(bytes));
        } else {
            cuerpo = json.readTree(new String(bytes, StandardCharsets.UTF_8));
        }
        caso.set("cuerpo", cuerpo);
        caso.set("llamadas", llamadas.deepCopy());
        casos.set(metodo + " " + ruta, caso);
    }

    private static String textoPdf(byte[] contenido) throws Exception {
        PdfReader lector = new PdfReader(contenido);
        StringBuilder texto = new StringBuilder();
        for (int pagina = 1; pagina <= lector.getNumberOfPages(); pagina++) {
            texto.append(PdfTextExtractor.getTextFromPage(lector, pagina)).append('\n');
        }
        lector.close();
        String resultado = texto.toString().replaceAll("Fecha de generación: [^\\n]*", "Fecha de generación: <HOY>");
        PeriodoAcademicoEnum actual = PeriodoAcademicoEnum.getPeriodoActual();
        if (actual != null) {
            resultado = resultado.replace("(Período actual: " + actual.getValor() + ")", "(Período actual: <ACTUAL>)");
        }
        return resultado;
    }
}
