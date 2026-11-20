package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.Map;

/**
 * Solicitudes por tipo de proceso de un programa en un rango de fechas.
 *
 * @param idPrograma     programa consultado
 * @param fechaInicio    inicio del rango
 * @param fechaFin       fin del rango
 * @param porTipoProceso solicitudes por tipo de proceso
 * @param fechaConsulta  momento de la consulta
 */
public record ProcesosPorProgramaYPeriodo(Integer idPrograma, Date fechaInicio, Date fechaFin,
        Map<String, Integer> porTipoProceso, Date fechaConsulta) {
}
