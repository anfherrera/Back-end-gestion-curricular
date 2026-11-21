package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;

/**
 * Comparación del total de solicitudes entre dos rangos de fechas.
 *
 * @param periodo1            estadísticas del primer rango
 * @param periodo2            estadísticas del segundo rango
 * @param variacionPorcentual cambio del total del segundo respecto del primero (0 si el primero no tiene)
 * @param tendencia           CRECIENTE, DECRECIENTE o ESTABLE
 * @param fechaConsulta       momento de la consulta
 */
public record TendenciasPorPeriodo(EstadisticasPorPeriodo periodo1, EstadisticasPorPeriodo periodo2,
        double variacionPorcentual, String tendencia, Date fechaConsulta) {
}
