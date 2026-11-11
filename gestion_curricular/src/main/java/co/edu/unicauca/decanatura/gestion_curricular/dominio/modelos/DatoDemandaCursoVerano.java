package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.Date;

/**
 * Lo que la predicción de demanda necesita de una solicitud de curso de verano.
 *
 * @param nombreSolicitud nombre de la solicitud (se usa para asociarla a una materia)
 * @param programa        nombre del programa del estudiante, o null
 * @param fechaRegistro   fecha en que se registró la solicitud, o null
 */
public record DatoDemandaCursoVerano(String nombreSolicitud, String programa, Date fechaRegistro) {
}
