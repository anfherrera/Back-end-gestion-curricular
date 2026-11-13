package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.util.Locale;
import java.util.Set;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.PeriodoAcademicoEnum;

/** Cómo interpretan los listados de solicitudes el parámetro {@code periodoAcademico}. */
final class PeriodoDeConsulta {

    private static final Set<String> TODOS = Set.of("todos", "todos los periodos", "todos los períodos");

    private PeriodoDeConsulta() {
    }

    /**
     * Listados de pendientes: el período pedido o, si no viene, el actual ({@link PeriodoAcademicoEnum#oActual}).
     * @return null (sin filtro) solo si no viene y la fecha no corresponde a ningún período conocido
     */
    static String oActual(String periodoAcademico) {
        return PeriodoAcademicoEnum.oActual(periodoAcademico);
    }

    /**
     * Listados de procesadas: el período pedido, o null (todos) si no viene o es "todos",
     * "todos los periodos" o "todos los períodos".
     */
    static String oTodos(String periodoAcademico) {
        if (periodoAcademico == null || periodoAcademico.trim().isEmpty()
                || TODOS.contains(periodoAcademico.trim().toLowerCase(Locale.ROOT))) {
            return null;
        }
        return periodoAcademico.trim();
    }
}
