package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.Map;

/**
 * Estadísticas de las solicitudes cuyo estado actual es uno dado. El desglose por estado va vacío.
 *
 * @param estado            estado tal como se pidió
 * @param descripcionEstado descripción del estado
 */
public record EstadisticasPorEstado(int totalSolicitudes, int totalAprobadas, int totalRechazadas,
        int totalEnProceso, int totalEnviadas, double porcentajeAprobacion, Map<String, Integer> porEstado,
        Map<String, Integer> porTipoProceso, Map<String, Integer> porPrograma, Date fechaConsulta,
        String estado, String descripcionEstado) implements ResumenSolicitudes {

    public EstadisticasPorEstado(ConteoSolicitudes conteo, Date fechaConsulta, String estado,
            String descripcionEstado) {
        this(conteo.totalSolicitudes(), conteo.totalAprobadas(), conteo.totalRechazadas(), conteo.totalEnProceso(),
                conteo.totalEnviadas(), conteo.porcentajeAprobacion(), conteo.porEstado(), conteo.porTipoProceso(),
                conteo.porPrograma(), fechaConsulta, estado, descripcionEstado);
    }
}
