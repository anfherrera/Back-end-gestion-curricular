package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.Date;

/**
 * Lo que la predicción de demanda necesita de una solicitud de curso de verano.
 *
 * @param materia       nombre de la materia del curso solicitado, o null
 * @param programa      nombre del programa del estudiante, o null
 * @param fechaRegistro fecha en que se registró la solicitud, o null
 */
public record DatoDemandaCursoVerano(String materia, String programa, Date fechaRegistro) {
}
