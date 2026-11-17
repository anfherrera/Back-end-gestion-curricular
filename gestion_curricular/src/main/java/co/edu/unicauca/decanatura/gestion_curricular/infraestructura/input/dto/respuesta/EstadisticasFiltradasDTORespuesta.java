package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Respuestas que arma EstadisticasRestController (el resto de sus endpoints devuelve tal cual los mapas del caso de
 * uso; ver ARQ-13). El orden de las claves es el que producía el {@code HashMap} anterior.
 */
public final class EstadisticasFiltradasDTORespuesta {
    private EstadisticasFiltradasDTORespuesta() {}

    /**
     * {@code /filtradas}: nueve claves copiadas del mapa del caso de uso, que se escriben siempre (null si el mapa
     * no las trae). Sus valores mantienen el tipo que puso el dominio, que varía según la consulta elegida.
     */
    @JsonPropertyOrder({"fechaConsulta", "porTipoProceso", "totalEnProceso", "totalSolicitudes", "totalRechazadas",
        "porcentajeAprobacion", "porEstado", "porPrograma", "totalAprobadas"})
    public record Filtradas(
            Object fechaConsulta,
            Object totalSolicitudes,
            Object totalAprobadas,
            Object totalRechazadas,
            Object totalEnProceso,
            Object porcentajeAprobacion,
            Object porTipoProceso,
            Object porPrograma,
            Object porEstado) {

        /** Copia las nueve claves del mapa del caso de uso. */
        public static Filtradas de(Map<String, ?> estadisticas) {
            return new Filtradas(estadisticas.get("fechaConsulta"), estadisticas.get("totalSolicitudes"),
                estadisticas.get("totalAprobadas"), estadisticas.get("totalRechazadas"),
                estadisticas.get("totalEnProceso"), estadisticas.get("porcentajeAprobacion"),
                estadisticas.get("porTipoProceso"), estadisticas.get("porPrograma"), estadisticas.get("porEstado"));
        }
    }

    /** Rechazo 400 de {@code /periodo} sin período ni par de fechas. */
    public record ErrorPeriodo(String error) {}
}
