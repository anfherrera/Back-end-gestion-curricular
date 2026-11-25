package co.edu.unicauca.decanatura.gestion_curricular.soporte;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.temporal.Temporal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.configuracion.NombresJsonDominio;

/**
 * Vuelca un grafo de objetos del proyecto (modelos de dominio y entidades) a JSON para las
 * caracterizaciones de los mapeos de persistencia (DUP-10).
 *
 * Cada objeto lleva su clase en {@code @tipo} y sus campos, de la superclase a la subclase y por
 * nombre dentro de cada clase. Un objeto que ya apareció en el mismo volcado se escribe como
 * {@code {"@ref": "<ruta de su primera aparición>"}}, así que quedan fijados los ciclos y los
 * objetos compartidos. Las fechas a menos de dos minutos de ahora se escriben como
 * {@code "<ahora>"}; las demás, en milisegundos.
 */
public final class GrafoObjetos {

    private static final String PAQUETE = "co.edu.unicauca.decanatura.gestion_curricular.";
    private static final long MARGEN_AHORA_MS = 120_000L;
    private static final JsonNodeFactory NODOS = JsonNodeFactory.instance;
    private static final ObjectMapper JSON = new ObjectMapper();

    private final IdentityHashMap<Object, String> vistos = new IdentityHashMap<>();
    private final long ahora = System.currentTimeMillis();

    private GrafoObjetos() {
    }

    public static JsonNode volcar(Object objeto) {
        return new GrafoObjetos().nodo(objeto, "$");
    }

    /** Vuelca el resultado de la llamada, o la clase y el mensaje de la excepción que lance. */
    public static JsonNode resultado(Callable<?> llamada) {
        try {
            return volcar(llamada.call());
        } catch (Exception e) {
            ObjectNode error = NODOS.objectNode();
            error.put("excepcion", e.getClass().getName());
            error.put("mensaje", e.getMessage());
            return error;
        }
    }

    /** Graba la referencia con -DgrabarReferencia=true si no existe y compara con ella. */
    public static void compararConReferencia(JsonNode casos, String rutaReferencia) throws Exception {
        Path referencia = Path.of("src/test/resources/referencias", rutaReferencia);
        if (Boolean.getBoolean("grabarReferencia") && Files.notExists(referencia)) {
            Files.createDirectories(referencia.getParent());
            Files.writeString(referencia, JSON.writerWithDefaultPrettyPrinter().writeValueAsString(casos));
        }
        compararConEsperado(casos, JSON.readTree(Files.readString(referencia)), rutaReferencia);
    }

    /** Compara con la referencia después de aplicarle, en orden, una o más correcciones de {@link Correcciones}. */
    public static void compararConReferencia(JsonNode casos, String rutaReferencia, String rutaCorreccion,
            String... otrasCorrecciones) throws Exception {
        JsonNode referencia = JSON.readTree(Files.readString(Path.of("src/test/resources/referencias", rutaReferencia)));
        JsonNode esperado = Correcciones.aplicar(referencia, rutaCorreccion);
        for (String otra : otrasCorrecciones) {
            esperado = Correcciones.aplicar(esperado, otra);
        }
        compararConEsperado(casos, esperado, rutaReferencia);
    }

    private static void compararConEsperado(JsonNode casos, JsonNode esperado, String rutaReferencia)
            throws Exception {
        if (!casos.equals(esperado)) {
            Path obtenido = Path.of("target/referencias-obtenidas", rutaReferencia);
            Files.createDirectories(obtenido.getParent());
            Files.writeString(obtenido, JSON.writerWithDefaultPrettyPrinter().writeValueAsString(casos));
        }
        assertThat(casos).isEqualTo(esperado);
    }

    private JsonNode nodo(Object valor, String ruta) {
        if (valor == null) {
            return NODOS.nullNode();
        }
        if (valor instanceof String texto) {
            return NODOS.textNode(texto);
        }
        if (valor instanceof Integer numero) {
            return NODOS.numberNode(numero);
        }
        if (valor instanceof Long numero) {
            return NODOS.numberNode(numero);
        }
        if (valor instanceof Number numero) {
            return NODOS.textNode(numero.getClass().getSimpleName() + ":" + numero);
        }
        if (valor instanceof Boolean logico) {
            return NODOS.booleanNode(logico);
        }
        if (valor instanceof Enum<?> constante) {
            return NODOS.textNode(constante.getDeclaringClass().getSimpleName() + "." + constante.name());
        }
        if (valor instanceof Date fecha) {
            long ms = fecha.getTime();
            return Math.abs(ms - ahora) < MARGEN_AHORA_MS ? NODOS.textNode("<ahora>") : NODOS.numberNode(ms);
        }
        if (valor instanceof Temporal temporal) {
            return NODOS.textNode(temporal.toString());
        }
        if (valor instanceof Optional<?> opcional) {
            ObjectNode envoltorio = NODOS.objectNode();
            envoltorio.set("@optional", nodo(opcional.orElse(null), ruta + ".get"));
            return envoltorio;
        }
        if (valor instanceof Collection<?> coleccion) {
            ArrayNode lista = NODOS.arrayNode();
            int i = 0;
            for (Object elemento : coleccion) {
                lista.add(nodo(elemento, ruta + "[" + i++ + "]"));
            }
            return lista;
        }
        if (valor instanceof Map<?, ?> mapa) {
            ObjectNode objeto = NODOS.objectNode();
            mapa.forEach((clave, v) -> objeto.set(String.valueOf(clave), nodo(v, ruta + "." + clave)));
            return objeto;
        }
        if (!valor.getClass().getName().startsWith(PAQUETE)) {
            return NODOS.textNode(valor.getClass().getName() + ":" + valor);
        }
        String previa = vistos.get(valor);
        if (previa != null) {
            ObjectNode referencia = NODOS.objectNode();
            referencia.put("@ref", previa);
            return referencia;
        }
        vistos.put(valor, ruta);
        ObjectNode objeto = NODOS.objectNode();
        objeto.put("@tipo", valor.getClass().getSimpleName());
        for (Field campo : campos(valor.getClass())) {
            try {
                campo.setAccessible(true);
                String nombre = historico(campo);
                objeto.set(nombre, nodo(campo.get(valor), ruta + "." + nombre));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        return objeto;
    }

    /** El nombre del campo antes de OPC-2, para que las referencias no cambien. */
    private static String historico(Field campo) {
        return NombresJsonDominio.nombre(campo.getDeclaringClass(), campo.getName());
    }

    private static List<Field> campos(Class<?> clase) {
        Deque<Class<?>> jerarquia = new ArrayDeque<>();
        for (Class<?> c = clase; c != null && c != Object.class; c = c.getSuperclass()) {
            jerarquia.push(c);
        }
        List<Field> campos = new ArrayList<>();
        for (Class<?> c : jerarquia) {
            List<Field> propios = new ArrayList<>();
            for (Field campo : c.getDeclaredFields()) {
                if (!Modifier.isStatic(campo.getModifiers()) && !campo.isSynthetic()) {
                    propios.add(campo);
                }
            }
            propios.sort(Comparator.comparing(GrafoObjetos::historico));
            campos.addAll(propios);
        }
        return campos;
    }
}
