package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

/**
 * Flujo de estados de una solicitud de Paz y Salvo. Es el flujo de tres aprobaciones
 * ({@link FlujoEstadosSolicitud#TRES_APROBACIONES}); esta clase se mantiene como atajo.
 */
public final class TransicionEstadoPazYSalvo {

    public static final String ENVIADA = FlujoEstadosSolicitud.ENVIADA;
    public static final String APROBADA_FUNCIONARIO = FlujoEstadosSolicitud.APROBADA_FUNCIONARIO;
    public static final String APROBADA_COORDINADOR = FlujoEstadosSolicitud.APROBADA_COORDINADOR;
    public static final String APROBADA = FlujoEstadosSolicitud.APROBADA;
    public static final String RECHAZADA = FlujoEstadosSolicitud.RECHAZADA;

    private static final FlujoEstadosSolicitud FLUJO = FlujoEstadosSolicitud.TRES_APROBACIONES;

    private TransicionEstadoPazYSalvo() {
    }

    /** @see FlujoEstadosSolicitud#normalizar(String) */
    public static String normalizar(String estado) {
        return FLUJO.normalizar(estado);
    }

    public static boolean esFinal(String estado) {
        return FLUJO.esFinal(estado);
    }

    /** @see FlujoEstadosSolicitud#validarYObtenerRolResponsable(String, String) */
    public static String validarYObtenerRolResponsable(String estadoActual, String nuevoEstado) {
        return FLUJO.validarYObtenerRolResponsable(estadoActual, nuevoEstado);
    }
}
