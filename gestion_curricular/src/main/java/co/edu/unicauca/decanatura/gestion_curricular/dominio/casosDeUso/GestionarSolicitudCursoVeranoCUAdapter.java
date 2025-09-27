package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.List;
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
        CursoOfertadoVerano cursoABuscar = null;
        SolicitudCursoVeranoIncripcion solicitudGuardada = null;
        Usuario usuarioBuscar = null;
        Solicitud preinscripcionBuscar = null;

        if (solicitudCursoVerano == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud de inscripción no puede ser nula");
        }
        if (solicitudCursoVerano.getObjUsuario() == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El usuario de la solicitud no puede ser nulo");
        }
        if (solicitudCursoVerano.getObjUsuario().getId_usuario() == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El usuario de la solicitud no puede ser nulo");
        }
        usuarioBuscar = this.objUsuario.obtenerUsuarioPorId(solicitudCursoVerano.getObjUsuario().getId_usuario());
        if (usuarioBuscar == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Usuario no encontrado");
        }
        if (solicitudCursoVerano.getDocumentos().isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Debe adjuntar el comprobante de pago en la solicitud de inscripción");
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
        if (!cursoABuscar.getEstadosCursoOfertados().get(cursoABuscar.getEstadosCursoOfertados().size() - 1).getEstado_actual().equals("Preinscripcion")) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El curso no está en estado de preinscripción");
        }

        if (solicitudCursoVerano.getCodicion_solicitud() == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Debe seleccionar una condición de solicitud válida");
        }

        // Verificar que el estudiante tenga una preinscripción aprobada
        preinscripcionBuscar = this.objGestionarSolicitudGateway.buscarSolicitudesPorUsuarioYCursoPre(
            usuarioBuscar.getId_usuario(), cursoABuscar.getId_curso()
        );

        if (preinscripcionBuscar == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Debe tener una preinscripción aprobada para este curso");
        }

        // Verificar que la preinscripción esté aprobada
        if (preinscripcionBuscar.getEstadosSolicitud().isEmpty() || 
            !preinscripcionBuscar.getEstadosSolicitud().get(preinscripcionBuscar.getEstadosSolicitud().size() - 1).getEstado_actual().equals("Aprobado")) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Su preinscripción no ha sido aprobada");
        }

        usuarioBuscar.getSolicitudes().add(solicitudCursoVerano);
        this.objUsuario.actualizarUsuario(usuarioBuscar);

        solicitudCursoVerano.setObjUsuario(usuarioBuscar);

        solicitudGuardada = this.objGestionarSolicitudGateway.crearSolicitudCursoVeranoInscripcion(solicitudCursoVerano);

        return solicitudGuardada;
    }

    @Override
    public List<SolicitudCursoVeranoPreinscripcion> buscarSolicitudesPorUsuario(Integer idUsuario) {
        if (idUsuario == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID del usuario no puede ser nulo");
        }
        return this.objGestionarSolicitudGateway.buscarSolicitudesPorUsuario(idUsuario);
    }

    @Override
    public SolicitudCursoVeranoPreinscripcion buscarSolicitudPorId(Integer idSolicitud) {
        if (idSolicitud == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID de la solicitud no puede ser nulo");
        }
        return this.objGestionarSolicitudGateway.buscarSolicitudPorId(idSolicitud);
    }

    @Override
    public List<SolicitudCursoVeranoPreinscripcion> buscarPreinscripcionesPorCurso(Integer idCurso) {
        if (idCurso == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID del curso no puede ser nulo");
        }
        return this.objGestionarSolicitudGateway.buscarPreinscripcionesPorCurso(idCurso);
    }

    @Override
    public List<SolicitudCursoVeranoIncripcion> buscarInscripcionesPorCurso(Integer idCurso) {
        if (idCurso == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID del curso no puede ser nulo");
        }
        return this.objGestionarSolicitudGateway.buscarInscripcionesPorCurso(idCurso);
    }

    @Override
    public Integer contarSolicitudesPorCurso(Integer idCurso) {
        if (idCurso == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID del curso no puede ser nulo");
        }
        return this.objGestionarSolicitudGateway.contarSolicitudesPorCurso(idCurso);
    }

    @Override
    public List<SolicitudCursoVeranoPreinscripcion> buscarCursosConAltaDemanda(Integer limiteMinimo) {
        if (limiteMinimo == null || limiteMinimo < 1) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El límite mínimo debe ser mayor a 0");
        }
        return this.objGestionarSolicitudGateway.buscarCursosConAltaDemanda(limiteMinimo);
    }

    @Override
    public SolicitudCursoVeranoPreinscripcion aprobarPreinscripcion(Integer idSolicitud, String comentarios) {
        if (idSolicitud == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID de la solicitud no puede ser nulo");
        }
        
        // Buscar la solicitud
        SolicitudCursoVeranoPreinscripcion solicitud = this.objGestionarSolicitudGateway.buscarSolicitudPorId(idSolicitud);
        if (solicitud == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró la solicitud con ID: " + idSolicitud);
        }
        
        // Verificar que esté en estado "Enviada"
        if (solicitud.getEstadosSolicitud().isEmpty() || 
            !solicitud.getEstadosSolicitud().get(solicitud.getEstadosSolicitud().size() - 1).getEstado_actual().equals("Enviada")) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud no está en estado 'Enviada' para ser aprobada");
        }
        
        // Aprobar la solicitud
        return this.objGestionarSolicitudGateway.aprobarPreinscripcion(idSolicitud, comentarios);
    }

    @Override
    public SolicitudCursoVeranoPreinscripcion rechazarPreinscripcion(Integer idSolicitud, String motivo) {
        if (idSolicitud == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID de la solicitud no puede ser nulo");
        }
        
        if (motivo == null || motivo.trim().isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Debe proporcionar un motivo para el rechazo");
        }
        
        // Buscar la solicitud
        SolicitudCursoVeranoPreinscripcion solicitud = this.objGestionarSolicitudGateway.buscarSolicitudPorId(idSolicitud);
        if (solicitud == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró la solicitud con ID: " + idSolicitud);
        }
        
        // Verificar que esté en estado "Enviada"
        if (solicitud.getEstadosSolicitud().isEmpty() || 
            !solicitud.getEstadosSolicitud().get(solicitud.getEstadosSolicitud().size() - 1).getEstado_actual().equals("Enviada")) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud no está en estado 'Enviada' para ser rechazada");
        }
        
        // Rechazar la solicitud
        return this.objGestionarSolicitudGateway.rechazarPreinscripcion(idSolicitud, motivo);
    }

    @Override
    public SolicitudCursoVeranoIncripcion validarPago(Integer idSolicitud, boolean esValido, String observaciones) {
        if (idSolicitud == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID de la solicitud no puede ser nulo");
        }
        
        // Buscar la solicitud
        SolicitudCursoVeranoIncripcion solicitud = this.objGestionarSolicitudGateway.buscarSolicitudInscripcionPorId(idSolicitud);
        if (solicitud == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró la solicitud de inscripción con ID: " + idSolicitud);
        }
        
        // Verificar que esté en estado "Enviada"
        if (solicitud.getEstadosSolicitud().isEmpty() || 
            !solicitud.getEstadosSolicitud().get(solicitud.getEstadosSolicitud().size() - 1).getEstado_actual().equals("Enviada")) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud no está en estado 'Enviada' para validar el pago");
        }
        
        // Validar el pago
        return this.objGestionarSolicitudGateway.validarPago(idSolicitud, esValido, observaciones);
    }

    @Override
    public SolicitudCursoVeranoIncripcion completarInscripcion(Integer idSolicitud) {
        if (idSolicitud == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID de la solicitud no puede ser nulo");
        }
        
        // Buscar la solicitud
        SolicitudCursoVeranoIncripcion solicitud = this.objGestionarSolicitudGateway.buscarSolicitudInscripcionPorId(idSolicitud);
        if (solicitud == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró la solicitud de inscripción con ID: " + idSolicitud);
        }
        
        // Verificar que el pago esté validado
        if (solicitud.getEstadosSolicitud().isEmpty() || 
            !solicitud.getEstadosSolicitud().get(solicitud.getEstadosSolicitud().size() - 1).getEstado_actual().equals("Pago_Validado")) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El pago debe estar validado para completar la inscripción");
        }
        
        // Completar la inscripción
        return this.objGestionarSolicitudGateway.completarInscripcion(idSolicitud);
    }
}
