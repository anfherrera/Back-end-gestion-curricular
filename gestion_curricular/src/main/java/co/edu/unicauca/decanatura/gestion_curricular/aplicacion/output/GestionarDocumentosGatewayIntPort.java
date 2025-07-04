package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;

public interface GestionarDocumentosGatewayIntPort {
    
    Documento crearDocumento(Documento documento);

    Documento actualizarDocumento(Documento documento);

    Documento buscarDocumentoId(Integer idDocumento);

    boolean eliminarDocumento (Documento documento);
    
}
