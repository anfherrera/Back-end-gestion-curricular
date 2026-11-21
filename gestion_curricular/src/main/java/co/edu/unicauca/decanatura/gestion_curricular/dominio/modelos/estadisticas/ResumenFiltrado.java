package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.Map;

/**
 * Resumen completo con filtros.
 *
 * @param filtrosAplicados solo los filtros pedidos: periodoAcademico (texto), idPrograma (número) y proceso (texto)
 */
public record ResumenFiltrado(Globales estadisticasGlobales, Map<String, ResumenDelProceso> porTipoProceso,
        Map<String, ResumenDelEstado> porEstado, int totalProgramas, Date fechaGeneracion,
        Map<String, Object> filtrosAplicados) implements ResumenCompleto {
}
