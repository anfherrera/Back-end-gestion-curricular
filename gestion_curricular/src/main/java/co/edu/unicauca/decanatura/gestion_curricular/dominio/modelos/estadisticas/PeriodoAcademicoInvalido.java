package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

/**
 * El periodo académico pedido no tiene el formato YYYY-1 o YYYY-2.
 *
 * @param error texto del aviso
 */
public record PeriodoAcademicoInvalido(String error) implements EstadisticasPeriodoAcademico {
}
