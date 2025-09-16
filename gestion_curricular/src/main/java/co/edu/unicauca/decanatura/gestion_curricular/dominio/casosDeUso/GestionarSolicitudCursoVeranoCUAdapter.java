package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarCursoOfertadoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudCursoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

public class GestionarSolicitudCursoVeranoCUAdapter implements GestionarSolicitudCursoVeranoCUIntPort {

    private final GestionarSolicitudCursoVeranoGatewayIntPort objGestionarSolicitudGateway;
    private final GestionarCursoOfertadoVeranoGatewayIntPort objCursoOfertado;
    private final GestionarUsuarioGatewayIntPort objUsuario;
    private final FormateadorResultadosIntPort objFormateadorResultados;

    public GestionarSolicitudCursoVeranoCUAdapter(
        GestionarSolicitudCursoVeranoGatewayIntPort objGestionarSolicitudGateway,
        GestionarCursoOfertadoVeranoGatewayIntPort objCursoOfertado,
        GestionarUsuarioGatewayIntPort objUsuario, 
        GestionarDocumentosGatewayIntPort objDocumentosGateway,
        FormateadorResultadosIntPort objFormateadorResultados
    ) {
        this.objUsuario = objUsuario;
        this.objFormateadorResultados = objFormateadorResultados;
        this.objCursoOfertado = objCursoOfertado;
        this.objGestionarSolicitudGateway = objGestionarSolicitudGateway;
    }

    @Override
    public SolicitudCursoVeranoPreinscripcion crearSolicitudCursoVeranoPreinscripcion(SolicitudCursoVeranoPreinscripcion solicitudCursoVerano) {
        CursoOfertadoVerano cursoABuscar = null;
        SolicitudCursoVeranoPreinscripcion solicitudGuardada = null;
        Usuario usuarioBuscar = null;
        Solicitud preinscripcionBuscar = null;

        if (solicitudCursoVerano == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud de preinscripción no puede ser nula");
        }
        if (solicitudCursoVerano.getObjUsuario() == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud de preinscripción no puede ser nula");
        }
        if (solicitudCursoVerano.getObjUsuario().getId_usuario() == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El usuario de la solicitud no puede ser nulo");
        }
        usuarioBuscar = this.objUsuario.obtenerUsuarioPorId(solicitudCursoVerano.getObjUsuario().getId_usuario());
        if (usuarioBuscar == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Usuario no encontrado");
        }
        if (!solicitudCursoVerano.getDocumentos().isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("No se debe adjuntar documentos en la solicitud de preinscripción");
        }
        if (solicitudCursoVerano.getObjCursoOfertadoVerano() == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Debe seleccionar un curso válido");
        }
        if (solicitudCursoVerano.getObjCursoOfertadoVerano().getId_curso() == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Debe seleccionar un curso válido");
        }   
        cursoABuscar = this.objCursoOfertado.obtenerCursoPorId(solicitudCursoVerano.getObjCursoOfertadoVerano().getId_curso());
        if (cursoABuscar == null) {   
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("No se encontró el curso");
        }
        if (cursoABuscar.getEstadosCursoOfertados().isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El curso no tiene estados asociados");
        }
        if (!cursoABuscar.getEstadosCursoOfertados().get(cursoABuscar.getEstadosCursoOfertados().size() - 1).getEstado_actual().equals("Publicado")) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El curso no está en estado de publicado");
        }

        if (solicitudCursoVerano.getCodicion_solicitud() == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Debe seleccionar una condición de solicitud válida");
        }

        preinscripcionBuscar = this.objGestionarSolicitudGateway.buscarSolicitudesPorUsuarioYCursoPre(
            usuarioBuscar.getId_usuario(), cursoABuscar.getId_curso()
        );

        if (preinscripcionBuscar != null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Ya existe una solicitud de preinscripción para este curso");
        }

        usuarioBuscar.getSolicitudes().add(solicitudCursoVerano);
        this.objUsuario.actualizarUsuario(usuarioBuscar);

        solicitudCursoVerano.setObjUsuario(usuarioBuscar);

        solicitudGuardada = this.objGestionarSolicitudGateway.crearSolicitudCursoVeranoPreinscripcion(solicitudCursoVerano);

        
        
        return solicitudGuardada;
    }

    @Override
    public SolicitudCursoVeranoIncripcion crearSolicitudCursoVeranoInscripcion(SolicitudCursoVeranoIncripcion solicitudCursoVerano) {
        // TODO: Implementar
        return null;
    }
}
