package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.Map;

/**
 * Dashboard ejecutivo con filtros. Con periodo o programa, las globales son las del resumen completo (con los KPI
 * del proceso si se pidió); solo con proceso, las cuenta la base de datos.
 *
 * @param filtrosAplicados solo los filtros pedidos: periodoAcademico (texto normalizado), idPrograma (número) y
 *                         proceso (el texto recibido, sin normalizar)
 */
public record DashboardEjecutivoFiltrado(Globales resumenGlobal, Map<String, ?> topProcesos,
        Map<String, Integer> resumenEstados, Date fechaGeneracion, Map<String, Object> filtrosAplicados)
        implements DashboardEjecutivo {
}
