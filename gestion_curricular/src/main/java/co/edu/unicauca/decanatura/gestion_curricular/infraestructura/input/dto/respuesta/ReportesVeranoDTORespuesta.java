package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Respuestas del dashboard y del seguimiento de cursos de verano. El orden de las claves es el que
 * producía el {@code HashMap} anterior.
 */
public final class ReportesVeranoDTORespuesta {
    private ReportesVeranoDTORespuesta() {}

    /** {@code GET /api/cursos-intersemestrales/dashboard/estadisticas}. */
    @JsonPropertyOrder({"cursosActivos", "totalCursos", "totalPreinscripciones", "cursosGestionados",
        "totalInscripciones", "porcentajeProgreso"})
    public record Dashboard(int cursosActivos, int totalPreinscripciones, int totalInscripciones,
            int cursosGestionados, int totalCursos, long porcentajeProgreso) {}

    /** {@code GET /api/cursos-intersemestrales/seguimiento/{idUsuario}}. */
    @JsonPropertyOrder({"preinscripciones", "totalPreinscripciones", "totalActividades", "inscripciones",
        "totalInscripciones"})
    public record Seguimiento(List<ActividadPreinscripcion> preinscripciones,
            List<ActividadInscripcion> inscripciones, int totalPreinscripciones, int totalInscripciones,
            int totalActividades) {}

    @JsonPropertyOrder({"accionesDisponibles", "fecha", "estado", "tipo", "estudianteId", "curso",
        "estadoCurso", "id", "cursoId"})
    public record ActividadPreinscripcion(Integer id, Date fecha, String estado, String tipo, String curso,
            Integer estudianteId, Integer cursoId, String estadoCurso, List<String> accionesDisponibles) {}

    /**
     * Fila de {@code GET /api/cursos-intersemestrales/solicitudes} y del Excel que exporta el mismo listado
     * ({@code ReporteSolicitudesVeranoMapper}).
     */
    @JsonPropertyOrder({"tipo", "codigo", "estado", "codigoMateria", "grupoCurso", "nombreCompleto",
        "motivoSolicitud", "condicion", "fecha", "idMateria", "curso", "idCurso", "id"})
    public record SolicitudReporte(Integer id, Date fecha, String tipo, String motivoSolicitud,
            String nombreCompleto, String codigo, String estado, String condicion, String curso, Integer idMateria,
            Integer idCurso, String codigoMateria, String grupoCurso) {}

    @JsonPropertyOrder({"accionesDisponibles", "fecha", "estado", "estudianteId", "tipoSolicitud", "curso",
        "nombreSolicitud", "estadoCurso", "id", "cursoId"})
    public record ActividadInscripcion(Integer id, Date fecha, String estado, String tipoSolicitud,
            String nombreSolicitud, String curso, Integer cursoId, Integer estudianteId, String estadoCurso,
            List<String> accionesDisponibles) {}
}
