package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

/**
 * Nombre descriptivo de una solicitud: "{tipo} - {nombre del estudiante}", o solo "{tipo}"
 * si no se conoce el nombre. Ej.: "Solicitud de Reingreso - Ana Pérez".
 */
public final class NombreSolicitud {

    public static final String ECAES = "Solicitud ECAES";
    public static final String REINGRESO = "Solicitud de Reingreso";
    public static final String HOMOLOGACION = "Solicitud de Homologación";
    public static final String PAZ_Y_SALVO = "Solicitud Paz y Salvo";
    public static final String CURSO_VERANO = "Solicitud Curso Verano";

    private NombreSolicitud() {
    }

    /** Usa el primer nombre no vacío de {@code nombresEstudiante}. */
    public static String de(String tipo, String... nombresEstudiante) {
        for (String nombre : nombresEstudiante) {
            if (nombre != null && !nombre.isBlank()) {
                return tipo + " - " + nombre.trim();
            }
        }
        return tipo;
    }

    /** Nombre del usuario, o null si no hay usuario. */
    public static String nombreDe(Usuario usuario) {
        return usuario != null ? usuario.getNombre_completo() : null;
    }
}
