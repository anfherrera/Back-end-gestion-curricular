package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;

public interface GestionarDocumentosCUIntPort {

    Documento buscarDocumentoId(Integer idDocumento);

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
}
