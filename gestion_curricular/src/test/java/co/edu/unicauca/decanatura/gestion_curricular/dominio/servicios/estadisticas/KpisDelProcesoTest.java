package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.Globales;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.GlobalesDelPeriodo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.GlobalesDelProceso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.ResumenCompleto;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.ResumenDelProceso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.ResumenSinFiltros;

@DisplayName("ResumenEstadisticas.conKpisDelProceso (ARQ-6, salió de EstadisticasRestController)")
class KpisDelProcesoTest {

    private static final Date FECHA = new Date(0);

    private static GlobalesDelPeriodo globales() {
        return new GlobalesDelPeriodo(14, 4, 0, 0, 5, 28.57, Map.of("Reingreso", 2, "ECAES", 2),
                Map.of("Sistemas", 14), FECHA);
    }

    private static ResumenDelProceso reingreso() {
        return new ResumenDelProceso("Reingreso", 3, 1, 1, 1, 33.3,
                Map.of("ENVIADA", 2, "APROBADA", 1, "RECHAZADA", 1, "APROBADA_FUNCIONARIO", 1, "APROBADA_COORDINADOR", 0),
                Map.of(), Map.of(), "Reingreso", new Date(1));
    }

    private static ResumenCompleto resumenCon(ResumenDelProceso proceso) {
        return new ResumenSinFiltros(globales(), Map.of(proceso.tipoProceso(), proceso), Map.of(), 1, FECHA);
    }

    @Test
    void reemplazaLosKpiPorLosDelProceso() {
        ResumenDelProceso stats = reingreso();

        Globales g = ResumenEstadisticas.conKpisDelProceso(globales(), resumenCon(stats), "Reingreso");

        assertThat(g).isEqualTo(new GlobalesDelProceso(3, 1, 1, 1, 2, 33.3, stats.porEstado(),
                Map.of("Reingreso", stats), Map.of("Sistemas", 14), FECHA));
    }

    @Test
    void sinProcesoOSiNoEstaEnElResumenNoCambiaNada() {
        ResumenCompleto resumen = resumenCon(reingreso());

        assertThat(ResumenEstadisticas.conKpisDelProceso(globales(), resumen, null)).isEqualTo(globales());
        assertThat(ResumenEstadisticas.conKpisDelProceso(globales(), resumen, "Desconocido")).isEqualTo(globales());
    }
}
