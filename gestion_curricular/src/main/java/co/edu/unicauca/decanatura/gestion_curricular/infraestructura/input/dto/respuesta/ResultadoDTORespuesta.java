package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Sobre {@code {data, success, message}} de los catálogos de períodos y tipos de documento.
 * {@code data} se escribe siempre, también cuando es null. El orden de las claves es el que
 * producía el {@code HashMap} anterior.
 */
@JsonPropertyOrder({"data", "success", "message"})
public record ResultadoDTORespuesta<T>(T data, boolean success, String message) {

    public static <T> ResultadoDTORespuesta<T> exito(T data, String message) {
        return new ResultadoDTORespuesta<>(data, true, message);
    }

    /** Respuesta de fallo, sin la clave {@code data}. */
    @JsonPropertyOrder({"success", "message"})
    public record Fallo(boolean success, String message) {
        public Fallo(String message) {
            this(false, message);
        }
    }

    @JsonPropertyOrder({"descripcion", "codigo"})
    public record TipoDocumentoDetalle(String codigo, String descripcion) {
    }

    @JsonPropertyOrder({"descripcion", "numeroPeriodo", "valor", "año"})
    public record PeriodoDetalle(String valor, int año, int numeroPeriodo, String descripcion) {
    }
}
