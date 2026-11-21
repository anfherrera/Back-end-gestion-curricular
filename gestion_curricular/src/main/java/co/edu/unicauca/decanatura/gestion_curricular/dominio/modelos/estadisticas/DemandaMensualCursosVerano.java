package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

/**
 * Solicitudes de cursos de verano registradas en un mes y su porcentaje sobre el total, con dos decimales (0.0 si
 * no hay solicitudes).
 */
public record DemandaMensualCursosVerano(String mes, int solicitudes, double porcentaje) {
}
