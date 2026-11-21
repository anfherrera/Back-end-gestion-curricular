package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.Map;

/**
 * Dashboard ejecutivo: el resumen global, los procesos y los estados. Sin filtros es un
 * {@link DashboardEjecutivoSinFiltros}; con periodo, programa o proceso, un {@link DashboardEjecutivoFiltrado},
 * que los anota.
 */
public sealed interface DashboardEjecutivo permits DashboardEjecutivoSinFiltros, DashboardEjecutivoFiltrado {

    Globales resumenGlobal();

    /** El porTipoProceso de las globales. */
    Map<String, ?> topProcesos();

    /** El desglose por estado de las globales; null si son {@link GlobalesDelPeriodo}, que no lo traen. */
    Map<String, Integer> resumenEstados();

    Date fechaGeneracion();
}
