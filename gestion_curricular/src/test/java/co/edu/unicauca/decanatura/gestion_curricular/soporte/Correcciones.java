package co.edu.unicauca.decanatura.gestion_curricular.soporte;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Referencias de un error corregido sin regrabar la referencia original.
 *
 * Una corrección es un JSON con solo las entradas que cambian. Cada clave es la de una entrada de primer nivel
 * de la referencia original o, si empieza por {@code /}, un JSON Pointer (RFC 6901) a una entrada anidada; en
 * él, {@code /} dentro de un nombre se escribe {@code ~1}. {@link #aplicar} las superpone y comprueba que cada
 * entrada exista en la original y que su valor sea distinto, para que la corrección no pueda añadir casos ni
 * quedar obsoleta sin que la prueba falle.
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
            JsonPointer puntero = JsonPointer.compile(
                    entrada.getKey().startsWith("/") ? entrada.getKey() : "/" + escapar(entrada.getKey()));
            JsonNode padre = resultado.at(puntero.head());
            String nombre = puntero.last().getMatchingProperty();
            assertThat(padre.isObject() && padre.has(nombre)).as("%s: %s", rutaCorreccion, entrada.getKey()).isTrue();
            assertThat(entrada.getValue()).as("%s: %s", rutaCorreccion, entrada.getKey())
                    .isNotEqualTo(padre.get(nombre));
            ((ObjectNode) padre).set(nombre, entrada.getValue());
        }
        return resultado;
    }

    private static String escapar(String nombre) {
        return nombre.replace("~", "~0").replace("/", "~1");
    }
}
