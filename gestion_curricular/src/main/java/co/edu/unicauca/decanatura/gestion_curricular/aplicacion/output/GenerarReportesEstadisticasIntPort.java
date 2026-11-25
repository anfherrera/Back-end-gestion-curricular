package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.AnalisisCursosVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.Globales;

public interface GenerarReportesEstadisticasIntPort {
    byte[] pdfCursosVerano(AnalisisCursosVerano datos, String periodo, Integer programa);
    byte[] pdfCompleto(Globales estadisticas, AnalisisCursosVerano cursos, String periodo, Integer programa);
    byte[] excel(Globales estadisticas, AnalisisCursosVerano cursos, String periodo, Integer programa);
}
