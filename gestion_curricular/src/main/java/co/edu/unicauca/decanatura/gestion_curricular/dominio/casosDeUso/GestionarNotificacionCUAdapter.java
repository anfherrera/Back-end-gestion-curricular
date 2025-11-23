package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarNotificacionGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarRolGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Notificacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import java.util.Date;

@Slf4j
public class GestionarNotificacionCUAdapter implements GestionarNotificacionCUIntPort {

    private final GestionarNotificacionGatewayIntPort objGestionarNotificacionGateway;
    private final GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway;
    private final GestionarRolGatewayIntPort objGestionarRolGateway;

    public GestionarNotificacionCUAdapter(
            GestionarNotificacionGatewayIntPort objGestionarNotificacionGateway,
            GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway,
            GestionarRolGatewayIntPort objGestionarRolGateway) {
        this.objGestionarNotificacionGateway = objGestionarNotificacionGateway;
        this.objGestionarUsuarioGateway = objGestionarUsuarioGateway;
        this.objGestionarRolGateway = objGestionarRolGateway;
    }

    @Override
    public Notificacion crearNotificacion(Notificacion notificacion) {
        if (notificacion == null) {
            throw new IllegalArgumentException("La notificación no puede ser nula.");
        }
        if (notificacion.getObjUsuario() == null || notificacion.getObjUsuario().getId_usuario() == null) {
            throw new IllegalArgumentException("La notificación debe tener un usuario asociado.");
        }
        if (notificacion.getFechaCreacion() == null) {
            notificacion.setFechaCreacion(new Date());
        }
        return objGestionarNotificacionGateway.crearNotificacion(notificacion);
    }

    @Override
    public Notificacion obtenerNotificacionPorId(Integer idNotificacion) {
        if (idNotificacion == null || idNotificacion <= 0) {
            throw new IllegalArgumentException("El ID de la notificación debe ser válido.");
        }
        return objGestionarNotificacionGateway.obtenerNotificacionPorId(idNotificacion);
    }

    @Override
    public List<Notificacion> buscarPorUsuario(Integer idUsuario) {
        if (idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException("El ID del usuario debe ser válido.");
        }
        return objGestionarNotificacionGateway.buscarPorUsuario(idUsuario);
    }

    @Override
    public List<Notificacion> buscarNoLeidasPorUsuario(Integer idUsuario) {
        if (idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException("El ID del usuario debe ser válido.");
        }
        return objGestionarNotificacionGateway.buscarNoLeidasPorUsuario(idUsuario);
    }

    @Override
    public List<Notificacion> buscarUrgentesPorUsuario(Integer idUsuario) {
        if (idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException("El ID del usuario debe ser válido.");
        }
        return objGestionarNotificacionGateway.buscarUrgentesPorUsuario(idUsuario);
    }

    @Override
    public List<Notificacion> buscarPorTipoSolicitud(String tipoSolicitud) {
        if (tipoSolicitud == null || tipoSolicitud.isBlank()) {
            throw new IllegalArgumentException("El tipo de solicitud no puede estar vacío.");
        }
        return objGestionarNotificacionGateway.buscarPorTipoSolicitud(tipoSolicitud);
    }

    @Override
    public List<Notificacion> buscarPorSolicitud(Integer idSolicitud) {
        if (idSolicitud == null || idSolicitud <= 0) {
            throw new IllegalArgumentException("El ID de la solicitud debe ser válido.");
        }
        return objGestionarNotificacionGateway.buscarPorSolicitud(idSolicitud);
    }

    @Override
    public Long contarNoLeidasPorUsuario(Integer idUsuario) {
        if (idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException("El ID del usuario debe ser válido.");
        }
        return objGestionarNotificacionGateway.contarNoLeidasPorUsuario(idUsuario);
    }

