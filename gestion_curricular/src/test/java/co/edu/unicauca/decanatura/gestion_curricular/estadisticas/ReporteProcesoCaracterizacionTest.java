package co.edu.unicauca.decanatura.gestion_curricular.estadisticas;

import java.util.TimeZone;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarEstadisticasCUIntPort.DatosReporte;
import co.edu.unicauca.decanatura.gestion_curricular.soporte.GrafoObjetos;

/**
 * Caracterización de {@code prepararReporte} (los datos de /export/pdf y /export/excel): para cada texto de proceso,
 * si el reporte lleva la sección de cursos de verano y qué proceso queda en sus estadísticas.
 */
@DisplayName("prepararReporte: proceso pedido y sección de cursos de verano")
class ReporteProcesoCaracterizacionTest {

    /** null = sin el parámetro. */
    private static final String[] PROCESOS = {
            null, "", "  ", "CURSO_VERANO", "Cursos de Verano", "Curso Intersemestral", "verano", "Reingreso",
            "Homologación", "homolog", "Desconocido" };

    private static final String[] PERIODOS = { null, "2025-2" };

    private static TimeZone zonaOriginal;

    @BeforeAll
    static void preparar() {
        zonaOriginal = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
    }

    @AfterAll
    static void restaurar() {
        TimeZone.setDefault(zonaOriginal);
    }

    @Test
    void seccionDeCursosDeVeranoSegunElProceso() throws Exception {
        GestionarEstadisticasCUIntPort estadisticas = EstadisticasCaracterizacionTest.crearCasoDeUso();
        ObjectNode casos = JsonNodeFactory.instance.objectNode();
        for (String periodo : PERIODOS) {
            for (String proceso : PROCESOS) {
                DatosReporte datos = estadisticas.prepararReporte(proceso, null, periodo, null, null);
                ObjectNode caso = casos.putObject("periodo=[" + periodo + "] proceso=[" + proceso + "]");
                caso.put("incluyeCursosVerano", datos.cursosVerano() != null);
                caso.set("porTipoProceso", GrafoObjetos.volcar(datos.estadisticas().get("porTipoProceso")));
            }
        }
        GrafoObjetos.compararConReferencia(casos, "estadisticas/reporte-proceso.json",
                "estadisticas/correcciones/reporte-proceso-bug13.json");
    }
}
