package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.Map;

/**
 * Solicitudes por programa de un tipo de proceso con un estado actual dado.
 *
 * @param tipoProceso   proceso tal como se pidió
 * @param estado        estado tal como se pidió
 * @param porPrograma   solicitudes por nombre de programa
 * @param fechaConsulta momento de la consulta
 */
public record ProgramasPorProcesoYEstado(String tipoProceso, String estado, Map<String, Integer> porPrograma,
        Date fechaConsulta) {
}
