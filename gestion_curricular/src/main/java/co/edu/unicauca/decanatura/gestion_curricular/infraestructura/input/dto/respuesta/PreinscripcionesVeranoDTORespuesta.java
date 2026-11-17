package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * Respuestas de PreinscripcionesVeranoRestController. El orden de las claves es el que producía el
 * {@code HashMap} anterior.
 */
public final class PreinscripcionesVeranoDTORespuesta {
    private PreinscripcionesVeranoDTORespuesta() {}

    /**
     * Elemento de {@code /preinscripciones/usuario/{id}}. {@code comentarioEstado} solo aparece si no está en
     * blanco. {@code cursoId} aparece si hay curso y {@code cursoNombre} si además hay materia; en los dos casos se
     * escriben aunque valgan null, por eso van desenvueltos desde un record que es null cuando la clave falta.
     */
    @JsonPropertyOrder({"comentarioEstado", "accionesDisponibles", "fecha", "estado", "tipo", "estudianteId",
        "cursoNombre", "id", "cursoId"})
    public record PreinscripcionUsuario(
            Integer id,
            Date fecha,
            String estado,
            @JsonInclude(JsonInclude.Include.NON_NULL) String comentarioEstado,
            String tipo,
            Integer estudianteId,
            @JsonUnwrapped CursoId cursoId,
            @JsonUnwrapped CursoNombre cursoNombre,
            List<String> accionesDisponibles) {}

    /** Clave {@code cursoId} de {@link PreinscripcionUsuario}. */
    public record CursoId(Integer cursoId) {}

    /** Clave {@code cursoNombre} de {@link PreinscripcionUsuario}. */
    public record CursoNombre(String cursoNombre) {}

    /** Respuesta 200 de {@code /solicitudes-curso-nuevo}; {@code fecha} es el {@code Date.toString()} guardado. */
    @JsonPropertyOrder({"fecha", "codigo", "estado", "objUsuario", "id_solicitud", "curso", "nombreCompleto",
        "condicion"})
    public record CursoNuevoSolicitado(
            @JsonProperty("id_solicitud") Integer idSolicitud,
            String nombreCompleto,
            String codigo,
            String curso,
            String condicion,
            String fecha,
            String estado,
            UsuarioSolicitante objUsuario) {}

    @JsonPropertyOrder({"nombre_completo", "id_usuario"})
    public record UsuarioSolicitante(
            @JsonProperty("id_usuario") Integer idUsuario,
            @JsonProperty("nombre_completo") String nombreCompleto) {}

    /** Rechazo 400 de la creación de una preinscripción. */
    @JsonPropertyOrder({"codigo", "error"})
    public record PreinscripcionRechazada(String error, String codigo) {}

    /** Elemento de {@code /preinscripciones/curso/{idCurso}}; usuario y curso se omiten si faltan. */
    @JsonPropertyOrder({"estado", "objUsuario", "id_solicitud", "observaciones", "objCurso",
        "fecha_preinscripcion", "condicion", "id_preinscripcion"})
    public record PreinscripcionCurso(
            @JsonProperty("id_preinscripcion") Integer idPreinscripcion,
            @JsonProperty("id_solicitud") Integer idSolicitud,
            @JsonProperty("fecha_preinscripcion") Date fechaPreinscripcion,
            String estado,
            String observaciones,
            String condicion,
            @JsonInclude(JsonInclude.Include.NON_NULL) Estudiante objUsuario,
            @JsonInclude(JsonInclude.Include.NON_NULL) CursoPreinscrito objCurso) {}

    /** {@code objUsuario} de {@link PreinscripcionCurso}; {@code objRol} se omite si el usuario no tiene rol. */
    @JsonPropertyOrder({"nombre_completo", "codigo", "codigo_estudiante", "id_usuario", "correo", "objRol"})
    public record Estudiante(
            @JsonProperty("id_usuario") Integer idUsuario,
            @JsonProperty("nombre_completo") String nombreCompleto,
            String correo,
            String codigo,
            @JsonProperty("codigo_estudiante") String codigoEstudiante,
            @JsonInclude(JsonInclude.Include.NON_NULL) RolEstudiante objRol) {}

    @JsonPropertyOrder({"id_rol", "nombre"})
    public record RolEstudiante(@JsonProperty("id_rol") Integer idRol, String nombre) {}

    /** {@code objCurso} de {@link PreinscripcionCurso}. */
    @JsonPropertyOrder({"descripcion", "objDocente", "cupo_maximo", "estado", "fecha_inicio", "codigo_curso",
        "cupo_estimado", "id_curso", "nombre_curso", "espacio_asignado", "objMateria", "fecha_fin",
        "cupo_disponible"})
    public record CursoPreinscrito(
            @JsonProperty("id_curso") Integer idCurso,
            @JsonProperty("nombre_curso") String nombreCurso,
            @JsonProperty("codigo_curso") String codigoCurso,
            String descripcion,
            @JsonProperty("fecha_inicio") String fechaInicio,
            @JsonProperty("fecha_fin") String fechaFin,
            @JsonProperty("cupo_maximo") Integer cupoMaximo,
            @JsonProperty("cupo_estimado") Integer cupoEstimado,
            @JsonProperty("cupo_disponible") Integer cupoDisponible,
            @JsonProperty("espacio_asignado") String espacioAsignado,
            String estado,
            MateriaDTORespuesta objMateria,
            DocenteDTORespuesta objDocente) {}

    /** Rechazo 400 de la edición de observaciones. */
    public record ErrorObservaciones(String error) {}

    /** Respuesta 200 de la edición de observaciones. */
    @JsonPropertyOrder({"success", "preinscripcion", "message"})
    public record ObservacionesActualizadas(String message, boolean success, Observaciones preinscripcion) {}

    @JsonPropertyOrder({"estado", "observaciones", "id_preinscripcion"})
    public record Observaciones(
            @JsonProperty("id_preinscripcion") Integer idPreinscripcion,
            String observaciones,
            String estado) {}

    /** Respuesta 200 de la aprobación. */
    @JsonPropertyOrder({"success", "id_solicitud", "message"})
    public record Aprobacion(boolean success, String message, @JsonProperty("id_solicitud") Integer idSolicitud) {}

    /** Respuesta 200 del rechazo; {@code motivo} es el recibido sin espacios en los extremos. */
    @JsonPropertyOrder({"motivo", "success", "id_solicitud", "message"})
    public record Rechazo(boolean success, String message, @JsonProperty("id_solicitud") Integer idSolicitud,
            String motivo) {}
}
