package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.Map;

/**
 * Estadísticas de las solicitudes de un tipo de proceso. Los desgloses por tipo de proceso y por programa van vacíos.
 *
 * @param tipoProceso nombre del proceso tal como se pidió
 * @param descripcion descripción del proceso
 */
public record EstadisticasPorProceso(int totalSolicitudes, int totalAprobadas, int totalRechazadas,
        int totalEnProceso, int totalEnviadas, double porcentajeAprobacion, Map<String, Integer> porEstado,
        Map<String, Integer> porTipoProceso, Map<String, Integer> porPrograma, Date fechaConsulta,
        String tipoProceso, String descripcion) implements ResumenSolicitudes {

    public EstadisticasPorProceso(ConteoSolicitudes conteo, Date fechaConsulta, String tipoProceso,
            String descripcion) {
        this(conteo.totalSolicitudes(), conteo.totalAprobadas(), conteo.totalRechazadas(), conteo.totalEnProceso(),
                conteo.totalEnviadas(), conteo.porcentajeAprobacion(), conteo.porEstado(), conteo.porTipoProceso(),
                conteo.porPrograma(), fechaConsulta, tipoProceso, descripcion);
    }
}
