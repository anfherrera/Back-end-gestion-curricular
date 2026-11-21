package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ReglasEstadisticas: reglas que salieron de EstadisticasRestController (ARQ-6)")
class ReglasEstadisticasTest {

    @Nested
    @DisplayName("tasaProcesamiento")
    class TasaProcesamiento {

        @Test
        void decididasSobreElTotalSinRedondear() {
            assertThat(ReglasEstadisticas.tasaProcesamiento(14, 4, 4)).isEqualTo(57.14285714285714);
        }

        @Test
        void sinSolicitudesEsCero() {
            assertThat(ReglasEstadisticas.tasaProcesamiento(0, 3, 1)).isZero();
            assertThat(ReglasEstadisticas.tasaProcesamiento(null, 3, 1)).isZero();
        }

        @Test
        void losConteosNulosValenCero() {
            assertThat(ReglasEstadisticas.tasaProcesamiento(10, null, 5)).isEqualTo(50.0);
            assertThat(ReglasEstadisticas.tasaProcesamiento(10, null, null)).isZero();
        }
    }

    @Nested
    @DisplayName("eficienciaAprobacion")
    class EficienciaAprobacion {

        @Test
        void aprobadasSobreLasDecididasSinRedondear() {
            assertThat(ReglasEstadisticas.eficienciaAprobacion(1, 2)).isEqualTo(33.33333333333333);
        }

        @Test
        void sinDecididasEsCero() {
            assertThat(ReglasEstadisticas.eficienciaAprobacion(0, 0)).isZero();
            assertThat(ReglasEstadisticas.eficienciaAprobacion(null, null)).isZero();
        }

        @Test
        void losConteosNulosValenCero() {
            assertThat(ReglasEstadisticas.eficienciaAprobacion(null, 4)).isZero();
            assertThat(ReglasEstadisticas.eficienciaAprobacion(4, null)).isEqualTo(100.0);
        }
    }
}
