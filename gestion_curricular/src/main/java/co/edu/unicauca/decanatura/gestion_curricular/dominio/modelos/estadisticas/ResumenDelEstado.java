package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.Map;

/**
 * Resumen de un estado dentro del resumen completo. El desglose por estado va vacío; los totales de aprobadas,
 * rechazadas y en proceso son el total del estado o 0, y el porcentaje de aprobación es 100 solo para APROBADA.
 */
public record ResumenDelEstado(String estado, int totalSolicitudes, int totalAprobadas, int totalRechazadas,
        int totalEnProceso, double porcentajeAprobacion, Map<String, Integer> porTipoProceso,
        Map<String, Integer> porPrograma, Map<String, Integer> porEstado, String descripcionEstado,
        Date fechaConsulta) {
}
