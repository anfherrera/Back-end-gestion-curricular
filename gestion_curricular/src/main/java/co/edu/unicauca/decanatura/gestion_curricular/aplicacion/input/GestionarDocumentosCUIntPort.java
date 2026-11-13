package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.ArchivoSubido;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;

public interface GestionarDocumentosCUIntPort {

    Documento buscarDocumentoId(Integer idDocumento);

    /** Registra un documento ya guardado en disco (con o sin solicitud asociada). */
    Documento registrarDocumento(Documento documento);

    /** Guarda un PDF sin solicitud y registra su autor y nombre original. */
    Documento subirDocumento(ArchivoSubido archivo, Integer idUsuario);

    /** Guarda y registra un PDF; conserva la política de nombre de cada operación. */
    Documento adjuntarASolicitud(ArchivoSubido archivo, Solicitud solicitud, String carpeta,
            String comentario, boolean usarNombreGuardado);

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
