package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.Map;

public interface GenerarReportesEstadisticasIntPort {
    byte[] pdfGeneral(Map<String, Object> estadisticas);
    byte[] pdfCursosVerano(Map<String, Object> datos, String periodo, Integer programa);
    byte[] pdfCompleto(Map<String, Object> estadisticas, Map<String, Object> cursos, String periodo, Integer programa);
    byte[] excel(Map<String, Object> estadisticas, Map<String, Object> cursos, String periodo, Integer programa);
}
