package co.edu.unicauca.decanatura.gestion_curricular.soporte;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Compara los valores de dos volcados de {@link GrafoObjetos} sin tener en cuenta la forma del
 * grafo (DUP-10, decisión de la revisión 15).
 *
 * El mapeo explícito de solicitudes conserva los valores de {@code ModelMapper}, pero no sus copias
 * del mismo objeto ni sus ciclos anidados. Para comparar, cada {@code @ref} se sustituye por el
 * objeto al que apunta y se podan los campos cuya forma cambió:
 * <ul>
 * <li>{@code usuarios} de un rol, que se omite (las copias de {@code ModelMapper} anidadas lo
 * llenaban y las directas no);</li>
 * <li>{@code solicitudes} de un curso, que queda como la lista de sus {@code id_solicitud} (antes
 * eran copias de la clase base);</li>
 * <li>{@code objSolicitud} de un estado o un documento y {@code objCursoOfertadoVerano} de un
 * estado de curso, que quedan como el id al que apuntan.</li>
 * <li>{@code solicitudes} y {@code cursosOfertadosInscritos} de un usuario y {@code usuarios} de
 * un programa, que quedan como sus ids. El mapeo los deja vacíos; solo aparecen llenos en las
 * entidades del repositorio que el gateway guarda sin mapear.</li>
 * </ul>
 */
public final class ProyeccionValores {

    private static final JsonNodeFactory NODOS = JsonNodeFactory.instance;
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Pattern PASO = Pattern.compile("\\.([^.\\[]+)|\\[(\\d+)\\]");
    private static final int PROFUNDIDAD_MAXIMA = 60;

    private ProyeccionValores() {
    }

    /** Compara los valores de {@code casos} con los de una referencia grabada con ModelMapper. */
    public static void compararConReferencia(JsonNode casos, String rutaReferencia) throws Exception {
        JsonNode referencia = JSON.readTree(Files.readString(Path.of("src/test/resources/referencias", rutaReferencia)));
        assertThat(normalizarCasos(casos)).isEqualTo(normalizarCasos(referencia));
    }

    public static JsonNode normalizarCasos(JsonNode casos) {
        ObjectNode resultado = NODOS.objectNode();
        Iterator<Map.Entry<String, JsonNode>> campos = casos.fields();
        while (campos.hasNext()) {
            Map.Entry<String, JsonNode> caso = campos.next();
            resultado.set(caso.getKey(), normalizarCaso(caso.getValue()));
        }
        return resultado;
    }

    private static JsonNode normalizarCaso(JsonNode caso) {
        if (caso.isObject() && caso.size() == 2 && caso.has("resultado") && caso.has("guardados")) {
            ObjectNode guardado = NODOS.objectNode();
            guardado.set("resultado", normalizar(caso.get("resultado"), caso.get("resultado"), 0));
            guardado.set("guardados", normalizar(caso.get("guardados"), caso.get("guardados"), 0));
            return guardado;
        }
        return normalizar(caso, caso, 0);
    }

    private static JsonNode normalizar(JsonNode nodo, JsonNode raiz, int profundidad) {
        if (profundidad > PROFUNDIDAD_MAXIMA) {
            throw new IllegalStateException("Grafo sin podar demasiado profundo: " + nodo.fieldNames().next() + " " + nodo.path("@tipo").asText());
        }
        if (nodo.isArray()) {
            ArrayNode lista = NODOS.arrayNode();
            nodo.forEach(elemento -> lista.add(normalizar(elemento, raiz, profundidad + 1)));
            return lista;
        }
        if (!nodo.isObject()) {
            return nodo;
        }
        if (nodo.has("@ref")) {
            return normalizar(buscar(raiz, nodo.get("@ref").asText()), raiz, profundidad + 1);
        }
        String tipo = nodo.has("@tipo") ? nodo.get("@tipo").asText().replaceFirst("Entity$", "") : "";
        ObjectNode objeto = NODOS.objectNode();
        Iterator<Map.Entry<String, JsonNode>> campos = nodo.fields();
        while (campos.hasNext()) {
            Map.Entry<String, JsonNode> campo = campos.next();
            String nombre = campo.getKey();
            JsonNode valor = campo.getValue();
            if (tipo.equals("Rol") && nombre.equals("usuarios")) {
                continue;
            }
            if ((tipo.equals("CursoOfertadoVerano") || tipo.equals("Usuario")) && nombre.equals("solicitudes")
                    && valor.isArray()) {
                objeto.set(nombre, ids(valor, raiz, "id_solicitud"));
            } else if (tipo.equals("Usuario") && nombre.equals("cursosOfertadosInscritos") && valor.isArray()) {
                objeto.set(nombre, ids(valor, raiz, "id_curso"));
            } else if (tipo.equals("Programa") && nombre.equals("usuarios") && valor.isArray()) {
                objeto.set(nombre, ids(valor, raiz, "id_usuario"));
            } else if ((tipo.equals("EstadoSolicitud") || tipo.equals("Documento")) && nombre.equals("objSolicitud")) {
                objeto.set(nombre, id(valor, raiz, "id_solicitud"));
            } else if (tipo.equals("EstadoCursoOfertado") && nombre.equals("objCursoOfertadoVerano")) {
                objeto.set(nombre, id(valor, raiz, "id_curso"));
            } else {
                objeto.set(nombre, normalizar(valor, raiz, profundidad + 1));
            }
        }
        return objeto;
    }

    private static ArrayNode ids(JsonNode lista, JsonNode raiz, String campo) {
        ArrayNode ids = NODOS.arrayNode();
        lista.forEach(elemento -> ids.add(id(elemento, raiz, campo)));
        return ids;
    }

    private static JsonNode id(JsonNode nodo, JsonNode raiz, String campo) {
        if (nodo == null || nodo.isNull()) {
            return NullNode.getInstance();
        }
        JsonNode objeto = nodo.has("@ref") ? buscar(raiz, nodo.get("@ref").asText()) : nodo;
        return objeto.has(campo) ? objeto.get(campo) : NullNode.getInstance();
    }

    private static JsonNode buscar(JsonNode raiz, String ruta) {
        if (!ruta.startsWith("$")) {
            throw new IllegalArgumentException("Ruta no válida: " + ruta);
        }
        JsonNode actual = raiz;
        Matcher paso = PASO.matcher(ruta.substring(1));
        while (paso.find()) {
            if (paso.group(1) != null) {
                String campo = paso.group(1).equals("get") && actual.has("@optional") ? "@optional" : paso.group(1);
                actual = actual.get(campo);
            } else {
                actual = actual.get(Integer.parseInt(paso.group(2)));
            }
            if (actual == null) {
                throw new IllegalArgumentException("Ruta sin destino: " + ruta);
            }
        }
        return actual;
    }
}
