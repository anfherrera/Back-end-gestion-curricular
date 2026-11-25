package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;

/**
 * Estadísticas globales sin filtros con los indicadores de rendimiento.
 *
 * @param estadisticasGlobales   las que cuenta la base de datos
 * @param indicadoresRendimiento tasas derivadas de ellas
 * @param fechaConsulta          momento de la consulta
 */
public record Rendimiento(GlobalesContadas estadisticasGlobales, Indicadores indicadoresRendimiento,
        Date fechaConsulta) {

    /**
     * Tasa de procesamiento (decididas sobre el total) y eficiencia de aprobación (aprobadas sobre las decididas),
     * en porcentaje y sin redondear; el tiempo promedio no se calcula ("N/A").
     */
    public record Indicadores(double tasaProcesamiento, double eficienciaAprobacion,
            String tiempoPromedioProcesamiento) {
    }
}
