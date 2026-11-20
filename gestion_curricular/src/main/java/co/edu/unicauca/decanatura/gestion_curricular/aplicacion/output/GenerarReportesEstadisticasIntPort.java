package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.Map;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.Globales;

public interface GenerarReportesEstadisticasIntPort {
    byte[] pdfGeneral(Globales estadisticas);
    byte[] pdfCursosVerano(Map<String, Object> datos, String periodo, Integer programa);
    byte[] pdfCompleto(Globales estadisticas, Map<String, Object> cursos, String periodo, Integer programa);
    byte[] excel(Globales estadisticas, Map<String, Object> cursos, String periodo, Integer programa);
}
