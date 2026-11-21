package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Respuestas de los cursos de {@code /api/cursos-intersemestrales/cursos-verano} (CursosVeranoRestController).
 * El orden de las claves es el que producía el {@code HashMap} anterior.
 */
public final class CursosVeranoDTORespuesta {
    private CursosVeranoDTORespuesta() {}

    /** Salón resuelto de un curso: {@code salonInfo} del listado y {@code salon} del detalle y la edición. */
    @JsonPropertyOrder({"id_salon", "descripcion", "numero_salon", "edificio", "activo"})
    public record SalonInfo(
            @JsonProperty("id_salon") Integer idSalon,
            @JsonProperty("numero_salon") String numeroSalon,
            String edificio,
            Boolean activo,
            String descripcion) {}

    /** Respuesta 200 de la creación; fechas y período son los recibidos, no los calculados. */
    @JsonPropertyOrder({"descripcion", "objDocente", "cupo_maximo", "estado", "fecha_inicio", "periodo",
        "codigo_curso", "cupo_estimado", "id_curso", "message", "nombre_curso", "espacio_asignado", "objMateria",
        "fecha_fin", "cupo_disponible"})
    public record CursoCreado(
            @JsonProperty("id_curso") Integer idCurso,
            @JsonProperty("nombre_curso") String nombreCurso,
            @JsonProperty("codigo_curso") String codigoCurso,
            String descripcion,
            @JsonProperty("fecha_inicio") String fechaInicio,
            @JsonProperty("fecha_fin") String fechaFin,
            String periodo,
            @JsonProperty("cupo_maximo") Integer cupoMaximo,
            @JsonProperty("cupo_disponible") Integer cupoDisponible,
            @JsonProperty("cupo_estimado") Integer cupoEstimado,
            @JsonProperty("espacio_asignado") String espacioAsignado,
            String estado,
            MateriaDTORespuesta objMateria,
            DocenteDTORespuesta objDocente,
            String message) {}

    /** Respuesta 200 de la edición. */
    @JsonPropertyOrder({"id_salon", "nombre_curso", "espacio_asignado", "debug_info", "estado", "codigo_curso",
        "cupo_estimado", "id_curso", "message", "salon"})
    public record CursoActualizado(
            @JsonProperty("id_curso") Integer idCurso,
            @JsonProperty("nombre_curso") String nombreCurso,
            @JsonProperty("codigo_curso") String codigoCurso,
            @JsonProperty("cupo_estimado") Integer cupoEstimado,
            @JsonProperty("espacio_asignado") String espacioAsignado,
            SalonInfo salon,
            @JsonProperty("id_salon") Integer idSalon,
            String estado,
            String message,
            @JsonProperty("debug_info") String debugInfo) {}

    /** Respuesta 200 del borrado. */
    @JsonPropertyOrder({"nombre_curso", "id_curso_eliminado", "message"})
    public record CursoEliminado(
            String message,
            @JsonProperty("id_curso_eliminado") Long idCursoEliminado,
            @JsonProperty("nombre_curso") String nombreCurso) {}

    /** Detalle de {@code GET /cursos-verano/{id}}; repite el id en {@code id_curso} e {@code idCurso}. */
    @JsonPropertyOrder({"descripcion", "objDocente", "cupo_maximo", "estado", "fecha_inicio", "codigo_curso",
        "cupo_estimado", "id_curso", "solicitudes", "id_salon", "nombre_curso", "espacio_asignado", "objMateria",
        "fecha_fin", "idCurso", "cupo_disponible", "salon"})
    public record Detalle(
            @JsonProperty("id_curso") Integer idCurso,
            @JsonProperty("idCurso") Integer idCursoCompatible,
            @JsonProperty("nombre_curso") String nombreCurso,
            @JsonProperty("codigo_curso") String codigoCurso,
            String descripcion,
            @JsonProperty("fecha_inicio") String fechaInicio,
            @JsonProperty("fecha_fin") String fechaFin,
            @JsonProperty("cupo_maximo") Integer cupoMaximo,
            @JsonProperty("cupo_disponible") Integer cupoDisponible,
            @JsonProperty("cupo_estimado") Integer cupoEstimado,
            @JsonProperty("espacio_asignado") String espacioAsignado,
            String estado,
            SalonInfo salon,
            @JsonProperty("id_salon") Integer idSalon,
            int solicitudes,
            MateriaDTORespuesta objMateria,
            DocenteDTORespuesta objDocente) {}

    /** Rechazo 400/500 de la edición o el borrado con {@code error} y {@code message}. */
    @JsonPropertyOrder({"error", "message"})
    public record ErrorCurso(String error, String message) {}

    /** Rechazo 400 de una transición de estado no permitida en la edición. */
    @JsonPropertyOrder({"valido", "error", "message"})
    public record TransicionRechazada(boolean valido, String error, String message) {}

    /** Rechazo 400 del borrado de un curso con estudiantes inscritos. */
    @JsonPropertyOrder({"estudiantes_inscritos", "error", "message"})
    public record BorradoBloqueado(
            @JsonProperty("estudiantes_inscritos") int estudiantesInscritos,
            String error,
            String message) {}
}
