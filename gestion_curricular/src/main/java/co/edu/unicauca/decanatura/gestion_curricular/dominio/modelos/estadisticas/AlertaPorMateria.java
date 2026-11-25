package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.List;

/**
 * Alerta de una materia que crece más del 50% (tipo ALERTA_CAPACIDAD); el crecimiento va como texto, p. ej. "60%".
 */
public record AlertaPorMateria(String id, String tipo, String categoria, String prioridad, String titulo,
        String materia, String crecimientoProyectado, String descripcion, List<String> acciones)
        implements AlertaCursosVerano {
}
