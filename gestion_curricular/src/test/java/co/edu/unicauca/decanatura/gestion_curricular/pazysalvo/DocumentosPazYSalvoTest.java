package co.edu.unicauca.decanatura.gestion_curricular.pazysalvo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.DocumentosPazYSalvo;

@DisplayName("Paz y Salvo - reglas por nombre de documento")
class DocumentosPazYSalvoTest {

    private static final String TIPO_FORMATO = "Formato PM-FO-4-FOR-27";

    @ParameterizedTest(name = "\"{0}\" es el formato")
    @ValueSource(strings = { "formato_pm_fo_4_for_27.pdf", "pm_fo_4_for_27.pdf", "Formato PM-FO-4-FOR-27.pdf",
            "FORMATO-PM-FO-4-FOR-27.PDF", "PM FO 4 FOR 27.pdf", "pmfo4for27.pdf", "pm-fo_4 for-27 firmado.pdf" })
    @DisplayName("BUG-1: el formato se reconoce con cualquier separador")
    void formatoConCualquierSeparador(String nombre) {
        assertThat(DocumentosPazYSalvo.esFormatoPmFo4For27(nombre)).isTrue();
        assertThat(DocumentosPazYSalvo.tipoDeDocumento(nombre)).isEqualTo(TIPO_FORMATO);
    }

    @ParameterizedTest(name = "\"{0}\" no es el formato")
    @ValueSource(strings = { "formato.pdf", "formato de pago.pdf", "pm-fo-4-for-28.pdf", "pm-fo-4.pdf" })
    @DisplayName("BUG-1: la palabra \"formato\" sola no basta")
    void sinCodigoNoEsFormato(String nombre) {
        assertThat(DocumentosPazYSalvo.esFormatoPmFo4For27(nombre)).isFalse();
        assertThat(DocumentosPazYSalvo.tipoDeDocumento(nombre)).isNotEqualTo(TIPO_FORMATO);
    }

    @Test
    @DisplayName("Un oficio con el código en el nombre sigue siendo oficio")
    void oficioTienePrioridad() {
        assertThat(DocumentosPazYSalvo.tipoDeDocumento("paz_y_salvo_pm_fo_4_for_27.pdf")).isEqualTo("Oficio/Resolución");
    }
}
