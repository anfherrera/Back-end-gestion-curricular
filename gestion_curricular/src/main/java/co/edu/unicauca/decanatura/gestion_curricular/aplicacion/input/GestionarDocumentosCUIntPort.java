package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;

public interface GestionarDocumentosCUIntPort {

    Documento buscarDocumentoId(Integer idDocumento);

    /** Registra un documento ya guardado en disco (con o sin solicitud asociada). */
    Documento registrarDocumento(Documento documento);

    /**
     * Añade un comentario al documento concatenándolo al existente (no reemplaza).
     * @param idDocumento ID del documento
     * @param comentario texto nuevo a añadir (solo este fragmento)
     * @param nombreUsuario opcional, para prefijar "Rol - Nombre:"
     * @param rolUsuario opcional, para prefijar "Rol - Nombre:"
     */
    void añadirComentario(Integer idDocumento, String comentario, String nombreUsuario, String rolUsuario);

    /** Delegación a {@link #añadirComentario(Integer, String, String, String)} con usuario/rol null. */
    default void añadirComentario(Integer idDocumento, String comentario) {
        añadirComentario(idDocumento, comentario, null, null);
    }

    /**
     * Asocia a la solicitud los documentos que su dueño subió antes de enviarla y los mueve a
     * la carpeta de la solicitud.
     *
     * @param carpeta carpeta del tipo de solicitud (p. ej. "pazysalvo", "curso-verano")
     * @return cantidad de documentos asociados
     */
    int asociarDocumentosPendientes(Solicitud solicitud, Integer idUsuario, String carpeta);
}
