package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Respuesta de {@code POST /api/archivos/subir/pdf}. Los identificadores recibidos solo aparecen
 * si llegaron con texto. El orden de las claves es el que producía el {@code HashMap} anterior.
 */
@JsonPropertyOrder({"tipo", "guardadoEnBD", "solicitudId", "tamaño", "fechaSubida",
    "inscripcionId", "ruta", "tipoSolicitud", "id", "nombre"})
public record PdfSubidoDTORespuesta(
        long id,
        String nombre,
        String ruta,
        @JsonProperty("tamaño") long tamano,
        String tipo,
        String fechaSubida,
        @JsonProperty("guardadoEnBD") boolean guardadoEnBD,
        @JsonInclude(JsonInclude.Include.NON_NULL) String inscripcionId,
        @JsonInclude(JsonInclude.Include.NON_NULL) String solicitudId,
        @JsonInclude(JsonInclude.Include.NON_NULL) String tipoSolicitud) {

    /** Cuerpo {@code {error}} de los rechazos del endpoint. */
    public record Error(String error) {
    }
}
