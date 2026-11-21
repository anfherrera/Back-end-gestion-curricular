package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.Map;

/**
 * Resumen completo: las estadísticas globales y las de cada proceso y estado. Sin filtros es un
 * {@link ResumenSinFiltros}; con periodo, programa o proceso, un {@link ResumenFiltrado}, que los anota.
 */
public sealed interface ResumenCompleto permits ResumenSinFiltros, ResumenFiltrado {

    /** {@link GlobalesDelPeriodo}, o {@link GlobalesDelProceso} si se pidió un proceso. */
    Globales estadisticasGlobales();

    Map<String, ResumenDelProceso> porTipoProceso();

    Map<String, ResumenDelEstado> porEstado();

    int totalProgramas();

    Date fechaGeneracion();
}
