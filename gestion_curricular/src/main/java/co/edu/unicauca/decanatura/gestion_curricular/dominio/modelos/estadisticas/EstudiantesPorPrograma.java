package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.Map;

/**
 * Distribución de los estudiantes por programa académico.
 *
 * @param estudiantesPorPrograma número de estudiantes por nombre de programa (sin los que no tienen programa)
 * @param totalEstudiantes       estudiantes considerados, incluidos los que no tienen programa
 * @param fechaConsulta          momento de la consulta
 * @param descripcion            texto fijo que describe el dato
 */
public record EstudiantesPorPrograma(Map<String, Integer> estudiantesPorPrograma, int totalEstudiantes,
        Date fechaConsulta, String descripcion) {
}
