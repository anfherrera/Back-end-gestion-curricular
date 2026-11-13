package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("TipoProcesoSolicitud.delFiltro (ARQ-6, salió de EstadisticasRestController)")
class TipoProcesoSolicitudTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "  " })
    void nuloOEnBlancoEsNull(String proceso) {
        assertThat(TipoProcesoSolicitud.delFiltro(proceso)).isNull();
    }

    @ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
    @CsvSource(delimiter = '|', value = {
            "Reingreso                    | Reingreso",
            "homologación                 | Homologacion",
            "HOMOLOGACION                 | Homologacion",
            "Solicitud de Homologación    | Homologacion",
            "paz_y_salvo                  | Paz y Salvo",
            "Paz y Salvo                  | Paz y Salvo",
            "ECAES                        | ECAES",
            "Curso Intersemestral         | Cursos de Verano",
            "CURSO_VERANO                 | Cursos de Verano",
            "reingreso y homologacion     | Reingreso",
            "' Desconocido '              | Desconocido" })
    void llevaALaClaveDelProceso(String proceso, String esperado) {
        assertThat(TipoProcesoSolicitud.delFiltro(proceso)).isEqualTo(esperado);
    }

    /**
     * BUG-13: el filtro y {@link TipoProcesoSolicitud#porNombre} no clasifican igual el mismo texto. Se fija lo que
     * devuelve cada uno hoy ("null" = null).
     */
    @ParameterizedTest(name = "BUG-13: \"{0}\": delFiltro={1}, porNombre={2}")
    @CsvSource(delimiter = '|', nullValues = "null", value = {
            "verano                    | verano              | Cursos de Verano",
            "intersemestral            | intersemestral      | Cursos de Verano",
            "Inscripción               | Inscripción         | Cursos de Verano",
            "homolog                   | homolog             | Homologacion",
            "Desconocido               | Desconocido         | null",
            "reingreso y homologacion  | Reingreso           | Homologacion" })
    void bug13DelFiltroYPorNombreDifieren(String texto, String delFiltro, String porNombre) {
        assertThat(TipoProcesoSolicitud.delFiltro(texto)).isEqualTo(delFiltro);
        assertThat(TipoProcesoSolicitud.porNombre(texto)).isEqualTo(porNombre);
    }
}
