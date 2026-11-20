package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.Map;

/**
 * Estadísticas globales de un periodo con los KPI de un proceso: los totales, el porcentaje y el desglose por
 * estado son los del proceso, porTipoProceso lleva solo su resumen, y porPrograma y fechaConsulta siguen siendo
 * los del periodo.
 *
 * @param totalEnviadas solicitudes del proceso en estado ENVIADA
 */
public record GlobalesDelProceso(int totalSolicitudes, int totalAprobadas, int totalRechazadas, int totalEnProceso,
        int totalEnviadas, double porcentajeAprobacion, Map<String, Integer> porEstado,
        Map<String, ResumenDelProceso> porTipoProceso, Map<String, Integer> porPrograma, Date fechaConsulta)
        implements Globales {
}
