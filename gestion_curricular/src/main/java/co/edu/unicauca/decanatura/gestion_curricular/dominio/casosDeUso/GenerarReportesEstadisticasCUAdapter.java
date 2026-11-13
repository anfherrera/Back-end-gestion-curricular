package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.Date;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GenerarReportesEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GenerarReportesEstadisticasIntPort;

/** Reúne los datos del reporte antes de invocar el formateador de salida. */
public class GenerarReportesEstadisticasCUAdapter implements GenerarReportesEstadisticasCUIntPort {
    private final GestionarEstadisticasCUIntPort estadisticas;
    private final GenerarReportesEstadisticasIntPort generador;

    public GenerarReportesEstadisticasCUAdapter(GestionarEstadisticasCUIntPort estadisticas,
            GenerarReportesEstadisticasIntPort generador) {
        this.estadisticas = estadisticas;
        this.generador = generador;
    }

    @Override public byte[] pdfGeneral() {
        return generador.pdfGeneral(estadisticas.obtenerEstadisticasGlobales());
    }
    @Override public byte[] pdfCursosVerano(String periodo, Integer programa) {
        return generador.pdfCursosVerano(estadisticas.obtenerEstadisticasCursosVerano(periodo, programa), periodo, programa);
    }
    @Override public byte[] pdfCompleto(String proceso, Integer programa, String periodo, Date inicio, Date fin) {
        var datos = estadisticas.prepararReporte(proceso, programa, periodo, inicio, fin);
        return generador.pdfCompleto(datos.estadisticas(), datos.cursosVerano(), periodo, programa);
    }
    @Override public byte[] excelGeneral() {
        return generador.excel(estadisticas.obtenerEstadisticasGlobales(), null, null, null);
    }
    @Override public byte[] excelCursosVerano(String periodo, Integer programa) {
        return generador.excel(null, estadisticas.obtenerEstadisticasCursosVerano(periodo, programa), periodo, programa);
    }
    @Override public byte[] excelCompleto(String proceso, Integer programa, String periodo, Date inicio, Date fin) {
        var datos = estadisticas.prepararReporte(proceso, programa, periodo, inicio, fin);
        return generador.excel(datos.estadisticas(), datos.cursosVerano(), periodo, programa);
    }
}
