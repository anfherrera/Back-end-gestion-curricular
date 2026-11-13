package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Proyecciones HTTP de las consultas de documentos de Paz y Salvo. */
public final class DocumentosPazYSalvoDTORespuesta {
    private DocumentosPazYSalvoDTORespuesta() {}

    public record DocumentoConTipo(Integer id, String nombre, String nombreArchivo,
            String ruta, Date fecha, boolean esValido, String comentario, String tipo) {}

    public record Oficio(Integer id, String nombre, String nombreArchivo, String ruta) {}

    public record Comentario(Integer id, String nombre, String comentario) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Comentarios(String comentarioRechazo, List<Comentario> documentosConComentarios,
            String mensaje) {}

    public record Revision(Map<String, Boolean> documentosRequeridos, boolean todosCompletos,
            int totalDocumentos) {}
}
