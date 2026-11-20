package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.Map;

/**
 * Vista del dashboard de tendencias y comparativas: crecimiento entre los dos últimos meses con datos,
 * comparativas por proceso y por programa, y la recomendación estratégica.
 *
 * @param crecimientoTemporal  crecimiento de solicitudes y de estudiantes activos
 * @param comparativaProcesos  demanda y eficiencia de cada proceso
 * @param comparativaProgramas solicitudes y estudiantes de cada programa
 * @param resumenEstrategico   totales y recomendación
 * @param fechaConsulta        momento de la consulta
 * @param descripcion          descripción de la vista
 */
public record VistaTendencias(CrecimientoTemporal crecimientoTemporal, ComparativaProcesos comparativaProcesos,
        ComparativaProgramas comparativaProgramas, ResumenEstrategico resumenEstrategico, Date fechaConsulta,
        String descripcion) {

    /**
     * Tendencia (Creciente, Decreciente, Estable o Nueva si solo hay un mes con datos) y variación porcentual
     * entre los dos últimos meses con datos, con los conteos de cada mes.
     */
    public record CrecimientoTemporal(String tendenciaSolicitudes, double crecimientoSolicitudes,
            String tendenciaEstudiantes, double crecimientoEstudiantes, int mesesAnalizados, int totalSolicitudes,
            int totalEstudiantes, Map<String, Integer> solicitudesPorMes, Map<String, Integer> estudiantesPorMes) {
    }

    /** Solicitudes y aprobadas de cada proceso; los nombres son null si ningún proceso destaca. */
    public record ComparativaProcesos(String procesoMasDemandado, String procesoMasEficiente,
            double eficienciaMasAlta, int demandaMasAlta, Map<String, Integer> solicitudesPorProceso,
            Map<String, Integer> aprobadasPorProceso) {
    }

    /** Solicitudes y estudiantes de cada programa; los nombres son null si ningún programa destaca. */
    public record ComparativaProgramas(String programaMasActivo, String programaConMasEstudiantes,
            int maxSolicitudes, int maxEstudiantes, Map<String, Integer> solicitudesPorPrograma,
            Map<String, Integer> estudiantesPorPrograma) {
    }

    /** Totales del sistema y recomendación estratégica. */
    public record ResumenEstrategico(int totalSolicitudes, int totalEstudiantes, int totalProgramas,
            String periodoAnalizado, String recomendacionEstrategica) {
    }
}
