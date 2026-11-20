package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.Map;

/**
 * Resumen de un tipo de proceso dentro del resumen completo. Los desgloses por tipo de proceso y por programa van
 * vacíos; el porcentaje de aprobación lleva un decimal.
 *
 * @param porEstado solicitudes del proceso en cada uno de los cinco estados, aunque sean 0
 */
public record ResumenDelProceso(String tipoProceso, int totalSolicitudes, int totalAprobadas, int totalRechazadas,
        int totalEnProceso, double porcentajeAprobacion, Map<String, Integer> porEstado,
        Map<String, Integer> porTipoProceso, Map<String, Integer> porPrograma, String descripcion,
        Date fechaConsulta) {
}
