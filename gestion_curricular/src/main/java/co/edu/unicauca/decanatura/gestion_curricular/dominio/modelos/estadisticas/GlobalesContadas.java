package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.Map;

/**
 * Estadísticas globales que cuenta la base de datos, con o sin filtros de proceso, programa y fechas. El porcentaje
 * de aprobación lleva un decimal.
 */
public record GlobalesContadas(int totalSolicitudes, int totalAprobadas, int totalRechazadas, int totalEnProceso,
        int totalEnviadas, double porcentajeAprobacion, Map<String, Integer> porEstado,
        Map<String, Integer> porTipoProceso, Map<String, Integer> porPrograma, Date fechaConsulta)
        implements Globales, ResumenSolicitudes {
}
