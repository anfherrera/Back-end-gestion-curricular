package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocumentosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;

public class GestionarDocumentosCUAdapter implements GestionarDocumentosCUIntPort {

    private final GestionarDocumentosGatewayIntPort gestionarDocumentosGateway;
    private final FormateadorResultadosIntPort formateadorResultados;

    public GestionarDocumentosCUAdapter(GestionarDocumentosGatewayIntPort gestionarDocumentosGateway,
                                        FormateadorResultadosIntPort formateadorResultados) {
        this.gestionarDocumentosGateway = gestionarDocumentosGateway;
        this.formateadorResultados = formateadorResultados;
    }

    @Override
    public Documento buscarDocumentoId(Integer idDocumento) {
        if(idDocumento == null) {
            this.formateadorResultados.retornarRespuestaErrorEntidadExiste("El ID del documento debe ser un número positivo.");

        }

        Documento documento = gestionarDocumentosGateway.buscarDocumentoId(idDocumento);
        if (documento == null) {
            this.formateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró el documento con ID: " + idDocumento);
        }
        return documento;
    }

    @Override
    public void añadirComentario(Integer idDocumento, String comentario) {
        if(idDocumento == null || comentario == null ) {
            this.formateadorResultados.retornarRespuestaErrorEntidadExiste("El ID del documento y el comentario son obligatorios.");
        }
        Documento documento = gestionarDocumentosGateway.buscarDocumentoId(idDocumento);
        if (documento == null) {
            this.formateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró el documento con ID: " + idDocumento);
        }
        documento.setComentario(comentario); 
        //gestionarDocumentosGateway.añadirComentario(documento, comentario);
        gestionarDocumentosGateway.actualizarDocumento(documento);
    }

    
    
}
