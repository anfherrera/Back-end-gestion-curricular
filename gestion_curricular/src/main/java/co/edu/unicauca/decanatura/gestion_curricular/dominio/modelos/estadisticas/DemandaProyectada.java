package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

/**
 * Predicción de demanda de una materia o un programa para el próximo periodo de verano.
 *
 * @param nombre              materia o programa
 * @param demandaActual       solicitudes del periodo actual
 * @param demandaEstimada     solicitudes estimadas para el próximo periodo
 * @param tendencia           CRECIENTE, DECRECIENTE o ESTABLE
 * @param variacion           demanda estimada menos la actual
 * @param porcentajeVariacion variación sobre la demanda actual, redondeada (0 si la demanda actual es 0)
 */
public record DemandaProyectada(String nombre, int demandaActual, int demandaEstimada, String tendencia,
        int variacion, int porcentajeVariacion) {
}
