package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas;

import java.util.Date;
import java.util.Map;

/** Resumen completo de todas las solicitudes, sin filtros. */
public record ResumenSinFiltros(Globales estadisticasGlobales, Map<String, ResumenDelProceso> porTipoProceso,
        Map<String, ResumenDelEstado> porEstado, int totalProgramas, Date fechaGeneracion) implements ResumenCompleto {
}
