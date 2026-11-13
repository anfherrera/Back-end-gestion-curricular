package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadosSolicitud;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Flujo de estados de un tipo de solicitud: qué pasos se permiten y qué rol decide cada uno.
 *
 * <p>{@link #TRES_APROBACIONES} (homologación, reingreso y paz y salvo):
 * <pre>
 * ENVIADA ──(Funcionario)──▶ APROBADA_FUNCIONARIO ──(Coordinador)──▶ APROBADA_COORDINADOR ──(Secretario)──▶ APROBADA
 *    │                              │                                     │
 *    └──────────────(mismo rol)─────┴────────────▶ RECHAZADA ◀────────────┘
 * </pre>
 *
 * <p>{@link #ECAES}:
 * <pre>
 * ENVIADA ──(Funcionario)──▶ PRE_REGISTRADO | RECHAZADA
 * </pre>
 *
 * Los estados sin siguientes son finales. El Administrador puede hacer cualquier paso válido,
 * pero no saltarse pasos.
 */
public final class FlujoEstadosSolicitud {

    // Formas canónicas de EstadosSolicitud, como texto para los mapas del flujo.
    public static final String ENVIADA = EstadosSolicitud.ENVIADA.name();
    public static final String APROBADA_FUNCIONARIO = EstadosSolicitud.APROBADA_FUNCIONARIO.name();
    public static final String APROBADA_COORDINADOR = EstadosSolicitud.APROBADA_COORDINADOR.name();
    public static final String APROBADA = EstadosSolicitud.APROBADA.name();
    public static final String RECHAZADA = EstadosSolicitud.RECHAZADA.name();
    public static final String PRE_REGISTRADO = EstadosSolicitud.PRE_REGISTRADO.name();

    public static final String FUNCIONARIO = "Funcionario";
    public static final String COORDINADOR = "Coordinador";
    public static final String SECRETARIO = "Secretario";

    public static final FlujoEstadosSolicitud TRES_APROBACIONES = new FlujoEstadosSolicitud(
            Map.of(
                    ENVIADA, Set.of(APROBADA_FUNCIONARIO, RECHAZADA),
                    APROBADA_FUNCIONARIO, Set.of(APROBADA_COORDINADOR, RECHAZADA),
                    APROBADA_COORDINADOR, Set.of(APROBADA, RECHAZADA),
                    APROBADA, Set.of(),
                    RECHAZADA, Set.of()),
            Map.of(
                    ENVIADA, FUNCIONARIO,
                    APROBADA_FUNCIONARIO, COORDINADOR,
                    APROBADA_COORDINADOR, SECRETARIO));

    public static final FlujoEstadosSolicitud ECAES = new FlujoEstadosSolicitud(
            Map.of(
                    ENVIADA, Set.of(PRE_REGISTRADO, RECHAZADA),
                    PRE_REGISTRADO, Set.of(),
                    RECHAZADA, Set.of()),
            Map.of(ENVIADA, FUNCIONARIO));

    /** Estado actual → estados siguientes permitidos. */
    private final Map<String, Set<String>> siguientes;
    /** Estado actual → rol que decide en ese paso. */
    private final Map<String, String> rolResponsable;

    /** Clave normalizada (sin tildes, espacios ni guiones bajos) → estado canónico. */
    private final Map<String, String> porClave = new HashMap<>();

    private FlujoEstadosSolicitud(Map<String, Set<String>> siguientes, Map<String, String> rolResponsable) {
        this.siguientes = siguientes;
        this.rolResponsable = rolResponsable;
        siguientes.keySet().forEach(estado -> porClave.put(clave(estado), estado));
    }

    /**
     * Normaliza un estado guardado o recibido ("Enviada", "enviada", "APROBADA FUNCIONARIO"...)
     * a su forma canónica. Un estado vacío o nulo se considera ENVIADA (solicitud recién creada).
     *
     * @return el estado canónico, o null si no pertenece a este flujo
     */
    public String normalizar(String estado) {
        if (estado == null || estado.isBlank()) {
            return ENVIADA;
        }
        return porClave.get(clave(estado));
    }

    /** "Aprobada funcionario", "APROBADA_FUNCIONARIO" y "aprobadaFuncionario" dan la misma clave. */
    private static String clave(String estado) {
        return Normalizer.normalize(estado.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[\\s_]", "")
                .toUpperCase(Locale.ROOT);
    }

    public boolean esFinal(String estado) {
        String e = normalizar(estado);
        return e != null && siguientes.get(e).isEmpty();
    }

    /**
     * Valida la transición y devuelve el rol responsable de ese paso.
     *
     * @throws IllegalArgumentException si alguno de los estados no existe o la transición no está permitida
     */
    public String validarYObtenerRolResponsable(String estadoActual, String nuevoEstado) {
        String actual = normalizar(estadoActual);
        String nuevo = normalizar(nuevoEstado);
        if (actual == null) {
            throw new IllegalArgumentException("El estado actual de la solicitud no es válido: " + estadoActual);
        }
        if (nuevo == null) {
            throw new IllegalArgumentException("Estado no válido: '" + nuevoEstado
                    + "'. Estados permitidos: " + siguientes.keySet());
        }
        Set<String> permitidos = siguientes.get(actual);
        if (!permitidos.contains(nuevo)) {
            if (permitidos.isEmpty()) {
                throw new IllegalArgumentException("La solicitud ya está en estado final (" + actual
                        + ") y no puede cambiar a " + nuevo);
            }
            throw new IllegalArgumentException("No se puede pasar de " + actual + " a " + nuevo
                    + ". Desde " + actual + " solo se permite: " + permitidos);
        }
        return rolResponsable.get(actual);
    }

    /**
     * Estado en el que una solicitud espera la decisión de {@code rol}: ENVIADA para el Funcionario,
     * APROBADA_FUNCIONARIO para el Coordinador y APROBADA_COORDINADOR para el Secretario.
     *
     * @param rol {@link #FUNCIONARIO}, {@link #COORDINADOR} o {@link #SECRETARIO}
     * @return vacío si el rol no decide ningún paso de este flujo (el Coordinador en ECAES)
     */
    public Optional<EstadosSolicitud> estadoPendientePara(String rol) {
        return rolResponsable.entrySet().stream()
                .filter(paso -> paso.getValue().equals(rol))
                .map(paso -> EstadosSolicitud.valueOf(paso.getKey()))
                .findFirst();
    }

    /**
     * Estado al que pasa la solicitud cuando {@code rol} la aprueba (el siguiente que no es RECHAZADA).
     * Sirve para listar lo que el rol ya procesó: APROBADA_FUNCIONARIO para el Funcionario,
     * APROBADA_COORDINADOR para el Coordinador y APROBADA para el Secretario.
     *
     * @return vacío si el rol no decide ningún paso de este flujo
     */
    public Optional<EstadosSolicitud> estadoAprobadoPor(String rol) {
        return estadoPendientePara(rol)
                .flatMap(pendiente -> siguientes.get(pendiente.name()).stream()
                        .filter(siguiente -> !RECHAZADA.equals(siguiente))
                        .map(EstadosSolicitud::valueOf)
                        .findFirst());
    }

    /** Último estado con texto de la lista (ignora registros vacíos). Null si no hay. */
    public static String ultimoEstado(List<EstadoSolicitud> estados) {
        if (estados == null) {
            return null;
        }
        for (int i = estados.size() - 1; i >= 0; i--) {
            String estado = estados.get(i).getEstado_actual();
            if (estado != null && !estado.isBlank()) {
                return estado;
            }
        }
        return null;
    }
}
