package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import lombok.extern.slf4j.Slf4j;

/**
 * Asocia a una solicitud recién creada los documentos que su dueño subió antes de enviarla.
 *
 * El estudiante sube los archivos primero (quedan sin solicitud y en la raíz) y luego envía el
 * formulario. Al crear la solicitud, sus documentos pendientes se mueven a la carpeta de la
 * solicitud ({@code <carpeta>/solicitud_<id>/}) y quedan asociados a ella. Solo se toman los
 * documentos que subió ese mismo usuario.
 */
@Slf4j
public class AsociadorDocumentosPendientes {

    private final GestionarDocumentosGatewayIntPort documentosGateway;
    private final GestionarArchivosCUIntPort archivos;

    public AsociadorDocumentosPendientes(GestionarDocumentosGatewayIntPort documentosGateway,
                                         GestionarArchivosCUIntPort archivos) {
        this.documentosGateway = documentosGateway;
        this.archivos = archivos;
    }

    /**
     * @param solicitud solicitud ya guardada (con id)
     * @param idUsuario dueño de la solicitud; solo se asocian los documentos que él subió
     * @param carpeta   carpeta del tipo de solicitud (p. ej. "homologacion", "pazysalvo")
     * @return cantidad de documentos asociados
     */
    public int asociar(Solicitud solicitud, Integer idUsuario, String carpeta) {
        List<Documento> pendientes = documentosGateway.buscarDocumentosSinSolicitud(idUsuario);
        for (Documento documento : pendientes) {
            moverACarpetaDeLaSolicitud(documento, carpeta, solicitud.getId_solicitud());
            // Se asocia siempre, aunque no se haya podido mover el archivo
            documento.setObjSolicitud(solicitud);
            documentosGateway.actualizarDocumento(documento);
        }
        return pendientes.size();
    }

    private void moverACarpetaDeLaSolicitud(Documento documento, String carpeta, Integer idSolicitud) {
        String rutaActual = documento.getRuta_documento() != null ? documento.getRuta_documento() : documento.getNombre();
        if (rutaActual == null || rutaActual.contains("/")) {
            return; // Ya está organizado
        }
        try {
            String nuevaRuta = archivos.moverArchivoAOrganizado(rutaActual, documento.getNombre(), carpeta, idSolicitud);
            if (nuevaRuta != null) {
                documento.setRuta_documento(nuevaRuta);
            }
        } catch (Exception e) {
            log.warn("No se pudo mover el documento {} a la carpeta de la solicitud {}; se asocia con su ruta actual. Error: {}",
                    documento.getNombre(), idSolicitud, e.getMessage());
        }
    }
}
