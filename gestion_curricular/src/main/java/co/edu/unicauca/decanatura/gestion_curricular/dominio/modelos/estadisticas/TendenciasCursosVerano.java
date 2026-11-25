package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.List;

/**
 * Tendencias temporales de los cursos de verano: la demanda de cada uno de los doce meses, aunque sea 0.
 *
 * @param todosLosMeses        los doce meses, de enero a diciembre
 * @param tendenciasTemporales demanda de cada mes, en el mismo orden
 * @param totalSolicitudes     solicitudes consideradas
 * @param fechaConsulta        momento de la consulta
 * @param descripcion          descripción de la consulta
 */
public record TendenciasCursosVerano(List<String> todosLosMeses, List<DemandaMensualCursosVerano> tendenciasTemporales,
        int totalSolicitudes, Date fechaConsulta, String descripcion) {
}
