package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.HashMap;
import java.util.Map;

/**
 * Conteo de un grupo de solicitudes: totales, porcentaje de aprobación y desgloses por estado, tipo de proceso y
 * programa. Lo comparten las estadísticas que se arman sobre un filtro (proceso, estado, programa o periodo).
 *
 * @param totalSolicitudes     solicitudes del grupo
 * @param totalAprobadas       en estado APROBADA
 * @param totalRechazadas      en estado RECHAZADA
 * @param totalEnProceso       aprobadas por funcionario o por coordinador
 * @param totalEnviadas        en estado ENVIADA
 * @param porcentajeAprobacion aprobadas sobre el total, con un decimal
 * @param porEstado            solicitudes por estado actual
 * @param porTipoProceso       solicitudes por tipo de proceso
 * @param porPrograma          solicitudes por nombre de programa
 */
public record ConteoSolicitudes(int totalSolicitudes, int totalAprobadas, int totalRechazadas, int totalEnProceso,
        int totalEnviadas, double porcentajeAprobacion, Map<String, Integer> porEstado,
        Map<String, Integer> porTipoProceso, Map<String, Integer> porPrograma) {

    /** El mismo conteo sin el desglose por estado (consultas de un estado concreto). */
    public ConteoSolicitudes sinPorEstado() {
        return new ConteoSolicitudes(totalSolicitudes, totalAprobadas, totalRechazadas, totalEnProceso, totalEnviadas,
                porcentajeAprobacion, new HashMap<>(), porTipoProceso, porPrograma);
    }

    /** El mismo conteo sin el desglose por programa (consultas de un programa concreto). */
    public ConteoSolicitudes sinPorPrograma() {
        return new ConteoSolicitudes(totalSolicitudes, totalAprobadas, totalRechazadas, totalEnProceso, totalEnviadas,
                porcentajeAprobacion, porEstado, porTipoProceso, new HashMap<>());
    }

    /** El mismo conteo con otro porcentaje de aprobación. */
    public ConteoSolicitudes conPorcentajeAprobacion(double porcentaje) {
        return new ConteoSolicitudes(totalSolicitudes, totalAprobadas, totalRechazadas, totalEnProceso, totalEnviadas,
                porcentaje, porEstado, porTipoProceso, porPrograma);
    }
}
