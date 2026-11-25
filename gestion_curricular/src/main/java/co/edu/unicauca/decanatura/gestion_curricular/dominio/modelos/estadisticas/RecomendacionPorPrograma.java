package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.List;

/**
 * Recomendación de priorizar la oferta para un programa que crece (tipo ENFOQUE_PROGRAMA).
 */
public record RecomendacionPorPrograma(String id, String tipo, String categoria, String prioridad, String titulo,
        String programa, int solicitudesActuales, int solicitudesProyectadas, int incremento, int porcentajeIncremento,
        String descripcion, List<String> acciones, String justificacion, List<String> beneficios)
        implements RecomendacionCursosVerano {
}
