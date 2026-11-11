package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.AsociadorDocumentosPendientes;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCursoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarCursoOfertadoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudCursoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

@Slf4j
public class GestionarSolicitudCursoVeranoCUAdapter implements GestionarSolicitudCursoVeranoCUIntPort {

    /** Fases del curso (ver TransicionEstadoCurso) en las que se aceptan solicitudes. */
    private static final String ESTADO_PREINSCRIPCION = "Preinscripcion";
    private static final String ESTADO_INSCRIPCION = "Inscripcion";

    private final GestionarSolicitudCursoVeranoGatewayIntPort objGestionarSolicitudGateway;
    private final GestionarCursoOfertadoVeranoGatewayIntPort objCursoOfertado;
    private final GestionarUsuarioGatewayIntPort objUsuario;
    private final GestionarDocumentosGatewayIntPort objDocumentosGateway;
    private final FormateadorResultadosIntPort objFormateadorResultados;
    private final AsociadorDocumentosPendientes asociadorDocumentos;
    private final GestionarNotificacionCUIntPort objGestionarNotificacionCU;

    public GestionarSolicitudCursoVeranoCUAdapter(
        GestionarSolicitudCursoVeranoGatewayIntPort objGestionarSolicitudGateway,
        GestionarCursoOfertadoVeranoGatewayIntPort objCursoOfertado,
        GestionarUsuarioGatewayIntPort objUsuario, 
        GestionarDocumentosGatewayIntPort objDocumentosGateway,
        FormateadorResultadosIntPort objFormateadorResultados,
        GestionarArchivosCUIntPort gestionarArchivos,
        GestionarNotificacionCUIntPort objGestionarNotificacionCU
    ) {
        this.objUsuario = objUsuario;
        this.objFormateadorResultados = objFormateadorResultados;
        this.objCursoOfertado = objCursoOfertado;
        this.objGestionarSolicitudGateway = objGestionarSolicitudGateway;
        this.objDocumentosGateway = objDocumentosGateway;
        this.asociadorDocumentos = new AsociadorDocumentosPendientes(objDocumentosGateway, gestionarArchivos);
        this.objGestionarNotificacionCU = objGestionarNotificacionCU;
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
        if (solicitudCursoVerano.getDocumentos() != null && !solicitudCursoVerano.getDocumentos().isEmpty()) {
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
            // Curso existente: solo admite preinscripciones mientras está en la fase de Preinscripcion
            cursoABuscar = this.objCursoOfertado.obtenerCursoPorId(solicitudCursoVerano.getObjCursoOfertadoVerano().getId_curso());
            if (cursoABuscar == null) {
                this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("No se encontró el curso");
            }
            exigirEstadoCurso(cursoABuscar, ESTADO_PREINSCRIPCION, "preinscripciones");
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

        // La relación usuario-solicitud la guarda la solicitud (objUsuario); no se actualiza el usuario,
        // porque su lista tiene orphanRemoval y guardarla incompleta podría borrar otras solicitudes.
        solicitudCursoVerano.setObjUsuario(usuarioBuscar);

        solicitudGuardada = this.objGestionarSolicitudGateway.crearSolicitudCursoVeranoPreinscripcion(solicitudCursoVerano);
        
        // Crear notificación automática para el estudiante
        try {
            this.objGestionarNotificacionCU.notificarCreacionSolicitud(solicitudGuardada, "CURSO_VERANO_PREINSCRIPCION");
        } catch (Exception e) {
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
        // El comprobante de pago se sube después de crear la inscripción (se asocia al final),
        // por eso no se exige aquí.
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
        // Solo se admiten inscripciones mientras el curso está en la fase de Inscripcion
        exigirEstadoCurso(cursoABuscar, ESTADO_INSCRIPCION, "inscripciones");

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
        String estadoPreinscripcion = ultimoEstado(preinscripcionBuscar.getEstadosSolicitud());
        if (!"Aprobado".equals(estadoPreinscripcion) && !"Aprobada".equals(estadoPreinscripcion)) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Su preinscripción no ha sido aprobada");
        }

        // No puede tener otra inscripción activa (una con pago rechazado no cuenta)
        boolean tieneInscripcionActiva = this.objGestionarSolicitudGateway
                .buscarInscripcionesPorUsuarioYCurso(usuarioBuscar.getId_usuario(), cursoABuscar.getId_curso())
                .stream()
                .anyMatch(insc -> !"Pago_Rechazado".equals(ultimoEstado(insc.getEstadosSolicitud())));
        if (tieneInscripcionActiva) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Ya tiene una inscripción activa para este curso");
        }

        // El curso debe tener cupo (se vuelve a verificar con bloqueo al aceptar la inscripción)
        Integer cupo = cursoABuscar.getCupo_estimado();
        if (cupo != null && cupo > 0) {
            Integer ocupados = this.objGestionarSolicitudGateway.contarInscripcionesAceptadasPorCurso(cursoABuscar.getId_curso());
            if (ocupados != null && ocupados >= cupo) {
                throw new co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.CupoAgotadoException(cupo, ocupados);
            }
        }

        // La relación usuario-solicitud la guarda la solicitud (objUsuario); ver crearSolicitudCursoVeranoPreinscripcion
        solicitudCursoVerano.setObjUsuario(usuarioBuscar);
        solicitudCursoVerano.setObjCursoOfertadoVerano(cursoABuscar);

        solicitudGuardada = this.objGestionarSolicitudGateway.crearSolicitudCursoVeranoInscripcion(solicitudCursoVerano);

        // Mover a la carpeta de la solicitud y asociar los documentos que este estudiante subió antes de enviarla
        asociadorDocumentos.asociar(solicitudGuardada, usuarioBuscar.getId_usuario(), "curso-verano");

        // Crear notificación automática para el estudiante
        try {
            this.objGestionarNotificacionCU.notificarCreacionSolicitud(solicitudGuardada, "CURSO_VERANO_INSCRIPCION");
        } catch (Exception e) {
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
        
        // Verificar que esté en estado "Enviada" o "Pago_Validado" usando el último estado
        String estadoActual = null;
        if (solicitud.getEstadosSolicitud() != null && !solicitud.getEstadosSolicitud().isEmpty()) {
            EstadoSolicitud ultimoEstado = solicitud.getEstadosSolicitud().get(solicitud.getEstadosSolicitud().size() - 1);
            estadoActual = ultimoEstado.getEstado_actual();
        }
        
        if (estadoActual == null || (!estadoActual.equals("Enviada") && !estadoActual.equals("Pago_Validado"))) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud no está en estado 'Enviada' o 'Pago_Validado' para validar el pago. Estado actual: " + estadoActual);
        }
        
        // Regla de cupo: aceptar una inscripción nueva no puede superar el cupo del curso
        if (esValido && "Enviada".equals(estadoActual)
                && solicitud.getObjCursoOfertadoVerano() != null
                && solicitud.getObjCursoOfertadoVerano().getId_curso() != null) {
            Integer cupo = solicitud.getObjCursoOfertadoVerano().getCupo_estimado();
            if (cupo != null && cupo > 0) {
                Integer ocupados = this.objGestionarSolicitudGateway
                        .contarInscripcionesAceptadasPorCurso(solicitud.getObjCursoOfertadoVerano().getId_curso());
                if (ocupados != null && ocupados >= cupo) {
                    throw new co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones
                            .CupoAgotadoException(cupo, ocupados);
                }
            }
        }

        // Validar el pago (el gateway vuelve a contar con el curso bloqueado, por si otro
        // funcionario aceptó una inscripción entre este conteo y el guardado)
        SolicitudCursoVeranoIncripcion solicitudActualizada = this.objGestionarSolicitudGateway.validarPago(idSolicitud, esValido, observaciones);
        
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

        return solicitudCompletada;
    }

