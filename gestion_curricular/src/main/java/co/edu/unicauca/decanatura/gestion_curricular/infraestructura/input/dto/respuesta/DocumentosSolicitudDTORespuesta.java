package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Proyecciones HTTP de los documentos de Reingreso y Homologación. El orden de las claves es el que
 * producía el {@code HashMap} anterior (distinto del de {@link DocumentosPazYSalvoDTORespuesta}).
 */
public final class DocumentosSolicitudDTORespuesta {
    private DocumentosSolicitudDTORespuesta() {}

    /** Oficio o resolución de {@code /obtenerOficios/{idSolicitud}}; {@code id} es el de la solicitud. */
    @JsonPropertyOrder({"nombreArchivo", "ruta", "id", "nombre"})
    public record Oficio(Integer id, String nombre, String nombreArchivo, String ruta) {}

    /** Resultado de {@code /validarDocumentosRequeridos/{idSolicitud}}. */
    @JsonPropertyOrder({"documentosRequeridos", "totalDocumentos", "todosCompletos"})
    public record Revision(Map<String, Boolean> documentosRequeridos, boolean todosCompletos,
            int totalDocumentos) {}

    /** Respuesta 200 de la subida de una resolución de Homologación. */
    @JsonPropertyOrder({"idDocumento", "success", "idSolicitud", "message", "nombre"})
    public record ResolucionSubida(boolean success, String message, Integer idDocumento, String nombre,
            Integer idSolicitud) {}
}
