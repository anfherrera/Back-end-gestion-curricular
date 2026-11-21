package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.List;

/**
 * Recomendación de ampliar la oferta de una materia que crece (tipo OFERTA_MATERIA), con los recursos que necesita
 * en grupos de 20 estudiantes.
 */
public record RecomendacionPorMateria(String id, String tipo, String categoria, String prioridad, String titulo,
        String materia, int demandaActual, int demandaProyectada, int crecimiento, int porcentajeCrecimiento,
        Recursos recursos, String descripcion, List<String> acciones, String justificacion, Impacto impacto)
        implements RecomendacionCursosVerano {

    /**
     * Docentes, aulas y laboratorios necesarios, y grupos actuales y recomendados.
     */
    public record Recursos(int docentes, int aulas, int laboratorios, int gruposActuales, int gruposRecomendados,
            int capacidadPorGrupo) {
    }

    /**
     * Estudiantes atendidos y beneficiados si se amplía la oferta.
     */
    public record Impacto(int estudiantesAtendidos, int estudiantesBeneficiados, String tasaCoberturaObjetivo) {
    }
}
