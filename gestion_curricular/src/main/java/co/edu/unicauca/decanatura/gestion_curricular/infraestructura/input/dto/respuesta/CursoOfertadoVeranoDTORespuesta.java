package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;

/**
 * Respuestas de {@code /api/cursos-verano} (CursoOfertadoRestController). El orden de las claves es el que
 * producía el {@code HashMap} anterior; las solicitudes se siguen serializando como modelos de dominio.
 */
public final class CursoOfertadoVeranoDTORespuesta {
    private CursoOfertadoVeranoDTORespuesta() {}

    /** Respuesta de {@code /preinscripciones/{idCurso}}. */
    @JsonPropertyOrder({"preinscripciones", "total", "idCurso"})
    public record PreinscripcionesCurso(Integer idCurso, List<SolicitudCursoVeranoPreinscripcion> preinscripciones,
            int total) {}

    /** Respuesta de {@code /inscripciones/{idCurso}}. */
    @JsonPropertyOrder({"total", "inscripciones", "idCurso"})
    public record InscripcionesCurso(Integer idCurso, List<SolicitudCursoVeranoInscripcion> inscripciones,
            int total) {}

    /** Aprobación de una preinscripción, validación del pago o cierre de una inscripción. */
    @JsonPropertyOrder({"success", "solicitud", "message"})
    public record Decision<T>(boolean success, String message, T solicitud) {}

    /** Rechazo de una preinscripción; {@code motivo} es el recibido sin espacios en los extremos. */
    @JsonPropertyOrder({"motivo", "success", "solicitud", "message"})
    public record Rechazo(boolean success, String message, SolicitudCursoVeranoPreinscripcion solicitud,
            String motivo) {}
}
