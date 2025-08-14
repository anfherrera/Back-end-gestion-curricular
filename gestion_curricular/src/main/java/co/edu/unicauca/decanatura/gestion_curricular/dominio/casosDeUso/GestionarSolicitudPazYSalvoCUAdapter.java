package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudPazYSalvoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudPazYSalvoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.TipoDocumentoSolicitudPazYSalvo;

public class GestionarSolicitudPazYSalvoCUAdapter implements GestionarSolicitudPazYSalvoCUIntPort {


    private final GestionarSolicitudPazYSalvoGatewayIntPort objGestionarSolicitudGateway;
    private final GestionarUsuarioGatewayIntPort objUsuario;
    private final GestionarDocumentosGatewayIntPort objDocumentosGateway;
    private final FormateadorResultadosIntPort objFormateadorResultados;
    
    
    public GestionarSolicitudPazYSalvoCUAdapter(GestionarSolicitudPazYSalvoGatewayIntPort objGestionarSolicitudGateway,
    GestionarUsuarioGatewayIntPort objUsuario, 
    GestionarDocumentosGatewayIntPort objDocumentosGateway,
    FormateadorResultadosIntPort objFormateadorResultados){
        this.objUsuario = objUsuario;
        this.objFormateadorResultados = objFormateadorResultados;
        this.objDocumentosGateway = objDocumentosGateway;
        this.objGestionarSolicitudGateway = objGestionarSolicitudGateway;
    }

    @Override
    public SolicitudPazYSalvo crearSolicitudPazYSalvo(SolicitudPazYSalvo solicitudPazYSalvo) {
        if (solicitudPazYSalvo == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud no puede ser nula");
        }

        if (solicitudPazYSalvo.getObjUsuario() == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El usuario no puede ser nulo");
        }
        if(solicitudPazYSalvo.getObjUsuario().getId_usuario() == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El usuario no puede ser nulo");

        }
        Usuario usuarioBuscar = this.objUsuario.obtenerUsuarioPorId(solicitudPazYSalvo.getObjUsuario().getId_usuario());
        if (usuarioBuscar == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Usuario no encontrado");
        }

        List<Documento> documentos = solicitudPazYSalvo.getDocumentos();
        if (documentos == null || documentos.isEmpty() || documentos.size() > 6) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Se deben adjuntar entre 1 y 6 documentos");
        }

        boolean contienePP_H = false;
        boolean contieneTI_G = false;

        for (Documento doc : documentos) {
            if(doc.getTipoDocumentoSolicitudPazYSalvo() == null){
                this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("No hay un tipo de documento");
            }
            String tipo = doc.getTipoDocumentoSolicitudPazYSalvo().name();
            if (tipo.equals(TipoDocumentoSolicitudPazYSalvo.formato_PP_H.name())) {
                contienePP_H = true;
            }
            if (tipo.equals(TipoDocumentoSolicitudPazYSalvo.formato_TI_G.name())) {
                contieneTI_G = true;
            }

        }

        if (!contienePP_H && !contieneTI_G) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Se debe ingresar al menos uno de los dos formatos: PP_H o TI_G");
        }

        if (contienePP_H && contieneTI_G) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Se ingresaron ambos formatos. Solo se debe adjuntar uno");
        }

        // Crear la solicitud
        SolicitudPazYSalvo solicitudGuardada = this.objGestionarSolicitudGateway.crearSolicitudPazYSalvo(solicitudPazYSalvo);

        // Asociar y guardar los documentos
        for (Documento doc : solicitudGuardada.getDocumentos()) {
            doc.setObjSolicitud(solicitudGuardada);
            this.objDocumentosGateway.actualizarDocumento(doc);
        }

        // Asociar solicitud al usuario
        usuarioBuscar.getSolicitudes().add(solicitudGuardada);
        this.objUsuario.actualizarUsuario(usuarioBuscar);

        return solicitudGuardada;
    }
}
