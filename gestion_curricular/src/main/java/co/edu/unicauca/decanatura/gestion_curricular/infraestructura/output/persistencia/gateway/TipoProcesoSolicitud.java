package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.text.Normalizer;

import org.hibernate.Hibernate;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudCursoVeranoInscripcionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudCursoVeranoPreinscripcionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEcaesEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudHomologacionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudPazYSalvoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudReingresoEntity;

/**
 * Determina el tipo de proceso de una solicitud a partir de su clase (herencia JOINED),
 * no del texto de su nombre. El texto del nombre varía (tildes, "Inscripción", etc.)
 * y producía conteos incorrectos en las estadísticas.
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
    public static String de(SolicitudEntity solicitud) {
        if (solicitud == null) {
            return null;
        }
        Object real = Hibernate.unproxy(solicitud);
        if (real instanceof SolicitudHomologacionEntity) {
            return HOMOLOGACION;
        }
        if (real instanceof SolicitudPazYSalvoEntity) {
            return PAZ_Y_SALVO;
        }
        if (real instanceof SolicitudReingresoEntity) {
            return REINGRESO;
        }
        if (real instanceof SolicitudCursoVeranoPreinscripcionEntity
                || real instanceof SolicitudCursoVeranoInscripcionEntity) {
            return CURSOS_VERANO;
        }
        if (real instanceof SolicitudEcaesEntity) {
            return ECAES;
        }
        // Solicitud genérica sin subtipo: último recurso, por el nombre
        return porNombre(((SolicitudEntity) real).getNombre_solicitud());
    }

    /** Clasificación por texto, sin tildes ni mayúsculas. Solo para solicitudes sin subtipo. */
    static String porNombre(String nombre) {
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
