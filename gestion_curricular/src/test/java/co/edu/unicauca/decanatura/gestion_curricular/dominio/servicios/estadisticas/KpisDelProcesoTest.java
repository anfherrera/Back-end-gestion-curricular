package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EstadisticasGenerales.aplicarKpisDelProceso (ARQ-6, salió de EstadisticasRestController)")
class KpisDelProcesoTest {

    private static Map<String, Object> globales() {
        Map<String, Object> g = new HashMap<>();
        g.put("totalSolicitudes", 14);
        g.put("totalAprobadas", 4);
        g.put("totalEnviadas", 5);
        g.put("porTipoProceso", Map.of("Reingreso", 2, "ECAES", 2));
        return g;
    }

    private static Map<String, Object> resumenCon(String proceso, Map<String, Object> stats) {
        Map<String, Object> porTipo = new HashMap<>();
        porTipo.put(proceso, stats);
        Map<String, Object> resumen = new HashMap<>();
        resumen.put("porTipoProceso", porTipo);
        return resumen;
    }

    @Test
    void reemplazaLosKpiPorLosDelProceso() {
        Map<String, Object> stats = Map.of(
                "totalSolicitudes", 3, "totalAprobadas", 1, "totalRechazadas", 1, "totalEnProceso", 1,
                "porcentajeAprobacion", 33.3, "porEstado", Map.of("ENVIADA", 2, "APROBADA", 1));
        Map<String, Object> g = globales();

        EstadisticasGenerales.aplicarKpisDelProceso(g, resumenCon("Reingreso", stats), "Reingreso");

        assertThat(g).containsEntry("totalSolicitudes", 3).containsEntry("totalAprobadas", 1)
                .containsEntry("totalRechazadas", 1).containsEntry("totalEnProceso", 1)
                .containsEntry("totalEnviadas", 2).containsEntry("porcentajeAprobacion", 33.3)
                .containsEntry("porEstado", stats.get("porEstado"))
                .containsEntry("porTipoProceso", Map.of("Reingreso", stats));
    }

    @Test
    void loQueFaltaEnElProcesoValeCero() {
        Map<String, Object> g = globales();

        EstadisticasGenerales.aplicarKpisDelProceso(g, resumenCon("ECAES", Map.of()), "ECAES");

        assertThat(g).containsEntry("totalSolicitudes", 0).containsEntry("totalAprobadas", 0)
                .containsEntry("totalRechazadas", 0).containsEntry("totalEnProceso", 0)
                .containsEntry("totalEnviadas", 0).containsEntry("porcentajeAprobacion", 0.0)
                .containsEntry("porEstado", Map.of());
    }

    @Test
    void sinProcesoOSiNoEstaEnElResumenNoCambiaNada() {
        Map<String, Object> resumen = resumenCon("Reingreso", Map.of("totalSolicitudes", 3));

        Map<String, Object> g = globales();
        EstadisticasGenerales.aplicarKpisDelProceso(g, resumen, null);
        assertThat(g).isEqualTo(globales());

        EstadisticasGenerales.aplicarKpisDelProceso(g, resumen, "Desconocido");
        assertThat(g).isEqualTo(globales());

        EstadisticasGenerales.aplicarKpisDelProceso(g, new HashMap<>(), "Reingreso");
        assertThat(g).isEqualTo(globales());
    }
}
