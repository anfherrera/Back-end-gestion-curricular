package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.List;

/**
 * Predicción de la demanda de cursos de verano. Tiene dos formas:
 * <ul>
 * <li>{@link PrediccionesCalculadas}: la de la regresión sobre el histórico mensual, con recomendaciones y alertas
 * (confiabilidad MEDIA).</li>
 * <li>{@link PrediccionesDeRespaldo}: la que queda si el cálculo falla; la demanda actual y listas vacías
 * (confiabilidad BAJA).</li>
 * </ul>
 */
public sealed interface PrediccionesCursosVerano permits PrediccionesCalculadas, PrediccionesDeRespaldo {

    int demandaEstimadaProximoPeriodo();

    List<DemandaProyectada> materiasConTendenciaCreciente();

    List<DemandaProyectada> materiasConTendenciaDecreciente();

    List<DemandaProyectada> materiasEstables();

    List<DemandaProyectada> programasConTendenciaCreciente();

    List<DemandaProyectada> programasConTendenciaDecreciente();

    String confiabilidad();
}
