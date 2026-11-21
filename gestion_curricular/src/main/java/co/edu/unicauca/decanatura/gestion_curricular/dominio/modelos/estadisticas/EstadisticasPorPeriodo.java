package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.Map;

/**
 * Estadísticas de las solicitudes con algún estado dentro de un rango de fechas.
 *
 * @param fechaInicio inicio del rango
 * @param fechaFin    fin del rango
 */
public record EstadisticasPorPeriodo(int totalSolicitudes, int totalAprobadas, int totalRechazadas,
        int totalEnProceso, int totalEnviadas, double porcentajeAprobacion, Map<String, Integer> porEstado,
        Map<String, Integer> porTipoProceso, Map<String, Integer> porPrograma, Date fechaConsulta,
        Date fechaInicio, Date fechaFin) implements ResumenSolicitudes {

    public EstadisticasPorPeriodo(ConteoSolicitudes conteo, Date fechaConsulta, Date fechaInicio, Date fechaFin) {
        this(conteo.totalSolicitudes(), conteo.totalAprobadas(), conteo.totalRechazadas(), conteo.totalEnProceso(),
                conteo.totalEnviadas(), conteo.porcentajeAprobacion(), conteo.porEstado(), conteo.porTipoProceso(),
                conteo.porPrograma(), fechaConsulta, fechaInicio, fechaFin);
    }
}
