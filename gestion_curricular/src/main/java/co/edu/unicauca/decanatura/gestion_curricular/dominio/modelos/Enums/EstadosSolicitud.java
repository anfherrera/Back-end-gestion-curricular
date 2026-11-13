package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums;

/**
 * Estados de las solicitudes de homologación, reingreso, paz y salvo y ECAES.
 * El flujo entre ellos está en {@code FlujoEstadosSolicitud}.
 *
 * <p>Hay dos formas de escribir cada estado:
 * <ul>
 *   <li>{@link #name()}: la forma canónica. La devuelve {@code FlujoEstadosSolicitud.normalizar},
 *       la usan las estadísticas y sale en el JSON.</li>
 *   <li>{@link #textoGuardado()}: lo que se escribe en {@code estado_actual} y se usa en las consultas.
 *       Coincide con {@code name()} salvo en {@link #ENVIADA}: las solicitudes se crean con
 *       {@code "Enviada"} y los cambios de estado guardan la forma canónica.</li>
 * </ul>
 */
public enum EstadosSolicitud {
    ENVIADA("Enviada"),
    APROBADA_FUNCIONARIO,
    APROBADA_COORDINADOR,
    APROBADA,
    RECHAZADA,
    PRE_REGISTRADO;

    private final String textoGuardado;

    EstadosSolicitud() {
        this.textoGuardado = name();
    }

    EstadosSolicitud(String textoGuardado) {
        this.textoGuardado = textoGuardado;
    }

    /** Texto con el que este estado se guarda en la base de datos. */
    public String textoGuardado() {
        return textoGuardado;
    }
}
