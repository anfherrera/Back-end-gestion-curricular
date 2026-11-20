package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

/**
 * Resultado de pedir las estadísticas de un periodo académico: las del semestre o, si el texto no tiene el formato
 * YYYY-1 o YYYY-2, el aviso de formato inválido.
 */
public sealed interface EstadisticasPeriodoAcademico permits EstadisticasPorSemestre, PeriodoAcademicoInvalido {
}
