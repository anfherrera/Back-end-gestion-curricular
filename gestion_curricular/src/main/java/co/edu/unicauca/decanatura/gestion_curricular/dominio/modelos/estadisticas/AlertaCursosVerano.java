package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.List;

/**
 * Alerta crítica de la predicción de demanda de cursos de verano: una materia con crecimiento muy alto
 * ({@link AlertaPorMateria}) o un programa con demanda concentrada ({@link AlertaPorPrograma}).
 */
public sealed interface AlertaCursosVerano permits AlertaPorMateria, AlertaPorPrograma {

    String id();

    String tipo();

    String categoria();

    String prioridad();

    String titulo();

    String descripcion();

    List<String> acciones();
}
