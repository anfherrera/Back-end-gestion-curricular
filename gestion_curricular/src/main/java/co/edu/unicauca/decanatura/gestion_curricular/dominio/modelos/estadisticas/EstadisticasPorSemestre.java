package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.Map;

/**
 * Estadísticas de un semestre (1: enero-junio, 2: julio-diciembre), contado como un periodo de fechas.
 *
 * @param fechaInicio      primer día del semestre
 * @param fechaFin         último día del semestre
 * @param anio             año del semestre
 * @param semestre         1 o 2
 * @param periodoAcademico el periodo como YYYY-S
 * @param tipoConsulta     siempre "porSemestre"
 * @param descripcion      "Primer Semestre YYYY" o "Segundo Semestre YYYY"
 */
public record EstadisticasPorSemestre(int totalSolicitudes, int totalAprobadas, int totalRechazadas,
        int totalEnProceso, int totalEnviadas, double porcentajeAprobacion, Map<String, Integer> porEstado,
        Map<String, Integer> porTipoProceso, Map<String, Integer> porPrograma, Date fechaConsulta,
        Date fechaInicio, Date fechaFin, int anio, int semestre, String periodoAcademico, String tipoConsulta,
        String descripcion) implements ResumenSolicitudes {

    public EstadisticasPorSemestre(EstadisticasPorPeriodo periodo, int anio, int semestre, String periodoAcademico,
            String tipoConsulta, String descripcion) {
        this(periodo.totalSolicitudes(), periodo.totalAprobadas(), periodo.totalRechazadas(),
                periodo.totalEnProceso(), periodo.totalEnviadas(), periodo.porcentajeAprobacion(),
                periodo.porEstado(), periodo.porTipoProceso(), periodo.porPrograma(), periodo.fechaConsulta(),
                periodo.fechaInicio(), periodo.fechaFin(), anio, semestre, periodoAcademico, tipoConsulta,
                descripcion);
    }
}
