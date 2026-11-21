package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.List;

/**
 * Recomendación de anticipar la oferta del mes pico (tipo PLANIFICACION_TEMPORAL).
 */
public record RecomendacionTemporal(String id, String tipo, String categoria, String prioridad, String titulo,
        String mesPico, int solicitudesActuales, int solicitudesProyectadas, int incrementoEsperado,
        int porcentajeCrecimiento, String descripcion, List<String> acciones, String justificacion)
        implements RecomendacionCursosVerano {
}