    @Override
    public void marcarTodasComoLeidas(Integer idUsuario) {
        if (idUsuario == null || idUsuario <= 0) {
            throw new IllegalArgumentException("El ID del usuario debe ser válido.");
        }
        objGestionarNotificacionGateway.marcarTodasComoLeidas(idUsuario);
    }

    @Override
    public void marcarComoLeida(Integer idNotificacion) {
        if (idNotificacion == null || idNotificacion <= 0) {
            throw new IllegalArgumentException("El ID de la notificación debe ser válido.");
        }
        objGestionarNotificacionGateway.marcarComoLeida(idNotificacion);
    }

    @Override
    public void eliminarAntiguas(Date fechaLimite) {
        if (fechaLimite == null) {
            throw new IllegalArgumentException("La fecha límite no puede ser nula.");
        }
        objGestionarNotificacionGateway.eliminarAntiguas(fechaLimite);
    }

    @Override
    public void notificarCreacionSolicitud(Solicitud solicitud, String tipoSolicitud) {
        try {
            if (solicitud == null) {
                log.error("Error: Solicitud es nula al intentar crear notificación");
                return;
            }
            
            if (solicitud.getObjUsuario() == null || solicitud.getObjUsuario().getId_usuario() == null) {
                log.error("Error: Usuario es nulo o sin ID al intentar crear notificación. Solicitud ID: {}", solicitud.getId_solicitud());
                return;
            }

            String estadoActual = "ENVIADA";
            if (solicitud.getEstadosSolicitud() != null && !solicitud.getEstadosSolicitud().isEmpty()) {
                estadoActual = solicitud.getEstadosSolicitud().get(solicitud.getEstadosSolicitud().size() - 1).getEstado_actual();
            }

            Notificacion notificacion = new Notificacion();
            notificacion.setTipoSolicitud(tipoSolicitud);
            notificacion.setTipoNotificacion("NUEVA_SOLICITUD");
            notificacion.setTitulo("Nueva solicitud creada");
            notificacion.setMensaje("Se ha creado una nueva solicitud de tipo: " + tipoSolicitud + ". Su solicitud está en estado: " + estadoActual);
            notificacion.setFechaCreacion(new Date());
            notificacion.setLeida(false);
            notificacion.setEsUrgente(false);
            notificacion.setObjUsuario(solicitud.getObjUsuario());
            notificacion.setObjSolicitud(solicitud);
            notificacion.setObjCurso(solicitud.getObjCursoOfertadoVerano());
            notificacion.setAccion("Ver solicitud");
            notificacion.setUrlAccion("/solicitudes/" + solicitud.getId_solicitud());

            // Notificar al estudiante
            crearNotificacion(notificacion);
            log.debug("Notificación creada para estudiante ID: {}, Solicitud ID: {}", 
                    solicitud.getObjUsuario().getId_usuario(), solicitud.getId_solicitud());

            // Notificar a funcionarios
            notificarAFuncionariosYCoordinadores(solicitud, tipoSolicitud, estadoActual);
        } catch (Exception e) {
            log.error("Error al crear notificación de creación de solicitud: {}", e.getMessage(), e);
        }
    }

    /**
     * Notifica a todos los funcionarios cuando se crea una nueva solicitud (estado "Enviada")
     * Los coordinadores y secretarias reciben notificaciones cuando cambia el estado
     */
    private void notificarAFuncionariosYCoordinadores(Solicitud solicitud, String tipoSolicitud, String estadoActual) {
        try {
            Rol rolFuncionario = objGestionarRolGateway.obtenerRolPorNombre("Funcionario");
            
            if (rolFuncionario == null || rolFuncionario.getId_rol() == null) {
                log.warn("No se encontró el rol 'Funcionario'");
                return;
            }

            List<Usuario> funcionarios = objGestionarUsuarioGateway.buscarUsuariosPorRol(rolFuncionario.getId_rol());
            for (Usuario funcionario : funcionarios) {
                crearNotificacionParaFuncionario(solicitud, tipoSolicitud, estadoActual, funcionario, "Funcionario");
            }
            log.debug("Notificaciones enviadas a {} funcionario(s) para solicitud ID: {}", 
                    funcionarios.size(), solicitud.getId_solicitud());
        } catch (Exception e) {
            log.error("Error al notificar a funcionarios: {}", e.getMessage(), e);
        }
    }

