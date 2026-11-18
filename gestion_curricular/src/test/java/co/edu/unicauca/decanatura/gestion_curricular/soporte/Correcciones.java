package co.edu.unicauca.decanatura.gestion_curricular.soporte;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Referencias de un error corregido sin regrabar la referencia original.
 *
 * Una corrección es un JSON con solo las entradas que cambian, con la misma clave que en la
 * referencia original. {@link #aplicar} las superpone y comprueba que cada clave exista en la
 * original y que su valor sea distinto, para que la corrección no pueda añadir casos ni quedar
 * obsoleta sin que la prueba falle.
 */
public final class Correcciones {

    private static final ObjectMapper JSON = new ObjectMapper();

    private Correcciones() {
    }

    /** Devuelve una copia de {@code referencia} con las entradas de {@code rutaCorreccion} sustituidas. */
    public static ObjectNode aplicar(JsonNode referencia, String rutaCorreccion) throws Exception {
        JsonNode correccion = JSON.readTree(Files.readString(Path.of("src/test/resources/referencias", rutaCorreccion)));
        assertThat(correccion.size()).as(rutaCorreccion).isPositive();
        ObjectNode resultado = ((ObjectNode) referencia).deepCopy();
        Iterator<Map.Entry<String, JsonNode>> entradas = correccion.fields();
        while (entradas.hasNext()) {
            Map.Entry<String, JsonNode> entrada = entradas.next();
            assertThat(resultado.has(entrada.getKey())).as("%s: %s", rutaCorreccion, entrada.getKey()).isTrue();
            assertThat(entrada.getValue()).as("%s: %s", rutaCorreccion, entrada.getKey())
                    .isNotEqualTo(resultado.get(entrada.getKey()));
            resultado.set(entrada.getKey(), entrada.getValue());
        }
        return resultado;
    }
}
