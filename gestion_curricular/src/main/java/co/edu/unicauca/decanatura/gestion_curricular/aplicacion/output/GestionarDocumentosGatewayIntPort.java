package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;

public interface GestionarDocumentosGatewayIntPort {
    
    Documento crearDocumento(Documento documento);

    Documento actualizarDocumento(Documento documento);

    Documento buscarDocumentoId(Integer idDocumento);

    boolean eliminarDocumento (Documento documento);

    List<Documento> buscarDocumentosSinSolicitud();

    void añadirComentario(Documento documento, String comentario);

    Optional<Documento> buscarDocumentoPorNombre(String nombre);
}
