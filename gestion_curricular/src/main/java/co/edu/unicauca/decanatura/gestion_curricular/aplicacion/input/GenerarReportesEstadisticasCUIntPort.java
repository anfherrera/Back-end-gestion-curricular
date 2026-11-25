package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.Date;

public interface GenerarReportesEstadisticasCUIntPort {
    byte[] pdfCursosVerano(String periodo, Integer programa);
    byte[] pdfCompleto(String proceso, Integer programa, String periodo, Date inicio, Date fin);
    byte[] excelCursosVerano(String periodo, Integer programa);
    byte[] excelCompleto(String proceso, Integer programa, String periodo, Date inicio, Date fin);
}