    /**
     * Crea una notificación para un funcionario, coordinador o secretario sobre una solicitud
     */
    private void crearNotificacionParaFuncionario(Solicitud solicitud, String tipoSolicitud, String estadoActual, Usuario funcionario, String tipoRol) {
        try {
            Notificacion notificacion = new Notificacion();
            notificacion.setTipoSolicitud(tipoSolicitud);
            notificacion.setTipoNotificacion("NUEVA_SOLICITUD");
            
            String nombreEstudiante = solicitud.getObjUsuario() != null && solicitud.getObjUsuario().getNombre_completo() != null
                    ? solicitud.getObjUsuario().getNombre_completo()
                    : "Estudiante";
            
            // Personalizar título y mensaje según el rol
            String titulo;
            String mensaje;
            
            if ("Coordinador".equals(tipoRol)) {
                titulo = "Solicitud aprobada por funcionario - Requiere revisión";
                mensaje = "Una solicitud de tipo " + tipoSolicitud + 
                        " del estudiante " + nombreEstudiante + 
                        " ha sido aprobada por un funcionario (Estado: " + estadoActual + "). " +
                        "Requiere su revisión como coordinador.";
            } else if ("Secretario".equals(tipoRol)) {
                titulo = "Solicitud aprobada por coordinador - Requiere revisión";
                mensaje = "Una solicitud de tipo " + tipoSolicitud + 
                        " del estudiante " + nombreEstudiante + 
                        " ha sido aprobada por un coordinador (Estado: " + estadoActual + "). " +
                        "Requiere su revisión como secretario.";
            } else {
                // Funcionario
                titulo = "Nueva solicitud pendiente de revisión";
                mensaje = "Se ha creado una nueva solicitud de tipo " + tipoSolicitud + 
                        " por el estudiante " + nombreEstudiante + 
                        ". Estado actual: " + estadoActual + 
                        ". Requiere revisión.";
            }
            
            notificacion.setTitulo(titulo);
            notificacion.setMensaje(mensaje);
            notificacion.setFechaCreacion(new Date());
            notificacion.setLeida(false);
            notificacion.setEsUrgente(true); // Urgente para todos los roles administrativos
            notificacion.setObjUsuario(funcionario);
            notificacion.setObjSolicitud(solicitud);
            notificacion.setObjCurso(solicitud.getObjCursoOfertadoVerano());
            notificacion.setAccion("Revisar solicitud");
            notificacion.setUrlAccion("/solicitudes/" + solicitud.getId_solicitud());

            crearNotificacion(notificacion);
        } catch (Exception e) {
            log.error("Error al crear notificación para {} ID {}: {}", tipoRol, funcionario.getId_usuario(), e.getMessage(), e);
        }
    }

