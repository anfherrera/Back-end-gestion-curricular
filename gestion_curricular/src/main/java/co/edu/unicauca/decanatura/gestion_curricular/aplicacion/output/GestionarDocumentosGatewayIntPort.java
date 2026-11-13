package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;

public interface GestionarDocumentosGatewayIntPort {
    /** Datos mínimos de acceso, sin cargar el grafo completo de una solicitud. */
    record AccesoDocumento(Integer idUsuarioSubio, Integer idDuenoSolicitud, String ruta, String nombre) {}

    java.util.Optional<AccesoDocumento> accesoDocumento(Integer idDocumento);

    /** Conserva el orden de la consulta por nombre. */
    List<AccesoDocumento> accesosPorNombre(String nombre);
    
    Documento crearDocumento(Documento documento);

    Documento actualizarDocumento(Documento documento);

    Documento buscarDocumentoId(Integer idDocumento);

    /** Documentos sin solicitud que subió el usuario indicado. Con idUsuario null devuelve una lista vacía. */
    List<Documento> buscarDocumentosSinSolicitud(Integer idUsuario);
}