    // ==================== Consultas ====================

    @Override
    public Solicitud buscarPreinscripcionPorUsuarioYCurso(Integer idUsuario, Integer idCurso) {
        if (idUsuario == null || idCurso == null) {
            return null;
        }
        return this.objGestionarSolicitudGateway.buscarSolicitudesPorUsuarioYCursoPre(idUsuario, idCurso);
    }

    @Override
    public List<SolicitudCursoVeranoIncripcion> buscarInscripcionesPorUsuarioYCurso(Integer idUsuario, Integer idCurso) {
        if (idUsuario == null || idCurso == null) {
            return List.of();
        }
        return this.objGestionarSolicitudGateway.buscarInscripcionesPorUsuarioYCurso(idUsuario, idCurso);
    }

    @Override
    public List<SolicitudCursoVeranoIncripcion> buscarInscripcionesPorUsuario(Integer idUsuario) {
        if (idUsuario == null) {
            return List.of();
        }
        return this.objGestionarSolicitudGateway.buscarInscripcionesPorUsuario(idUsuario);
    }

    @Override
    public Integer contarInscripcionesPorEstado(Integer idCurso, String estado) {
        return this.objGestionarSolicitudGateway.contarInscripcionesPorEstado(idCurso, estado);
    }

    @Override
    public Integer contarInscripcionesAceptadasPorCurso(Integer idCurso) {
        return this.objGestionarSolicitudGateway.contarInscripcionesAceptadasPorCurso(idCurso);
    }

    @Override
    public SolicitudCursoVeranoPreinscripcion actualizarObservacionesPreinscripcion(
            SolicitudCursoVeranoPreinscripcion preinscripcion) {
        if (preinscripcion == null || preinscripcion.getId_solicitud() == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La preinscripción a actualizar no puede ser nula");
        }
        return this.objGestionarSolicitudGateway.actualizarSolicitudCursoVerano(preinscripcion);
    }

    // ==================== Apoyo ====================

    /** Último estado con texto de la lista, o null si no hay. */
    private static String ultimoEstado(List<EstadoSolicitud> estados) {
        if (estados == null || estados.isEmpty()) {
            return null;
        }
        return estados.get(estados.size() - 1).getEstado_actual();
    }

    /** Exige que el curso esté en la fase indicada (último estado). */
    private void exigirEstadoCurso(CursoOfertadoVerano curso, String estadoRequerido, String queSolicitud) {
        String actual = curso.getEstadosCursoOfertados() == null || curso.getEstadosCursoOfertados().isEmpty()
                ? null
                : curso.getEstadosCursoOfertados().get(curso.getEstadosCursoOfertados().size() - 1).getEstado_actual();
        if (!estadoRequerido.equals(actual)) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio(
                    "El curso no está recibiendo " + queSolicitud + " (estado actual: "
                            + (actual != null ? actual : "sin estado") + ", se requiere: " + estadoRequerido + ")");
        }
    }
}
