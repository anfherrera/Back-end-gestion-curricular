package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;

public interface GestionarDocumentosCUIntPort {

    Documento buscarDocumentoId(Integer idDocumento);
    
}
