package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.Map;

/**
 * Estadísticas de las solicitudes de un programa. El desglose por programa va vacío.
 *
 * @param idPrograma programa consultado
 */
public record EstadisticasPorPrograma(int totalSolicitudes, int totalAprobadas, int totalRechazadas,
        int totalEnProceso, int totalEnviadas, double porcentajeAprobacion, Map<String, Integer> porEstado,
        Map<String, Integer> porTipoProceso, Map<String, Integer> porPrograma, Date fechaConsulta,
        Integer idPrograma) implements ResumenSolicitudes {

    public EstadisticasPorPrograma(ConteoSolicitudes conteo, Date fechaConsulta, Integer idPrograma) {
        this(conteo.totalSolicitudes(), conteo.totalAprobadas(), conteo.totalRechazadas(), conteo.totalEnProceso(),
                conteo.totalEnviadas(), conteo.porcentajeAprobacion(), conteo.porEstado(), conteo.porTipoProceso(),
                conteo.porPrograma(), fechaConsulta, idPrograma);
    }
}
