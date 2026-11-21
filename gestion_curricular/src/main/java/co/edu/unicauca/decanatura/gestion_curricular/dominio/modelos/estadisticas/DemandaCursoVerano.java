package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

/**
 * Solicitudes de cursos de verano de una materia o un programa y su porcentaje sobre el total, con dos decimales.
 */
public record DemandaCursoVerano(String nombre, int solicitudes, double porcentaje) {
}
