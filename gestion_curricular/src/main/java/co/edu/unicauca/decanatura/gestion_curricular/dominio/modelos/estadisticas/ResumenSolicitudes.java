package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.Map;

/**
 * Lo que tienen en común las estadísticas de solicitudes sobre un filtro: los totales, el porcentaje de aprobación,
 * los desgloses y el momento de la consulta.
 */
public interface ResumenSolicitudes {

    int totalSolicitudes();

    int totalAprobadas();

    int totalRechazadas();

    int totalEnProceso();

    double porcentajeAprobacion();

    Map<String, Integer> porEstado();

    Map<String, Integer> porTipoProceso();

    Map<String, Integer> porPrograma();

    Date fechaConsulta();
}
