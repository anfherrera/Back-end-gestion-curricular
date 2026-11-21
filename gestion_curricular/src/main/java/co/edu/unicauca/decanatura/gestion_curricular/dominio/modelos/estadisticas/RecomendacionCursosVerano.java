package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.List;

/**
 * Recomendación que se deriva de la predicción de demanda de cursos de verano: ampliar la oferta de una materia
 * ({@link RecomendacionPorMateria}), priorizar un programa ({@link RecomendacionPorPrograma}) o anticipar la oferta
 * del mes pico ({@link RecomendacionTemporal}).
 */
public sealed interface RecomendacionCursosVerano
        permits RecomendacionPorMateria, RecomendacionPorPrograma, RecomendacionTemporal {

    String id();

    String tipo();

    String categoria();

    /** ALTA, MEDIA o BAJA. */
    String prioridad();

    String titulo();

    String descripcion();

    List<String> acciones();

    String justificacion();
}
