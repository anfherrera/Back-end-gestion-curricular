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

/** HTTP y JSON completos antes de ARQ-6. Casos de uso reales, salidas con datos fijos.
 * No normaliza valores: los ids y las fechas de los datos son constantes.
 * Compara árboles JSON, por lo que el orden de campos de Map.of no interviene.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles({"test", "caracterizacion-verano"})
@org.springframework.security.test.context.support.WithMockUser(roles = "Funcionario")
class CursosVeranoEndpointsCaracterizacionTest {
    private static final String BASE = "/api/cursos-intersemestrales";
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
            curso.setId_curso(71);
            curso.setSalon(numero);
            curso.setCupo_estimado(20);
            curso.setPeriodo_academico("2025-2");
            curso.setFecha_inicio(Date.from(Instant.parse("2025-07-01T12:00:00Z")));
            when(cursos.listarTodos()).thenReturn(List.of(curso));
            when(cursos.listarPorPeriodo(anyString())).thenReturn(List.of(curso));
            when(cursos.obtenerCursoPorId(71)).thenReturn(curso);
            when(cursos.guardarCambiosCurso(eq(71), any(), any(), any())).thenReturn(curso);
            SolicitudCursoVeranoPreinscripcion solicitud = new SolicitudCursoVeranoPreinscripcion();
            solicitud.setId_solicitud(91);
            solicitud.setNombre_solicitud("Curso de prueba");
            solicitud.setFecha_registro_solicitud(curso.getFecha_inicio());
            solicitud.setPeriodo_academico("2025-2");
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
    // El @ComponentScan explícito de la aplicación también descubre @TestConfiguration.
    // Este perfil evita que los datos fijos sustituyan casos de uso en las demás pruebas.
    @org.springframework.context.annotation.Profile("caracterizacion-verano")
    static class Configuracion {
        @Bean Datos datosVerano() { return new Datos(); }
        @Bean @Primary GestionarSalonesCUIntPort salonesFijos(Datos d, FormateadorResultadosIntPort f) {
            return new GestionarSalonesCUAdapter(d.salones, f);
        }
        @Bean @Primary GestionarCursoOfertadoVeranoCUIntPort cursosFijos(Datos d, FormateadorResultadosIntPort f) {
            return new GestionarCursoOfertadoVeranoCUAdapter(d.cursos, mock(GestionarSolicitudGatewayIntPort.class),
                    mock(GestionarUsuarioGatewayIntPort.class), mock(GestionarMateriasIntPort.class),
                    mock(GestionarDocenteGatewayIntPort.class), d.salones, f, mock(GestionarNotificacionCUIntPort.class));
        }
        @Bean @Primary GestionarSolicitudCursoVeranoCUIntPort solicitudesFijas(Datos d, FormateadorResultadosIntPort f) {
            return new GestionarSolicitudCursoVeranoCUAdapter(d.solicitudes, d.cursos,
                    mock(GestionarUsuarioGatewayIntPort.class), mock(GestionarDocumentosGatewayIntPort.class), f,
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
            consultar(respuestas, caso[0] + "/todos", get(BASE + "/cursos-verano/todos"));
            consultar(respuestas, caso[0] + "/detalle", get(BASE + "/cursos-verano/71"));
            consultar(respuestas, caso[0] + "/editar", put(BASE + "/cursos-verano/71")
                    .contentType(MediaType.APPLICATION_JSON).content("{\"cupo_estimado\":20}"));
        }
        comparar("salones", respuestas);
    }

    @Test void periodosDelListado() throws Exception {
        ObjectNode respuestas = JSON.createObjectNode();
        datos.preparar("221", "encontrado");
        String[] periodos = {null, "", "   ", "todos", "2025-1", " 2025-2 ",
                "2025 - Primer Semestre", "Segundo Período 2025", "primer semestre 1999", "2025-3", "sin año"};
        for (int i = 0; i < periodos.length; i++) {
            var peticion = get(BASE + "/cursos-verano/todos");
            if (periodos[i] != null) peticion.param("periodoAcademico", periodos[i]);
            consultar(respuestas, "caso-" + i, peticion);
        }
        comparar("periodos-listado", respuestas);
    }

    @Test void periodosDelReporte() throws Exception {
        ObjectNode respuestas = JSON.createObjectNode();
        datos.preparar("221", "encontrado");
        String[] periodos = {null, "", "   ", "todos", "2025-1", " 2025-2 ",
                "2025 - Primer Semestre", "Segundo Período 2025", "primer semestre 1999", "2025-3", "sin año"};
        for (int i = 0; i < periodos.length; i++) {
            var peticion = get(BASE + "/solicitudes");
            if (periodos[i] != null) peticion.param("periodoAcademico", periodos[i]);
            consultar(respuestas, "caso-" + i, peticion);
        }
        comparar("periodos-reporte", respuestas);
    }

    @Test void permisosPorEstado() throws Exception {
        ObjectNode respuestas = JSON.createObjectNode();
        for (String estado : List.of("Borrador", "Abierto", "Publicado", "Preinscripcion", "Inscripcion", "Cerrado", "desconocido")) {
            for (String rol : List.of("FUNCIONARIO", "COORDINADOR", "ESTUDIANTE", "ADMINISTRADOR", "funcionario")) {
                consultar(respuestas, estado + "/" + rol, get(BASE + "/permisos-estado/{estado}/{rol}", estado, rol));
            }
        }
        comparar("permisos", respuestas);
    }

    private void consultar(ObjectNode r, String clave, MockHttpServletRequestBuilder peticion) throws Exception {
        var respuesta = mvc.perform(peticion).andReturn().getResponse();
        assertThat(respuesta.getStatus()).isEqualTo(200);
        ObjectNode valor = r.putObject(clave);
        valor.put("http", respuesta.getStatus());
        valor.set("json", JSON.readTree(respuesta.getContentAsByteArray()));
    }

    private void comparar(String nombre, JsonNode actual) throws Exception {
        Path archivo = REFERENCIAS.resolve(nombre + ".json");
        if (Boolean.getBoolean("grabarReferencia")) {
            Files.createDirectories(REFERENCIAS);
            Files.writeString(archivo, JSON.writerWithDefaultPrettyPrinter().writeValueAsString(actual) + "\n");
        }
        assertThat(actual).isEqualTo(JSON.readTree(Files.readString(archivo)));
    }
}
