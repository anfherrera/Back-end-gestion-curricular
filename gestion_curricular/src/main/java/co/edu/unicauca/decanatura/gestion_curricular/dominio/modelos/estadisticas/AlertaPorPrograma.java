package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.List;

/**
 * Alerta de un programa que crece y supera las 10 solicitudes estimadas (tipo ALERTA_PROGRAMA).
 */
public record AlertaPorPrograma(String id, String tipo, String categoria, String prioridad, String titulo,
        String programa, int demandaProyectada, String descripcion, List<String> acciones)
        implements AlertaCursosVerano {
}
