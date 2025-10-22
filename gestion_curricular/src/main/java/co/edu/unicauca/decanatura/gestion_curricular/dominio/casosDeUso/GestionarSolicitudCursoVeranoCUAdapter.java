package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.List;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
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
    private final GestionarNotificacionCUIntPort objNotificacion;
    private final FormateadorResultadosIntPort objFormateadorResultados;

    public GestionarSolicitudCursoVeranoCUAdapter(
        GestionarSolicitudCursoVeranoGatewayIntPort objGestionarSolicitudGateway,
        GestionarCursoOfertadoVeranoGatewayIntPort objCursoOfertado,
        GestionarUsuarioGatewayIntPort objUsuario, 
        GestionarDocumentosGatewayIntPort objDocumentosGateway,
        GestionarNotificacionCUIntPort objNotificacion,
        FormateadorResultadosIntPort objFormateadorResultados
    ) {
        this.objUsuario = objUsuario;
        this.objFormateadorResultados = objFormateadorResultados;
        this.objCursoOfertado = objCursoOfertado;
        this.objGestionarSolicitudGateway = objGestionarSolicitudGateway;
        this.objNotificacion = objNotificacion;
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
        
        // Verificar si es un curso nuevo (ID = 0) - solicitud de apertura
        if (solicitudCursoVerano.getObjCursoOfertadoVerano().getId_curso() == 0) {
            // Para cursos nuevos, no necesitamos validar que exista en la base de datos
            // Solo procedemos a crear la solicitud
        } else {
            // Para cursos existentes, validar que exista (sin validar estados)
        cursoABuscar = this.objCursoOfertado.obtenerCursoPorId(solicitudCursoVerano.getObjCursoOfertadoVerano().getId_curso());
        System.out.println("🔍 DEBUG: Curso buscado con ID: " + solicitudCursoVerano.getObjCursoOfertadoVerano().getId_curso());
        System.out.println("🔍 DEBUG: Curso encontrado: " + (cursoABuscar != null ? "SÍ" : "NO"));
        
        if (cursoABuscar == null) {   
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("No se encontró el curso");
        }
        
        System.out.println("🔍 DEBUG: Estados del curso: " + (cursoABuscar.getEstadosCursoOfertados() != null ? cursoABuscar.getEstadosCursoOfertados().size() : "NULL"));
        
        // Validación opcional de estados - solo si existen
        if (cursoABuscar.getEstadosCursoOfertados() != null && !cursoABuscar.getEstadosCursoOfertados().isEmpty()) {
            String ultimoEstado = cursoABuscar.getEstadosCursoOfertados().get(cursoABuscar.getEstadosCursoOfertados().size() - 1).getEstado_actual();
            System.out.println("🔍 DEBUG: Último estado del curso: " + ultimoEstado);
            // Solo validar si el estado es explícitamente "Cerrado" o "Cancelado"
            if ("Cerrado".equals(ultimoEstado) || "Cancelado".equals(ultimoEstado)) {
                this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El curso está " + ultimoEstado.toLowerCase() + " y no acepta preinscripciones");
            }
        } else {
            System.out.println("🔍 DEBUG: Curso sin estados - permitiendo preinscripción");
        }
        }

        if (solicitudCursoVerano.getCodicion_solicitud() == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Debe seleccionar una condición de solicitud válida");
        }

        // Para cursos nuevos (ID = 0), no necesitamos verificar preinscripciones existentes
        // ya que es una solicitud de apertura de curso
        if (solicitudCursoVerano.getObjCursoOfertadoVerano().getId_curso() != 0) {
        preinscripcionBuscar = this.objGestionarSolicitudGateway.buscarSolicitudesPorUsuarioYCursoPre(
            usuarioBuscar.getId_usuario(), cursoABuscar.getId_curso()
        );

        if (preinscripcionBuscar != null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Ya existe una solicitud de preinscripción para este curso");
            }
        } else {
            // Para cursos nuevos, no necesitamos verificar preinscripciones existentes
        }

        usuarioBuscar.getSolicitudes().add(solicitudCursoVerano);
        this.objUsuario.actualizarUsuario(usuarioBuscar);

        solicitudCursoVerano.setObjUsuario(usuarioBuscar);

        solicitudGuardada = this.objGestionarSolicitudGateway.crearSolicitudCursoVeranoPreinscripcion(solicitudCursoVerano);

        // Crear notificación para el estudiante
        if (solicitudGuardada != null) {
            this.objNotificacion.notificarNuevaPreinscripcion(
                usuarioBuscar.getId_usuario(), 
                solicitudGuardada.getId_solicitud()
            );
        }
        
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

        // Crear notificación para el estudiante
        if (solicitudGuardada != null) {
            this.objNotificacion.notificarNuevaInscripcion(
                usuarioBuscar.getId_usuario(), 
                solicitudGuardada.getId_solicitud()
            );
        }

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
    public SolicitudCursoVeranoIncripcion buscarPorIdInscripcion(Integer idSolicitud) {
        if (idSolicitud == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID de la solicitud no puede ser nulo");
        }
        return this.objGestionarSolicitudGateway.buscarSolicitudInscripcionPorId(idSolicitud);
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
        SolicitudCursoVeranoPreinscripcion solicitudAprobada = this.objGestionarSolicitudGateway.aprobarPreinscripcion(idSolicitud, comentarios);
        
        // Crear notificación para el estudiante
        if (solicitudAprobada != null) {
            this.objNotificacion.notificarPreinscripcionAprobada(
                solicitud.getObjUsuario().getId_usuario(), 
                solicitudAprobada.getId_solicitud()
            );
        }
        
        return solicitudAprobada;
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
        SolicitudCursoVeranoPreinscripcion solicitudRechazada = this.objGestionarSolicitudGateway.rechazarPreinscripcion(idSolicitud, motivo);
        
        // Crear notificación para el estudiante
        if (solicitudRechazada != null) {
            this.objNotificacion.notificarPreinscripcionRechazada(
                solicitud.getObjUsuario().getId_usuario(), 
                solicitudRechazada.getId_solicitud(),
                motivo
            );
        }
        
        return solicitudRechazada;
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
        SolicitudCursoVeranoIncripcion solicitudActualizada = this.objGestionarSolicitudGateway.validarPago(idSolicitud, esValido, observaciones);
        
        // Crear notificación para el estudiante
        if (solicitudActualizada != null) {
            this.objNotificacion.notificarPagoValidado(
                solicitud.getObjUsuario().getId_usuario(), 
                solicitudActualizada.getId_solicitud(),
                esValido
            );
        }
        
        return solicitudActualizada;
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
        SolicitudCursoVeranoIncripcion solicitudCompletada = this.objGestionarSolicitudGateway.completarInscripcion(idSolicitud);
        
        // Crear notificación para el estudiante
        if (solicitudCompletada != null) {
            this.objNotificacion.notificarInscripcionCompletada(
                solicitud.getObjUsuario().getId_usuario(), 
                solicitudCompletada.getId_solicitud()
            );
        }
        
        return solicitudCompletada;
    }
}
