package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.CondicionSolicitudVerano;

/**
 * Respuestas de InscripcionesVeranoRestController. El orden de las claves es el que producía el
 * {@code HashMap} anterior.
 */
public final class InscripcionesVeranoDTORespuesta {
    private InscripcionesVeranoDTORespuesta() {}

    /** Rechazo 400 de la creación de una inscripción sin preinscripción aprobada. */
    @JsonPropertyOrder({"codigo", "error"})
    public record InscripcionRechazada(String error, String codigo) {}

    /** Error con solo {@code error}: 400 o 500 de la creación, la aceptación y el rechazo. */
    public record ErrorInscripcion(String error) {}

    /**
     * Elemento de {@code /inscripciones/estudiantes-elegibles/{idCurso}}. {@code id_curso} y {@code nombre_curso}
     * aparecen solo si la inscripción tiene curso, aunque valgan null; por eso van desenvueltos desde records de un
     * solo campo, que son null cuando la clave falta. En el orden, las propiedades desenvueltas se nombran por su
     * componente ({@code idCurso}, {@code nombreCurso}) y no por la clave que escriben.
     */
    @JsonPropertyOrder({"nombre_completo", "codigo", "tiene_inscripcion_formal", "id_solicitud", "idCurso",
        "tipo_solicitud", "condicion_solicitud", "nombreCurso", "id_usuario", "estado_actual", "correo",
        "motivo_inclusion", "archivoPago", "fecha_solicitud", "nombre_solicitud"})
    public record EstudianteElegible(
            @JsonProperty("id_usuario") Integer idUsuario,
            @JsonProperty("nombre_completo") String nombreCompleto,
            String codigo,
            String correo,
            @JsonProperty("id_solicitud") Integer idSolicitud,
            @JsonProperty("nombre_solicitud") String nombreSolicitud,
            @JsonProperty("fecha_solicitud") Date fechaSolicitud,
            @JsonProperty("condicion_solicitud") CondicionSolicitudVerano condicionSolicitud,
            @JsonUnwrapped IdCurso idCurso,
            @JsonUnwrapped NombreCurso nombreCurso,
            @JsonProperty("estado_actual") String estadoActual,
            @JsonProperty("tipo_solicitud") String tipoSolicitud,
            @JsonProperty("motivo_inclusion") String motivoInclusion,
            @JsonProperty("tiene_inscripcion_formal") boolean tieneInscripcionFormal,
            ComprobantePago archivoPago) {}

    /** Clave {@code id_curso} de {@link EstudianteElegible}. */
    public record IdCurso(@JsonProperty("id_curso") Integer idCurso) {}

    /** Clave {@code nombre_curso} de {@link EstudianteElegible}. */
    public record NombreCurso(@JsonProperty("nombre_curso") String nombreCurso) {}

    /**
     * {@code archivoPago} de {@link EstudianteElegible}: el primer documento de la inscripción ({@code url} es su
     * ruta). Es null si la inscripción no tiene documentos.
     */
    @JsonPropertyOrder({"id_documento", "fecha", "nombre", "url"})
    public record ComprobantePago(
            @JsonProperty("id_documento") Integer idDocumento,
            String nombre,
            String url,
            Date fecha) {}

    /** Respuesta 200 de la aceptación. */
    @JsonPropertyOrder({"fecha_aceptacion", "estudiante_nombre", "success", "observaciones", "id_inscripcion",
        "message", "curso_nombre"})
    public record InscripcionAceptada(
            boolean success,
            String message,
            @JsonProperty("id_inscripcion") Integer idInscripcion,
            @JsonProperty("estudiante_nombre") String estudianteNombre,
            @JsonProperty("curso_nombre") String cursoNombre,
            @JsonProperty("fecha_aceptacion") Date fechaAceptacion,
            String observaciones) {}

    /** Respuesta 200 del rechazo. */
    @JsonPropertyOrder({"motivo", "estudiante_nombre", "success", "nuevo_estado", "id_inscripcion", "message",
        "curso_nombre", "fecha_rechazo"})
    public record InscripcionRechazadaPorFuncionario(
            boolean success,
            String message,
            @JsonProperty("id_inscripcion") Integer idInscripcion,
            @JsonProperty("estudiante_nombre") String estudianteNombre,
            @JsonProperty("curso_nombre") String cursoNombre,
            @JsonProperty("fecha_rechazo") Date fechaRechazo,
            String motivo,
            @JsonProperty("nuevo_estado") String nuevoEstado) {}
}
