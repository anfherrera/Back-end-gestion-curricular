package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.List;
import java.util.Map;

/**
 * Predicción que queda si el cálculo falla: la demanda estimada es la actual, las listas van vacías y no hay
 * programas estables, predicción por programa, recomendaciones, alertas ni fecha.
 *
 * @param prediccionesTemporales siempre vacío (se responde {@code {}})
 * @param confiabilidad          siempre BAJA
 */
public record PrediccionesDeRespaldo(int demandaEstimadaProximoPeriodo,
        List<DemandaProyectada> materiasConTendenciaCreciente, List<DemandaProyectada> materiasConTendenciaDecreciente,
        List<DemandaProyectada> materiasEstables, List<DemandaProyectada> programasConTendenciaCreciente,
        List<DemandaProyectada> programasConTendenciaDecreciente, Map<String, Integer> prediccionesTemporales,
        String confiabilidad) implements PrediccionesCursosVerano {
}
