package co.edu.unicauca.decanatura.gestion_curricular.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.excepciones.RestApiExceptionHandler;

/**
 * BUG-47: con varias violaciones sobre un mismo campo, el 400 de {@code @Valid} da siempre el mismo mensaje, sea cual
 * sea el orden en que lleguen. Gana la de presencia ({@code @NotNull}, {@code @NotBlank}, {@code @NotEmpty}); después,
 * un orden fijo. La forma {@code {campo: mensaje}} no cambia.
 */
@DisplayName("Manejador global: mensaje de validación estable por campo (BUG-47)")
class ValidacionPrioridadMensajesTest {

    private final RestApiExceptionHandler manejador = new RestApiExceptionHandler();

    @Test
    @DisplayName("@NotBlank gana a @Size en cualquier orden")
    void presenciaAntesQueTamano() throws Exception {
        List<ObjectError> errores = List.of(
                campo("nombre", "Size", "El nombre debe tener entre 3 y 30 caracteres."),
                campo("nombre", "NotBlank", "El nombre no puede estar vacío."));
        for (List<ObjectError> orden : ordenes(errores)) {
            assertThat(responder(orden)).containsExactly(Map.entry("nombre", "El nombre no puede estar vacío."));
        }
    }

    @Test
    @DisplayName("Sin restricción de presencia, el orden fijo decide (Size antes que Pattern) en cualquier orden")
    void ordenFijoEntreLasDemas() throws Exception {
        List<ObjectError> errores = List.of(
                campo("codigo", "Pattern", "El código solo admite letras y números."),
                campo("codigo", "Size", "El código debe tener entre 2 y 10 caracteres."));
        for (List<ObjectError> orden : ordenes(errores)) {
            assertThat(responder(orden))
                    .containsExactly(Map.entry("codigo", "El código debe tener entre 2 y 10 caracteres."));
        }
    }

    @Test
    @DisplayName("Cada campo conserva su mensaje y la forma {campo: mensaje} no cambia")
    void variosCampos() throws Exception {
        List<ObjectError> errores = List.of(
                campo("nombre", "NotNull", "El nombre es obligatorio."),
                campo("creditos", "Min", "Los créditos deben ser al menos 1."),
                new ObjectError("peticion", "La petición no es válida."));
        Map<String, String> cuerpo = responder(errores);
        assertThat(cuerpo).containsOnly(
                Map.entry("nombre", "El nombre es obligatorio."),
                Map.entry("creditos", "Los créditos deben ser al menos 1."),
                Map.entry("peticion", "La petición no es válida."));
    }

    private Map<String, String> responder(List<ObjectError> errores) throws Exception {
        BeanPropertyBindingResult resultado = new BeanPropertyBindingResult(new Object(), "peticion");
        errores.forEach(resultado::addError);
        MethodParameter parametro = new MethodParameter(
                ValidacionPrioridadMensajesTest.class.getDeclaredMethod("responder", List.class), 0);
        ResponseEntity<Map<String, String>> respuesta =
                manejador.handleValidacion(new MethodArgumentNotValidException(parametro, resultado));
        assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
        return respuesta.getBody();
    }

    /** La lista en su orden y al revés. */
    private static List<List<ObjectError>> ordenes(List<ObjectError> errores) {
        List<ObjectError> alReves = new ArrayList<>(errores);
        Collections.reverse(alReves);
        return List.of(errores, alReves);
    }

    /** Un error de campo como los que arma Spring: el último código es el nombre de la restricción. */
    private static FieldError campo(String nombre, String restriccion, String mensaje) {
        return new FieldError("peticion", nombre, "", false,
                new String[] { restriccion + ".peticion." + nombre, restriccion + "." + nombre, restriccion },
                null, mensaje);
    }
}
