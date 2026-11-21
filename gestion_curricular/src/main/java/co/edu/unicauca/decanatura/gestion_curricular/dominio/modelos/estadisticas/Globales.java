package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.Map;

/**
 * Estadísticas globales de las solicitudes. Tienen tres formas, según de dónde salen:
 * <ul>
 * <li>{@link GlobalesContadas}: las cuenta la base de datos (con o sin filtros de proceso, programa y fechas);
 * traen el desglose por estado.</li>
 * <li>{@link GlobalesDelPeriodo}: las del resumen de un periodo académico y programa; sin desglose por estado.</li>
 * <li>{@link GlobalesDelProceso}: las del periodo con los KPI de un proceso; porTipoProceso lleva el resumen
 * completo de ese proceso en lugar de un conteo.</li>
 * </ul>
 */
public sealed interface Globales permits GlobalesContadas, GlobalesDelPeriodo, GlobalesDelProceso {

    int totalSolicitudes();

    int totalAprobadas();

    int totalRechazadas();

    int totalEnProceso();

    int totalEnviadas();

    double porcentajeAprobacion();

    /** Solicitudes por tipo de proceso (o, en {@link GlobalesDelProceso}, el resumen del proceso). */
    Map<String, ?> porTipoProceso();

    Map<String, Integer> porPrograma();

    Date fechaConsulta();
}
