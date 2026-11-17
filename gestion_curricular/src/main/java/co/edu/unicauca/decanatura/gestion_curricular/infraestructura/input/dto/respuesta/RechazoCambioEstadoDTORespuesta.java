package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Cuerpo 400/403 de un cambio de estado rechazado por {@code ValidadorCambioEstado} o por el caso de uso
 * de Paz y Salvo. {@code estadoActual} y {@code error} se escriben aunque sean null.
 * El orden de las claves es el que producía el {@code HashMap} anterior.
 */
@JsonPropertyOrder({"estadoActual", "codigo", "error"})
public record RechazoCambioEstadoDTORespuesta(String error, String codigo, String estadoActual) {
}