    @Override
    public void notificarCambioEstadoSolicitud(Solicitud solicitud, String estadoAnterior, String estadoNuevo, String tipoSolicitud) {
        if (solicitud == null || solicitud.getObjUsuario() == null) {
            return;
        }

        // Notificar al estudiante sobre el cambio de estado
        Notificacion notificacion = new Notificacion();
        notificacion.setTipoSolicitud(tipoSolicitud);
        notificacion.setTipoNotificacion("CAMBIO_ESTADO");
        notificacion.setTitulo("Estado de solicitud actualizado");
        
        String mensaje = "Su solicitud de tipo " + tipoSolicitud + " ha cambiado de estado: " + 
            estadoAnterior + " → " + estadoNuevo;
        
        // Agregar información adicional según el estado
        if ("APROBADA".equals(estadoNuevo) || "APROBADA_COORDINADOR".equals(estadoNuevo) || "APROBADA_FUNCIONARIO".equals(estadoNuevo)) {
            notificacion.setEsUrgente(true);
            mensaje += ". ¡Su solicitud ha sido aprobada!";
        } else if ("RECHAZADA".equals(estadoNuevo)) {
            notificacion.setEsUrgente(true);
            mensaje += ". Su solicitud ha sido rechazada. Por favor, revise los detalles.";
        }

        notificacion.setMensaje(mensaje);
        notificacion.setFechaCreacion(new Date());
        notificacion.setLeida(false);
        notificacion.setObjUsuario(solicitud.getObjUsuario());
        notificacion.setObjSolicitud(solicitud);
        notificacion.setObjCurso(solicitud.getObjCursoOfertadoVerano());
        notificacion.setAccion("Ver solicitud");
        notificacion.setUrlAccion("/solicitudes/" + solicitud.getId_solicitud());

        // Notificar al estudiante
        crearNotificacion(notificacion);

        // Notificar a coordinadores cuando el funcionario aprueba (estado: APROBADA_FUNCIONARIO)
        if ("APROBADA_FUNCIONARIO".equals(estadoNuevo)) {
            notificarACoordinadores(solicitud, tipoSolicitud, estadoNuevo);
        }

        // Notificar a secretarias cuando el coordinador aprueba (estado: APROBADA_COORDINADOR)
        if ("APROBADA_COORDINADOR".equals(estadoNuevo)) {
            notificarASecretarias(solicitud, tipoSolicitud, estadoNuevo);
        }
    }

    /**
     * Notifica a todos los coordinadores cuando una solicitud es aprobada por un funcionario
     */
    private void notificarACoordinadores(Solicitud solicitud, String tipoSolicitud, String estadoActual) {
        try {
            Rol rolCoordinador = objGestionarRolGateway.obtenerRolPorNombre("Coordinador");
            
            if (rolCoordinador == null || rolCoordinador.getId_rol() == null) {
                log.warn("No se encontró el rol 'Coordinador'");
                return;
            }
            
            List<Usuario> coordinadores = objGestionarUsuarioGateway.buscarUsuariosPorRol(rolCoordinador.getId_rol());
            for (Usuario coordinador : coordinadores) {
                crearNotificacionParaFuncionario(solicitud, tipoSolicitud, estadoActual, coordinador, "Coordinador");
            }
            log.debug("Notificaciones enviadas a {} coordinador(es) para solicitud ID: {}", 
                    coordinadores.size(), solicitud.getId_solicitud());
        } catch (Exception e) {
            log.error("Error al notificar a coordinadores: {}", e.getMessage(), e);
        }
    }

    /**
     * Notifica a todas las secretarias cuando una solicitud es aprobada por un coordinador
     */
    private void notificarASecretarias(Solicitud solicitud, String tipoSolicitud, String estadoActual) {
        try {
            // El rol en la BD es "Secretario" (masculino)
            Rol rolSecretaria = objGestionarRolGateway.obtenerRolPorNombre("Secretario");
            
            if (rolSecretaria == null || rolSecretaria.getId_rol() == null) {
                log.warn("No se encontró el rol 'Secretario'");
                return;
            }
            
            List<Usuario> secretarias = objGestionarUsuarioGateway.buscarUsuariosPorRol(rolSecretaria.getId_rol());
            for (Usuario secretaria : secretarias) {
                crearNotificacionParaFuncionario(solicitud, tipoSolicitud, estadoActual, secretaria, "Secretario");
            }
            log.debug("Notificaciones enviadas a {} secretario(s)/secretaria(s) para solicitud ID: {}", 
                    secretarias.size(), solicitud.getId_solicitud());
        } catch (Exception e) {
            log.error("Error al notificar a secretarios/secretarias: {}", e.getMessage(), e);
        }
    }
}

