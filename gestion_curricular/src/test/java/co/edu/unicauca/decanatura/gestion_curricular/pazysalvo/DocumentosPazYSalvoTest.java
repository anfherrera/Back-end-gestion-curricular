package co.edu.unicauca.decanatura.gestion_curricular.pazysalvo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.DocumentosPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.DocumentosPazYSalvo.Requerido;

@DisplayName("Paz y Salvo - reglas por nombre de documento")
class DocumentosPazYSalvoTest {

    private static final String TIPO_FORMATO = "Formato PM-FO-4-FOR-27";

    @ParameterizedTest(name = "\"{0}\" es el formato")
    @ValueSource(strings = { "formato_pm_fo_4_for_27.pdf", "pm_fo_4_for_27.pdf", "Formato PM-FO-4-FOR-27.pdf",
            "FORMATO-PM-FO-4-FOR-27.PDF", "PM FO 4 FOR 27.pdf", "pmfo4for27.pdf", "pm-fo_4 for-27 firmado.pdf" })
    @DisplayName("BUG-1: el formato se reconoce con cualquier separador, igual en el tipo y en los requeridos")
    void formatoConCualquierSeparador(String nombre) {
        assertThat(DocumentosPazYSalvo.esFormatoPmFo4For27(nombre)).isTrue();
        assertThat(DocumentosPazYSalvo.tipoDeDocumento(nombre)).isEqualTo(TIPO_FORMATO);
        assertThat(DocumentosPazYSalvo.requeridoDe(nombre)).contains(Requerido.FORMATO_PM_FO_4_FOR_27);
    }

    @ParameterizedTest(name = "\"{0}\" no es el formato")
    @ValueSource(strings = { "formato.pdf", "formato de pago.pdf", "pm-fo-4-for-28.pdf", "pm-fo-4.pdf" })
    @DisplayName("BUG-1: la palabra \"formato\" sola no basta")
    void sinCodigoNoEsFormato(String nombre) {
        assertThat(DocumentosPazYSalvo.esFormatoPmFo4For27(nombre)).isFalse();
        assertThat(DocumentosPazYSalvo.tipoDeDocumento(nombre)).isNotEqualTo(TIPO_FORMATO);
        assertThat(DocumentosPazYSalvo.requeridoDe(nombre)).isNotEqualTo(Optional.of(Requerido.FORMATO_PM_FO_4_FOR_27));
    }

    @Test
    @DisplayName("Un oficio con el código en el nombre sigue siendo oficio")
    void oficioTienePrioridad() {
        assertThat(DocumentosPazYSalvo.tipoDeDocumento("paz_y_salvo_pm_fo_4_for_27.pdf")).isEqualTo("Oficio/Resolución");
    }

    @Test
    @DisplayName("Completo con los cinco obligatorios; Saber Pro no hace falta")
    void todosCompletos() {
        var revision = DocumentosPazYSalvo.revisar(documentos("pm_fo_4_for_27.pdf", "autorizacion_publicar.pdf",
                "hoja_vida_academica.pdf", "comprobante_pago.pdf", "documento_trabajo_grado.pdf"));
        assertThat(revision.todosCompletos()).isTrue();
        assertThat(revision.entregados()).containsEntry(Requerido.RESULTADO_SABER_PRO, false);
        assertThat(revision.totalDocumentos()).isEqualTo(5);
    }

    @Test
    @DisplayName("Cada archivo cuenta para un solo requerido, el primero que encaja")
    void unRequeridoPorArchivo() {
        var revision = DocumentosPazYSalvo.revisar(documentos("pm-fo-4-for-27 comprobante pago.pdf", null));
        assertThat(revision.entregados())
                .containsEntry(Requerido.FORMATO_PM_FO_4_FOR_27, true)
                .containsEntry(Requerido.COMPROBANTE_PAGO, false);
        assertThat(revision.todosCompletos()).isFalse();
        assertThat(revision.totalDocumentos()).isEqualTo(2);
    }

    @Test
    @DisplayName("Sin documentos: nada entregado")
    void sinDocumentos() {
        var revision = DocumentosPazYSalvo.revisar(null);
        assertThat(revision.entregados()).hasSize(Requerido.values().length).doesNotContainValue(true);
        assertThat(revision.totalDocumentos()).isZero();
    }

    private static List<Documento> documentos(String... nombres) {
        return Arrays.stream(nombres).map(n -> {
            Documento d = new Documento();
            d.setNombre(n);
            return d;
        }).toList();
    }
}
