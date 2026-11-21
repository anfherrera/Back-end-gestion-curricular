package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.Map;

/**
 * Dashboard ejecutivo sin filtros: las globales las cuenta la base de datos.
 */
public record DashboardEjecutivoSinFiltros(GlobalesContadas resumenGlobal, Map<String, Integer> topProcesos,
        Map<String, Integer> resumenEstados, Date fechaGeneracion) implements DashboardEjecutivo {
}
