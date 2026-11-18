package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
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

    /** Rechazo 404 o 400 de la cancelación; {@code timestamp} es {@code LocalDateTime.now().toString()}. */
    @JsonPropertyOrder({"error", "message", "status", "timestamp"})
    public record CancelacionRechazada(String error, String message, int status, String timestamp) {}

    /** Respuesta 200 de la cancelación. */
    @JsonPropertyOrder({"id_inscripcion", "mensaje", "status", "timestamp"})
    public record InscripcionCancelada(
            String mensaje,
            @JsonProperty("id_inscripcion") Integer idInscripcion,
            int status,
            String timestamp) {}

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
            PagoSupuesto archivoPago) {}

    /** Clave {@code id_curso} de {@link EstudianteElegible}. */
    public record IdCurso(@JsonProperty("id_curso") Integer idCurso) {}

    /** Clave {@code nombre_curso} de {@link EstudianteElegible}. */
    public record NombreCurso(@JsonProperty("nombre_curso") String nombreCurso) {}

    /** {@code archivoPago} fijo de {@link EstudianteElegible}: se da por validado sin consultar documentos. */
    @JsonPropertyOrder({"id_documento", "fecha", "nombre", "url"})
    public record PagoSupuesto(
            @JsonProperty("id_documento") String idDocumento,
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

    /** Respuesta fija de {@code /inscripciones/{id}/confirmar}: solo {@code id} sale de la petición. */
    @JsonPropertyOrder({"inscripcion", "message"})
    public record ConfirmacionSimulada(String message, InscripcionSimulada inscripcion) {}

    @JsonPropertyOrder({"estudiante", "fecha", "estado", "estudianteId", "id", "cursoId", "archivoPago"})
    public record InscripcionSimulada(
            Long id,
            String fecha,
            String estado,
            int estudianteId,
            int cursoId,
            EstudianteSimulado estudiante,
            ArchivoSimulado archivoPago) {}

    @JsonPropertyOrder({"codigo_estudiante", "id_usuario", "apellido", "nombre", "email"})
    public record EstudianteSimulado(
            @JsonProperty("id_usuario") int idUsuario,
            String nombre,
            String apellido,
            String email,
            @JsonProperty("codigo_estudiante") String codigoEstudiante) {}

    @JsonPropertyOrder({"id_documento", "fecha", "nombre", "url"})
    public record ArchivoSimulado(
            @JsonProperty("id_documento") int idDocumento,
            String nombre,
            String url,
            String fecha) {}

    /** Respuesta de {@code /inscripciones/{id}/info} cuando la inscripción no existe. */
    @JsonPropertyOrder({"encontrada", "mensaje"})
    public record InfoNoEncontrada(boolean encontrada, String mensaje) {}

    /**
     * Respuesta de {@code /inscripciones/{id}/info}. {@code documentos_count} es el número de documentos (0 si la
     * lista es null; {@code documentos_null} lo distingue). {@code documentos} y {@code archivo_pago} se omiten si
     * no hay documentos.
     */
    @JsonPropertyOrder({"encontrada", "documentos_null", "documentos", "archivo_pago", "fecha_registro",
        "id_solicitud", "documentos_count", "documentos_empty", "nombre_solicitud"})
    public record InfoInscripcion(
            boolean encontrada,
            @JsonProperty("id_solicitud") Integer idSolicitud,
            @JsonProperty("nombre_solicitud") String nombreSolicitud,
            @JsonProperty("fecha_registro") Date fechaRegistro,
            @JsonProperty("documentos_count") int documentosCount,
            @JsonProperty("documentos_null") boolean documentosNull,
            @JsonProperty("documentos_empty") boolean documentosEmpty,
            @JsonInclude(JsonInclude.Include.NON_NULL) List<DocumentoInfo> documentos,
            @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty("archivo_pago") ArchivoPagoInfo archivoPago) {}

    /** El primer documento de una inscripción, para descargarlo. Sus valores pueden ser null. */
    @JsonPropertyOrder({"nombre_archivo", "ruta_archivo", "id_documento"})
    public record ArchivoPagoInfo(
            @JsonProperty("nombre_archivo") String nombreArchivo,
            @JsonProperty("ruta_archivo") String rutaArchivo,
            @JsonProperty("id_documento") Integer idDocumento) {}

    @JsonPropertyOrder({"ruta", "es_valido", "id", "nombre"})
    public record DocumentoInfo(Integer id, String nombre, String ruta, @JsonProperty("es_valido") boolean esValido) {}

    /** Respuesta de {@code /inscripciones/curso/{idCurso}/estadisticas}. */
    @JsonPropertyOrder({"curso_id", "pendientes_revision", "aceptadas", "fecha_consulta", "total_inscripciones",
        "rechazadas"})
    public record EstadisticasInscripciones(
            @JsonProperty("total_inscripciones") Integer totalInscripciones,
            @JsonProperty("pendientes_revision") Integer pendientesRevision,
            Integer aceptadas,
            Integer rechazadas,
            @JsonProperty("curso_id") Long cursoId,
            @JsonProperty("fecha_consulta") Date fechaConsulta) {}
}
