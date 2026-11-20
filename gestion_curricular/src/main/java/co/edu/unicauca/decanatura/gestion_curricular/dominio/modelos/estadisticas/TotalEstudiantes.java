package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;

/**
 * Número de estudiantes registrados en el sistema.
 *
 * @param totalEstudiantes cuántos estudiantes hay registrados
 * @param fechaConsulta    momento de la consulta
 * @param descripcion      texto fijo que describe el dato
 */
public record TotalEstudiantes(long totalEstudiantes, Date fechaConsulta, String descripcion) {
}
