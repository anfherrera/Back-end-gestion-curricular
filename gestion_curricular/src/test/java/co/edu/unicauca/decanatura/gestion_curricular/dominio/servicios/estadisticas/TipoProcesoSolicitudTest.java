package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("TipoProcesoSolicitud.porNombre como filtro de proceso (ARQ-6, BUG-13)")
class TipoProcesoSolicitudTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "  " })
    void nuloOEnBlancoEsNull(String proceso) {
        assertThat(TipoProcesoSolicitud.porNombre(proceso)).isNull();
    }

    @ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
    @CsvSource(delimiter = '|', nullValues = "null", value = {
            "Reingreso                    | Reingreso",
            "homologación                 | Homologacion",
            "HOMOLOGACION                 | Homologacion",
            "Solicitud de Homologación    | Homologacion",
            "paz_y_salvo                  | Paz y Salvo",
            "Paz y Salvo                  | Paz y Salvo",
            "ECAES                        | ECAES",
            "Curso Intersemestral         | Cursos de Verano",
            "CURSO_VERANO                 | Cursos de Verano",
            "reingreso y homologacion     | Homologacion",
            "' Desconocido '              | null" })
    void llevaALaClaveDelProceso(String proceso, String esperado) {
        assertThat(TipoProcesoSolicitud.porNombre(proceso)).isEqualTo(esperado);
    }

    /** BUG-13: los textos en que la regla del filtro con periodo o programa no coincidía con esta. */
    @ParameterizedTest(name = "BUG-13: \"{0}\" -> {1}")
    @CsvSource(delimiter = '|', nullValues = "null", value = {
            "verano                    | Cursos de Verano",
            "intersemestral            | Cursos de Verano",
            "Inscripción               | Cursos de Verano",
            "homolog                   | Homologacion",
            "Desconocido               | null" })
    void bug13UnaSolaRegla(String texto, String esperado) {
        assertThat(TipoProcesoSolicitud.porNombre(texto)).isEqualTo(esperado);
    }
}
