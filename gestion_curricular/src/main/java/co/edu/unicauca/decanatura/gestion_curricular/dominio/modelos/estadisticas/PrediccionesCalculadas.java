package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.List;

/**
 * Predicción de demanda calculada con regresión lineal sobre el histórico mensual.
 *
 * @param demandaEstimadaProximoPeriodo    solicitudes estimadas para el próximo periodo completo
 * @param materiasConTendenciaCreciente    materias que crecen
 * @param materiasConTendenciaDecreciente  materias que bajan
 * @param materiasEstables                 materias estables
 * @param programasConTendenciaCreciente   programas que crecen
 * @param programasConTendenciaDecreciente programas que bajan
 * @param programasEstables                programas estables
 * @param todasLasPrediccionesPorPrograma  todos los programas, en el orden del análisis por programa
 * @param prediccionesTemporales           predicción del mes pico
 * @param recomendaciones                  por materia, por programa y, si tuvo solicitudes, del mes pico
 * @param alertasCriticas                  por materia y por programa
 * @param confiabilidad                    siempre MEDIA
 * @param fechaPrediccion                  momento de la predicción
 * @param estadisticasRecomendaciones      conteo de recomendaciones y alertas
 */
public record PrediccionesCalculadas(int demandaEstimadaProximoPeriodo,
        List<DemandaProyectada> materiasConTendenciaCreciente, List<DemandaProyectada> materiasConTendenciaDecreciente,
        List<DemandaProyectada> materiasEstables, List<DemandaProyectada> programasConTendenciaCreciente,
        List<DemandaProyectada> programasConTendenciaDecreciente, List<DemandaProyectada> programasEstables,
        List<DemandaProyectada> todasLasPrediccionesPorPrograma, PrediccionTemporal prediccionesTemporales,
        List<RecomendacionCursosVerano> recomendaciones, List<AlertaCursosVerano> alertasCriticas,
        String confiabilidad, Date fechaPrediccion, EstadisticasRecomendaciones estadisticasRecomendaciones)
        implements PrediccionesCursosVerano {

    /**
     * Mes con más solicitudes, su demanda actual y la estimada (un 20% más, truncada) y los meses recomendados.
     */
    public record PrediccionTemporal(String mesPico, int demandaActualMesPico, int demandaEstimadaMesPico,
            List<String> mesesRecomendados) {
    }

    /**
     * Recomendaciones en total y de cada prioridad, y número de alertas críticas.
     */
    public record EstadisticasRecomendaciones(int totalRecomendaciones, long prioridadAlta, long prioridadMedia,
            long prioridadBaja, int alertasCriticas) {
    }
}
