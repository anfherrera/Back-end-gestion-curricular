package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;

public interface GestionarDocumentosGatewayIntPort {
    
    Documento crearDocumento(Documento documento);

    Documento actualizarDocumento(Documento documento);

    Documento buscarDocumentoId(Integer idDocumento);

    boolean eliminarDocumento (Documento documento);

    List<Documento> buscarDocumentoSinSolicitud();

    void añadirComentario(Documento documento, String comentario); 
}
