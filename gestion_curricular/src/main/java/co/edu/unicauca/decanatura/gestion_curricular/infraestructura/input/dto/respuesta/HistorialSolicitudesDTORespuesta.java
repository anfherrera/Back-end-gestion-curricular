package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Cuerpo de {@code GET /api/solicitudes/historial}: las solicitudes filtradas y los totales del sistema.
 * El orden de las claves es el que producía el {@code HashMap} anterior.
 */
@JsonPropertyOrder({"total", "total_solicitudes_procesadas", "total_solicitudes_sistema",
    "total_solicitudes_no_procesadas", "solicitudes"})
public record HistorialSolicitudesDTORespuesta(
        List<SolicitudDTORespuesta> solicitudes,
        int total,
        @JsonProperty("total_solicitudes_sistema") int totalSistema,
        @JsonProperty("total_solicitudes_procesadas") int totalProcesadas,
        @JsonProperty("total_solicitudes_no_procesadas") int totalNoProcesadas) {
}
