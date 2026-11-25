package co.edu.unicauca.decanatura.gestion_curricular.verano;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.*;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.Correcciones;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.SoporteMvc;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores.*;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.*;

class DecisionesVeranoCaracterizacionTest {
    @Test
    void cuerposDeDecisionConservanCoercionesYValidaciones() throws Exception {
        var cu = mock(GestionarSolicitudCursoVeranoCUIntPort.class);
        var pre = new SolicitudCursoVeranoPreinscripcion();
        pre.setIdSolicitud(7);
        when(cu.aprobarPreinscripcion(anyInt(), nullable(String.class))).thenReturn(pre);
        when(cu.rechazarPreinscripcion(anyInt(), nullable(String.class))).thenReturn(pre);
        var curso = mock(GestionarCursoOfertadoVeranoCUIntPort.class);
        // EST-7 (paso 8a): CursoOfertadoRestController se retira; sus rutas fijan el 404
        var mvc = SoporteMvc.standaloneSetup(
                new PreinscripcionesVeranoRestController(cu, mock(CursosOfertadosMapperDominio.class)),
                new InscripcionesVeranoRestController(curso, cu))
                .setControllerAdvice(new RestApiExceptionHandler()).build();
        String[] rutas = {
            "/api/cursos-verano/preinscripciones/7/aprobar",
            "/api/cursos-verano/preinscripciones/7/rechazar",
            "/api/cursos-intersemestrales/preinscripciones/7/aprobar",
            "/api/cursos-intersemestrales/preinscripciones/7/rechazar",
            "/api/cursos-intersemestrales/preinscripciones/7/observaciones",
            "/api/cursos-intersemestrales/inscripciones/7/aceptar",
            "/api/cursos-intersemestrales/inscripciones/7/rechazar"
        };
        String[] cuerpos = {"", "null", "{}", "{\"motivo\":null,\"comentarios\":null,\"observaciones\":null}",
            "{\"motivo\":\"  prueba  \",\"comentarios\":\"  nota  \",\"observaciones\":\"  texto  \"}",
            "{\"motivo\":12,\"comentarios\":false,\"observaciones\":45}",
            "{\"extra\":{\"x\":1}}", "[]"};
        var json = new ObjectMapper();
        Map<String, Object> resultados = new LinkedHashMap<>();
        // EST-7 (paso 8b): la clave conserva la ruta antigua y se pide la nueva; en PUT …/estado, los cuerpos que
        // son objetos llevan además el nuevoEstado de la decisión (los demás no lo admiten)
        Map<String, String[]> nuevas = Map.of(
            "/api/cursos-intersemestrales/preinscripciones/7/aprobar", new String[] {"/api/cursos-verano/preinscripciones/7/estado", "aprobada"},
            "/api/cursos-intersemestrales/preinscripciones/7/rechazar", new String[] {"/api/cursos-verano/preinscripciones/7/estado", "rechazada"},
            "/api/cursos-intersemestrales/preinscripciones/7/observaciones", new String[] {"/api/cursos-verano/preinscripciones/7/observaciones", null},
            "/api/cursos-intersemestrales/inscripciones/7/aceptar", new String[] {"/api/cursos-verano/inscripciones/7/estado", "aceptada"},
            "/api/cursos-intersemestrales/inscripciones/7/rechazar", new String[] {"/api/cursos-verano/inscripciones/7/estado", "rechazada"});
        for (String ruta : rutas) {
            for (int i = 0; i < cuerpos.length; i++) {
                String[] nueva = nuevas.getOrDefault(ruta, new String[] {ruta, null});
                String cuerpo = cuerpos[i];
                if (nueva[1] != null && cuerpo.startsWith("{")) {
                    cuerpo = "{\"nuevoEstado\":\"" + nueva[1] + "\"" + (cuerpo.equals("{}") ? "}" : "," + cuerpo.substring(1));
                }
                var respuesta = mvc.perform(put(nueva[0]).contentType(MediaType.APPLICATION_JSON).content(cuerpo)).andReturn().getResponse();
                resultados.put(ruta + " #" + i, Map.of("status", respuesta.getStatus(),
                    "body", respuesta.getContentAsByteArray().length == 0 ? json.nullNode() : json.readTree(respuesta.getContentAsByteArray())));
            }
        }
        var actual = json.valueToTree(resultados);
        Path referencia = Path.of("src/test/resources/referencias/verano-endpoints/decisiones.json");
        if (Boolean.getBoolean("grabarReferencia")) {
            assertThat(Files.exists(referencia)).isFalse();
            Files.writeString(referencia, json.writerWithDefaultPrettyPrinter().writeValueAsString(actual));
        }
        // EST-7 (paso 8a): aprobar y rechazar de /api/cursos-verano se retiran y responden 404
        var esperado = Correcciones.aplicar(json.readTree(Files.readString(referencia)),
                "verano-endpoints/correcciones/decisiones-est7-paso8a.json");
        // EST-7 (paso 8b): PUT …/estado exige un objeto con nuevoEstado; los cuerpos vacíos, null o listas dan 400
        esperado = Correcciones.aplicar(esperado, "verano-endpoints/correcciones/decisiones-est7-paso8b.json");
        assertThat(actual).isEqualTo(esperado);
    }
}
