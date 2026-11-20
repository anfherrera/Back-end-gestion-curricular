package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta;

import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.ResumenSolicitudes;

/**
 * Respuestas que arma EstadisticasRestController (el resto de sus endpoints devuelve tal cual los resultados del
 * caso de uso; ver ARQ-13). El orden de las claves es el que producía el {@code HashMap} anterior.
 */
public final class EstadisticasFiltradasDTORespuesta {
    private EstadisticasFiltradasDTORespuesta() {}

    /**
     * {@code /filtradas}: los nueve campos comunes de las estadísticas de la consulta elegida (por proceso, programa,
     * estado o periodo, o las globales sin filtros).
     */
    @JsonPropertyOrder({"fechaConsulta", "porTipoProceso", "totalEnProceso", "totalSolicitudes", "totalRechazadas",
        "porcentajeAprobacion", "porEstado", "porPrograma", "totalAprobadas"})
    public record Filtradas(
            Date fechaConsulta,
            int totalSolicitudes,
            int totalAprobadas,
            int totalRechazadas,
            int totalEnProceso,
            double porcentajeAprobacion,
            Map<String, Integer> porTipoProceso,
            Map<String, Integer> porPrograma,
            Map<String, Integer> porEstado) {

        /** Copia los nueve campos de las estadísticas de la consulta. */
        public static Filtradas de(ResumenSolicitudes estadisticas) {
            return new Filtradas(estadisticas.fechaConsulta(), estadisticas.totalSolicitudes(),
                estadisticas.totalAprobadas(), estadisticas.totalRechazadas(), estadisticas.totalEnProceso(),
                estadisticas.porcentajeAprobacion(), estadisticas.porTipoProceso(), estadisticas.porPrograma(),
                estadisticas.porEstado());
        }
    }

    /** Rechazo 400 de {@code /periodo} sin período ni par de fechas. */
    public record ErrorPeriodo(String error) {}
}
