package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.Map;

/**
 * Estadísticas globales del resumen de un periodo académico y programa, contadas sobre sus solicitudes. No traen
 * desglose por estado; el porcentaje de aprobación lleva dos decimales.
 */
public record GlobalesDelPeriodo(int totalSolicitudes, int totalAprobadas, int totalRechazadas, int totalEnProceso,
        int totalEnviadas, double porcentajeAprobacion, Map<String, Integer> porTipoProceso,
        Map<String, Integer> porPrograma, Date fechaConsulta) implements Globales {
}
