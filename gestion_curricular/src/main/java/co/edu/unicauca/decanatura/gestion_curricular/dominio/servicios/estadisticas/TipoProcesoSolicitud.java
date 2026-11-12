package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas;

import java.text.Normalizer;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;

/**
 * Determina el tipo de proceso de una solicitud a partir de su clase, no del texto de su nombre.
 * El texto del nombre varía (tildes, "Inscripción", etc.) y producía conteos incorrectos en las estadísticas.
 */
public final class TipoProcesoSolicitud {

    public static final String HOMOLOGACION = "Homologacion";
    public static final String PAZ_Y_SALVO = "Paz y Salvo";
    public static final String REINGRESO = "Reingreso";
    public static final String CURSOS_VERANO = "Cursos de Verano";
    public static final String ECAES = "ECAES";

    private TipoProcesoSolicitud() {
    }

    /**
     * @return uno de HOMOLOGACION, PAZ_Y_SALVO, REINGRESO, CURSOS_VERANO, ECAES, o null si no se reconoce.
     */
    public static String de(Solicitud solicitud) {
        if (solicitud == null) {
            return null;
        }
        if (solicitud instanceof SolicitudHomologacion) {
            return HOMOLOGACION;
        }
        if (solicitud instanceof SolicitudPazYSalvo) {
            return PAZ_Y_SALVO;
        }
        if (solicitud instanceof SolicitudReingreso) {
            return REINGRESO;
        }
        if (solicitud instanceof SolicitudCursoVeranoPreinscripcion
                || solicitud instanceof SolicitudCursoVeranoIncripcion
                || solicitud instanceof SolicitudCursoVerano) {
            return CURSOS_VERANO;
        }
        if (solicitud instanceof SolicitudEcaes) {
            return ECAES;
        }
        // Solicitud genérica sin subtipo: último recurso, por el nombre
        return porNombre(solicitud.getNombre_solicitud());
    }

    /** Clasificación por texto, sin tildes ni mayúsculas. Solo para solicitudes sin subtipo. */
    public static String porNombre(String nombre) {
        if (nombre == null) {
            return null;
        }
        String n = Normalizer.normalize(nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
        if (n.contains("homolog")) {
            return HOMOLOGACION;
        }
        if (n.contains("paz") && n.contains("salvo")) {
            return PAZ_Y_SALVO;
        }
        if (n.contains("reingreso")) {
            return REINGRESO;
        }
        if (n.contains("ecaes")) {
            return ECAES;
        }
        if (n.contains("verano") || n.contains("intersemestral") || n.contains("inscripcion") || n.contains("curso")) {
            return CURSOS_VERANO;
        }
        return null;
    }
}
