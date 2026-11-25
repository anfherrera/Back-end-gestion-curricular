package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GenerarReportesEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;

/**
 * Caracterización previa a la migración de rutas de EST-7 (paso 9): {@code EstadisticasRestController}. A diferencia
 * de las de los pasos anteriores, la cadena de filtros está activa, porque {@code SeguridadConfig} tiene reglas de URL
 * para {@code /api/estadisticas/**} y el paso las cambia. Los dos casos de uso son simulados: devuelven los valores por
 * omisión de Mockito (los reportes, el nombre del método en ASCII) y anotan cada llamada con sus argumentos, con las
 * fechas en texto en la zona de la JVM. Así se fija qué ruta llama a qué método y con qué filtros; el contenido de las
 * respuestas ya lo fija {@code EstadisticasEndpointsCaracterizacionTest}.
 *
 * <p>Pide con el rol que dice la clave (o sin autenticar, {@code anonimo}). Guarda el estado, el tipo, el cuerpo (el
 * texto, si no es JSON), el {@code Content-Disposition} y las llamadas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RutasEstadisticasCaracterizacionTest {

    private static final String BASE = "/api/estadisticas";
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final List<String> LLAMADAS = new ArrayList<>();

    @Autowired MockMvc mvc;
    /**
     * {@code @MockBean} sustituye al bean del puerto. Se eligió frente a una {@code @TestConfiguration} cuando la
     * aplicación escaneaba sin el filtro de tipos de prueba (hasta OPC-4).
     */
    @MockBean GestionarEstadisticasCUIntPort estadisticaCU;
    @MockBean GenerarReportesEstadisticasCUIntPort reportesCU;

    /** Cada método de los dos puertos anota la llamada; los reportes devuelven el nombre del método en ASCII. */
    @BeforeEach
    void anotarLlamadas() throws Exception {
        anotarTodo(estadisticaCU, GestionarEstadisticasCUIntPort.class);
        anotarTodo(reportesCU, GenerarReportesEstadisticasCUIntPort.class);
    }

    private static <T> void anotarTodo(T simulado, Class<T> tipo) throws Exception {
        Answer<Object> respuesta = i -> {
            anotar(i);
            return i.getMethod().getReturnType() == byte[].class
                    ? i.getMethod().getName().getBytes(StandardCharsets.US_ASCII)
                    : Mockito.RETURNS_DEFAULTS.answer(i);
        };
        for (Method metodo : tipo.getMethods()) {
            if (metodo.isDefault() || Modifier.isStatic(metodo.getModifiers())) {
                continue;
            }
            Object stub = Mockito.doAnswer(respuesta).when(simulado);
            Class<?>[] parametros = metodo.getParameterTypes();
            Object[] argumentos = new Object[parametros.length];
            for (int p = 0; p < parametros.length; p++) {
                argumentos[p] = cualquiera(parametros[p]);
            }
            metodo.invoke(stub, argumentos);
        }
    }

    private static Object cualquiera(Class<?> tipo) {
        if (tipo == int.class) {
            return ArgumentMatchers.anyInt();
        }
        if (tipo == long.class) {
            return ArgumentMatchers.anyLong();
        }
        if (tipo == boolean.class) {
            return ArgumentMatchers.anyBoolean();
        }
        if (tipo == double.class) {
            return ArgumentMatchers.anyDouble();
        }
        return ArgumentMatchers.any();
    }

    @Test
    void consultas() throws Exception {
        ObjectNode casos = JSON.createObjectNode();
        String filtros = "?periodoAcademico=2025-2&idPrograma=1";

        // Las que usa el frontend: no cambian
        for (String ruta : List.of("/estudiantes-por-programa", "/estadisticas-por-proceso", "/estado-solicitudes",
                "/por-periodo", "/por-programa", "/tendencias-comparativas", "/cursos-verano",
                "/cursos-verano/tendencias-temporales")) {
            caso(casos, "funcionario", HttpMethod.GET, BASE + ruta, null);
            caso(casos, "funcionario", HttpMethod.GET, BASE + ruta + filtros, null);
            caso(casos, "estudiante", HttpMethod.GET, BASE + ruta, null);
        }
        caso(casos, "funcionario", HttpMethod.GET, BASE + "/total-estudiantes", null);
        caso(casos, "estudiante", HttpMethod.GET, BASE + "/total-estudiantes", null);
        // /resumen-completo, con cada rol
        for (String quien : List.of("admin", "funcionario", "coordinador", "secretario", "decano", "estudiante",
                "anonimo")) {
            caso(casos, quien, HttpMethod.GET, BASE + "/resumen-completo", null);
        }
        caso(casos, "funcionario", HttpMethod.GET, BASE + "/resumen-completo" + filtros + "&proceso=Reingreso", null);

        // Sin uso, pero ya siguen la convención: se conservan
        caso(casos, "funcionario", HttpMethod.GET, BASE + "/globales", null);
        caso(casos, "funcionario", HttpMethod.GET, BASE + "/globales?proceso=Reingreso&idPrograma=1"
                + "&periodoAcademico=2025-2&fechaInicio=2025-01-01&fechaFin=2025-06-30", null);
        caso(casos, "funcionario", HttpMethod.GET, BASE + "/dashboard" + filtros + "&proceso=ECAES", null);
        caso(casos, "funcionario", HttpMethod.GET, BASE + "/rendimiento", null);
        caso(casos, "funcionario", HttpMethod.GET, BASE + "/historial?anioInicio=2024&anioFin=2025", null);
        caso(casos, "estudiante", HttpMethod.GET, BASE + "/globales", null);

        // EST-7: las consultas y su seguridad no cambian
        GrafoObjetos.compararConReferencia(casos, "rutas/estadisticas-consultas.json");
    }

    @Test
    void reportes() throws Exception {
        ObjectNode casos = JSON.createObjectNode();
        String completos = "proceso=Reingreso&idPrograma=1&periodoAcademico=2025-2&fechaInicio=2025-01-01"
                + "&fechaFin=2025-06-30";
        String verano = "periodoAcademico=2025-2&idPrograma=1";

        for (String formato : List.of("pdf", "excel")) {
            // El reporte completo, con filtros opcionales
            caso(casos, "funcionario", HttpMethod.GET, BASE + "/export/" + formato, null);
            caso(casos, "funcionario", HttpMethod.GET, BASE + "/reportes?formato=" + formato, null);
            caso(casos, "funcionario", HttpMethod.GET, BASE + "/export/" + formato + "?" + completos, null);
            caso(casos, "funcionario", HttpMethod.GET, BASE + "/reportes?formato=" + formato + "&" + completos, null);
            caso(casos, "admin", HttpMethod.GET, BASE + "/export/" + formato, null);
            caso(casos, "admin", HttpMethod.GET, BASE + "/reportes?formato=" + formato, null);
            caso(casos, "estudiante", HttpMethod.GET, BASE + "/export/" + formato, null);
            caso(casos, "estudiante", HttpMethod.GET, BASE + "/reportes?formato=" + formato, null);
            // El de cursos de verano
            caso(casos, "funcionario", HttpMethod.GET, BASE + "/export/" + formato + "/cursos-verano", null);
            caso(casos, "funcionario", HttpMethod.GET, BASE + "/cursos-verano/reportes?formato=" + formato, null);
            caso(casos, "funcionario", HttpMethod.GET, BASE + "/export/" + formato + "/cursos-verano?" + verano, null);
            caso(casos, "funcionario", HttpMethod.GET,
                    BASE + "/cursos-verano/reportes?formato=" + formato + "&" + verano, null);
            caso(casos, "estudiante", HttpMethod.GET, BASE + "/export/" + formato + "/cursos-verano", null);
            caso(casos, "estudiante", HttpMethod.GET, BASE + "/cursos-verano/reportes?formato=" + formato, null);
        }

        // Solo rutas nuevas: sin formato, o con uno desconocido
        for (String ruta : List.of("/reportes", "/cursos-verano/reportes")) {
            caso(casos, "funcionario", HttpMethod.GET, BASE + ruta, null);
            caso(casos, "funcionario", HttpMethod.GET, BASE + ruta + "?formato=csv", null);
            caso(casos, "funcionario", HttpMethod.GET, BASE + ruta + "?formato=PDF", null);
        }

        // EST-7: las rutas nuevas responden como las antiguas, que pasan a 404; sin formato, o con otro, 400
        GrafoObjetos.compararConReferencia(casos, "rutas/estadisticas-reportes.json",
                "rutas/correcciones/estadisticas-reportes-est7.json");
    }

    @Test
    void retiradas() throws Exception {
        ObjectNode casos = JSON.createObjectNode();
        String fechas = "fechaInicio=2025-01-01&fechaFin=2025-06-30";

        // El CRUD con verbos: la escritura era solo del administrador
        for (String quien : List.of("admin", "funcionario", "estudiante")) {
            caso(casos, quien, HttpMethod.POST, BASE + "/crear", "{}");
            caso(casos, quien, HttpMethod.PUT, BASE + "/actualizar", "{}");
            caso(casos, quien, HttpMethod.DELETE, BASE + "/eliminar/1", null);
        }
        caso(casos, "funcionario", HttpMethod.GET, BASE + "/buscarPorId/1", null);

        // Consultas en camelCase, con filtros en la ruta o con ramas propias
        caso(casos, "funcionario", HttpMethod.GET, BASE + "/porSolicitudPeriodoEstadoPrograma?idEstadistica=1"
                + "&proceso=Reingreso&" + fechas + "&estado=APROBADA&idPrograma=1", null);
        caso(casos, "funcionario", HttpMethod.GET, BASE + "/porPeriodoEstadoPrograma?" + fechas + "&idPrograma=1", null);
        caso(casos, "funcionario", HttpMethod.GET, BASE + "/filtradas", null);
        caso(casos, "funcionario", HttpMethod.GET, BASE + "/filtradas?nombreProceso=Reingreso", null);
        caso(casos, "funcionario", HttpMethod.GET, BASE + "/programa/1/periodo?" + fechas, null);
        caso(casos, "funcionario", HttpMethod.GET, BASE + "/proceso/Reingreso/estado/APROBADA", null);
        caso(casos, "funcionario", HttpMethod.GET, BASE + "/tendencias", null);
        caso(casos, "funcionario", HttpMethod.GET, BASE + "/tendencias?fechaInicio1=2024-07-01&fechaFin1=2024-12-31"
                + "&fechaInicio2=2025-01-01&fechaFin2=2025-06-30", null);

        // Exportaciones del dashboard general
        caso(casos, "funcionario", HttpMethod.GET, BASE + "/export/pdf/general", null);
        caso(casos, "funcionario", HttpMethod.GET, BASE + "/export/excel/general", null);
        caso(casos, "estudiante", HttpMethod.GET, BASE + "/export/pdf/general", null);

        // EST-7: se retiran y responden 404 (también la escritura del funcionario, que antes era un 403)
        GrafoObjetos.compararConReferencia(casos, "rutas/estadisticas-retiradas.json",
                "rutas/correcciones/estadisticas-retiradas-est7.json");
    }

    private void caso(ObjectNode casos, String quien, HttpMethod metodo, String ruta, String cuerpo) throws Exception {
        LLAMADAS.clear();
        MockHttpServletRequestBuilder peticion = request(metodo, ruta);
        if (cuerpo != null) {
            peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
        }
        String rol = switch (quien) {
            case "admin" -> "Administrador";
            case "funcionario" -> "Funcionario";
            case "coordinador" -> "Coordinador";
            case "secretario" -> "Secretario";
            case "decano" -> "Decano";
            case "estudiante" -> "Estudiante";
            default -> null;
        };
        if (rol != null) {
            peticion.with(user(quien + "@prueba.test").roles(rol));
        }
        var respuesta = mvc.perform(peticion).andReturn().getResponse();
        ObjectNode caso = JSON.createObjectNode();
        caso.put("status", respuesta.getStatus());
        caso.put("tipo", respuesta.getContentType());
        String texto = respuesta.getContentAsString(StandardCharsets.UTF_8);
        String tipo = respuesta.getContentType();
        JsonNode valor = texto.isEmpty() ? JSON.nullNode()
                : tipo != null && tipo.contains("json") ? JSON.readTree(texto) : JSON.getNodeFactory().textNode(texto);
        caso.set("cuerpo", valor);
        caso.put("disposicion", respuesta.getHeader(HttpHeaders.CONTENT_DISPOSITION));
        ArrayNode llamadas = caso.putArray("llamadas");
        LLAMADAS.forEach(llamadas::add);
        casos.set(metodo + " " + ruta + " [" + quien + "]", caso);
    }

    private static void anotar(InvocationOnMock i) {
        if (i.getMethod().getDeclaringClass() == Object.class) {
            return;
        }
        List<String> argumentos = new ArrayList<>();
        for (Object argumento : i.getArguments()) {
            argumentos.add(local(argumento));
        }
        LLAMADAS.add(i.getMethod().getName() + "(" + String.join(", ", argumentos) + ")");
    }

    /**
     * Las fechas de los filtros, en la zona de la JVM: el controlador las lee en esa zona, así que el texto no depende
     * de ella (el instante sí).
     */
    private static String local(Object valor) {
        return valor instanceof Date d ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(d) : String.valueOf(valor);
    }
}
