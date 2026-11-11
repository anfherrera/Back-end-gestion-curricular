package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Flujo de estados de una solicitud de Paz y Salvo:
 *
 * <pre>
 * ENVIADA ──(Funcionario)──▶ APROBADA_FUNCIONARIO ──(Coordinador)──▶ APROBADA_COORDINADOR ──(Secretario)──▶ APROBADA
 *    │                              │                                     │
 *    └──────────────(mismo rol)─────┴────────────▶ RECHAZADA ◀────────────┘
 * </pre>
 *
 * APROBADA y RECHAZADA son estados finales. El Administrador puede hacer cualquier
 * transición válida, pero no saltarse pasos.
 */
public final class TransicionEstadoPazYSalvo {

    public static final String ENVIADA = "ENVIADA";
    public static final String APROBADA_FUNCIONARIO = "APROBADA_FUNCIONARIO";
    public static final String APROBADA_COORDINADOR = "APROBADA_COORDINADOR";
    public static final String APROBADA = "APROBADA";
    public static final String RECHAZADA = "RECHAZADA";

    /** Estado actual → (estados siguientes permitidos). */
    private static final Map<String, Set<String>> SIGUIENTES = Map.of(
            ENVIADA, Set.of(APROBADA_FUNCIONARIO, RECHAZADA),
            APROBADA_FUNCIONARIO, Set.of(APROBADA_COORDINADOR, RECHAZADA),
            APROBADA_COORDINADOR, Set.of(APROBADA, RECHAZADA),
            APROBADA, Set.of(),
            RECHAZADA, Set.of());

    /** Estado actual → rol que decide en ese paso. */
    private static final Map<String, String> ROL_RESPONSABLE = Map.of(
            ENVIADA, "Funcionario",
            APROBADA_FUNCIONARIO, "Coordinador",
            APROBADA_COORDINADOR, "Secretario");

    private TransicionEstadoPazYSalvo() {
    }

    /**
     * Normaliza un estado guardado o recibido ("Enviada", "enviada", "APROBADA FUNCIONARIO"...)
     * a su forma canónica. Un estado vacío o nulo se considera ENVIADA (solicitud recién creada).
     *
     * @return el estado canónico, o null si no es un estado de Paz y Salvo
     */
    public static String normalizar(String estado) {
        if (estado == null || estado.isBlank()) {
            return ENVIADA;
        }
        String clave = Normalizer.normalize(estado.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replace(' ', '_');
        return SIGUIENTES.containsKey(clave) ? clave : null;
    }

    public static boolean esFinal(String estado) {
        String e = normalizar(estado);
        return APROBADA.equals(e) || RECHAZADA.equals(e);
    }

    /**
     * Valida la transición y devuelve el rol responsable de ese paso.
     *
     * @throws IllegalArgumentException si alguno de los estados no existe o la transición no está permitida
     */
    public static String validarYObtenerRolResponsable(String estadoActual, String nuevoEstado) {
        String actual = normalizar(estadoActual);
        String nuevo = normalizar(nuevoEstado);
        if (actual == null) {
            throw new IllegalArgumentException("El estado actual de la solicitud no es válido: " + estadoActual);
        }
        if (nuevo == null) {
            throw new IllegalArgumentException("Estado no válido para Paz y Salvo: '" + nuevoEstado
                    + "'. Estados permitidos: " + SIGUIENTES.keySet());
        }
        if (!SIGUIENTES.get(actual).contains(nuevo)) {
            if (SIGUIENTES.get(actual).isEmpty()) {
                throw new IllegalArgumentException("La solicitud ya está en estado final (" + actual
                        + ") y no puede cambiar a " + nuevo);
            }
            throw new IllegalArgumentException("No se puede pasar de " + actual + " a " + nuevo
                    + ". Desde " + actual + " solo se permite: " + SIGUIENTES.get(actual));
        }
        return ROL_RESPONSABLE.get(actual);
    }
}
