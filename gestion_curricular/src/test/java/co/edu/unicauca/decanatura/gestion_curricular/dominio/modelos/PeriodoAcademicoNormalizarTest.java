package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.PeriodoAcademicoEnum;

@DisplayName("PeriodoAcademicoEnum.normalizar (ARQ-6, salió de EstadisticasRestController)")
class PeriodoAcademicoNormalizarTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    void nuloOEnBlancoEsNull(String periodo) {
        assertThat(PeriodoAcademicoEnum.normalizar(periodo)).isNull();
    }

    @ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
    @CsvSource(delimiter = '|', value = {
            "2025-1                  | 2025-1",
            "' 2025-2 '              | 2025-2",
            "Segundo Período 2025    | 2025-2",
            "2025 II                 | 2025-2",
            "primer semestre 1999    | 1999-1",
            "Semestre 1 de 1987      | 1987-1",
            "sin año                 | sin año",
            "' periodo actual '      | periodo actual" })
    void llevaAFormatoYYYYP(String periodo, String esperado) {
        assertThat(PeriodoAcademicoEnum.normalizar(periodo)).isEqualTo(esperado);
    }

    /** BUG-12: el dígito 2 del año ya no cuenta como "segundo período"; un período inexistente queda tal cual. */
    @ParameterizedTest(name = "BUG-12: \"{0}\" -> \"{1}\"")
    @CsvSource(delimiter = '|', value = {
            "2025 - Primer Semestre  | 2025-1",
            "Primer semestre 2024    | 2024-1",
            "2025-3                  | 2025-3",
            "' 2025-3 '              | 2025-3",
            "2031-1er                | 2031-1",
            "2022 Segundo Semestre   | 2022-2",
            "I-2022                  | 2022-1",
            "2022 - II               | 2022-2" })
    void bug12ElAnioNoCuentaParaElPeriodo(String periodo, String esperado) {
        assertThat(PeriodoAcademicoEnum.normalizar(periodo)).isEqualTo(esperado);
    }
}
