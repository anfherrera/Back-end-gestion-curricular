package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.proyecciones;

import java.util.Date;

/** Un cambio de estado de una solicitud, leído como columnas sueltas para las estadísticas. */
public record FilaEstadoEstadistica(Integer idSolicitud, String estado, Date fecha) {
}
