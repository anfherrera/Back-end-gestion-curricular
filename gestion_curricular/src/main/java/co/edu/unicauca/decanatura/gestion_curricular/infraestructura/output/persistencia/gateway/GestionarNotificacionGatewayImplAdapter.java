package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarNotificacionGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Notificacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.NotificacionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.NotificacionRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;

@Service
@Transactional
public class GestionarNotificacionGatewayImplAdapter implements GestionarNotificacionGatewayIntPort {

    private final NotificacionRepositoryInt notificacionRepository;
    private final UsuarioRepositoryInt usuarioRepository;
    private final SolicitudRepositoryInt solicitudRepository;
    private final ModelMapper notificacionMapper;

    public GestionarNotificacionGatewayImplAdapter(NotificacionRepositoryInt notificacionRepository,
                                                   UsuarioRepositoryInt usuarioRepository,
                                                   SolicitudRepositoryInt solicitudRepository,
                                                   ModelMapper notificacionMapper) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.solicitudRepository = solicitudRepository;
        this.notificacionMapper = notificacionMapper;
    }

    @Override
    @Transactional
    public Notificacion crearNotificacion(Notificacion notificacion) {
        NotificacionEntity entity = notificacionMapper.map(notificacion, NotificacionEntity.class);
        NotificacionEntity savedEntity = notificacionRepository.save(entity);
        return notificacionMapper.map(savedEntity, Notificacion.class);
    }

    @Override
    @Transactional
    public Notificacion actualizarNotificacion(Notificacion notificacion) {
        NotificacionEntity entity = notificacionMapper.map(notificacion, NotificacionEntity.class);
        NotificacionEntity savedEntity = notificacionRepository.save(entity);
        return notificacionMapper.map(savedEntity, Notificacion.class);
    }

    @Override
    @Transactional
    public boolean eliminarNotificacion(Integer idNotificacion) {
        if (notificacionRepository.existsById(idNotificacion)) {
            notificacionRepository.deleteById(idNotificacion);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public Notificacion obtenerNotificacionPorId(Integer idNotificacion) {
        return notificacionRepository.findById(idNotificacion)
            .map(entity -> notificacionMapper.map(entity, Notificacion.class))
            .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> buscarPorUsuario(Integer idUsuario) {
        List<NotificacionEntity> entities = notificacionRepository.buscarPorUsuario(idUsuario);
        return entities.stream()
            .map(entity -> notificacionMapper.map(entity, Notificacion.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notificacion> buscarPorUsuarioPaginado(Integer idUsuario, Pageable pageable) {
        Page<NotificacionEntity> entities = notificacionRepository.buscarPorUsuarioPaginado(idUsuario, pageable);
        return entities.map(entity -> notificacionMapper.map(entity, Notificacion.class));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> buscarNoLeidasPorUsuario(Integer idUsuario) {
        List<NotificacionEntity> entities = notificacionRepository.buscarNoLeidasPorUsuario(idUsuario);
        return entities.stream()
            .map(entity -> notificacionMapper.map(entity, Notificacion.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Integer contarNoLeidasPorUsuario(Integer idUsuario) {
        return notificacionRepository.contarNoLeidasPorUsuario(idUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> buscarPorTipoSolicitud(String tipoSolicitud) {
        List<NotificacionEntity> entities = notificacionRepository.buscarPorTipoSolicitud(tipoSolicitud);
        return entities.stream()
            .map(entity -> notificacionMapper.map(entity, Notificacion.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> buscarPorTipoNotificacion(String tipoNotificacion) {
        List<NotificacionEntity> entities = notificacionRepository.buscarPorTipoNotificacion(tipoNotificacion);
        return entities.stream()
            .map(entity -> notificacionMapper.map(entity, Notificacion.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> buscarPorUsuarioYTipoSolicitud(Integer idUsuario, String tipoSolicitud) {
        List<NotificacionEntity> entities = notificacionRepository.buscarPorUsuarioYTipoSolicitud(idUsuario, tipoSolicitud);
        return entities.stream()
            .map(entity -> notificacionMapper.map(entity, Notificacion.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> buscarPorSolicitud(Integer idSolicitud) {
        List<NotificacionEntity> entities = notificacionRepository.buscarPorSolicitud(idSolicitud);
        return entities.stream()
            .map(entity -> notificacionMapper.map(entity, Notificacion.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> buscarPorCurso(Integer idCurso) {
        List<NotificacionEntity> entities = notificacionRepository.buscarPorCurso(idCurso);
        return entities.stream()
            .map(entity -> notificacionMapper.map(entity, Notificacion.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> buscarUrgentes() {
        List<NotificacionEntity> entities = notificacionRepository.buscarUrgentes();
        return entities.stream()
            .map(entity -> notificacionMapper.map(entity, Notificacion.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> buscarPorRangoFechas(Date fechaInicio, Date fechaFin) {
        List<NotificacionEntity> entities = notificacionRepository.buscarPorRangoFechas(fechaInicio, fechaFin);
        return entities.stream()
            .map(entity -> notificacionMapper.map(entity, Notificacion.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean marcarComoLeida(Integer idNotificacion) {
        NotificacionEntity entity = notificacionRepository.findById(idNotificacion).orElse(null);
        if (entity != null) {
            entity.setLeida(true);
            notificacionRepository.save(entity);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean marcarComoNoLeida(Integer idNotificacion) {
        NotificacionEntity entity = notificacionRepository.findById(idNotificacion).orElse(null);
        if (entity != null) {
            entity.setLeida(false);
            notificacionRepository.save(entity);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean marcarTodasComoLeidas(Integer idUsuario) {
        List<NotificacionEntity> entities = notificacionRepository.buscarNoLeidasPorUsuario(idUsuario);
        entities.forEach(entity -> entity.setLeida(true));
        notificacionRepository.saveAll(entities);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> estadisticasPorTipoSolicitud() {
        List<Object[]> results = notificacionRepository.estadisticasPorTipoSolicitud();
        return results.stream()
            .collect(Collectors.toMap(
                result -> (String) result[0],
                result -> ((Number) result[1]).longValue()
            ));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> estadisticasPorTipoNotificacion() {
        List<Object[]> results = notificacionRepository.estadisticasPorTipoNotificacion();
        return results.stream()
            .collect(Collectors.toMap(
                result -> (String) result[0],
                result -> ((Number) result[1]).longValue()
            ));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, Long> estadisticasPorUsuario() {
        List<Object[]> results = notificacionRepository.estadisticasPorUsuario();
        return results.stream()
            .collect(Collectors.toMap(
                result -> ((Number) result[0]).intValue(),
                result -> ((Number) result[1]).longValue()
            ));
    }

    @Override
    @Transactional
    public void eliminarNotificacionesAntiguas(Date fechaLimite) {
        notificacionRepository.eliminarAntiguas(fechaLimite);
    }

    // Métodos de utilidad para crear notificaciones comunes
    @Override
    @Transactional
    public Notificacion crearNotificacionNuevaSolicitud(String tipoSolicitud, Integer idUsuario, Integer idSolicitud, String mensaje) {
        UsuarioEntity usuario = usuarioRepository.findById(idUsuario).orElse(null);
        SolicitudEntity solicitud = solicitudRepository.findById(idSolicitud).orElse(null);
        
        if (usuario == null) return null;
        
        Notificacion notificacion = new Notificacion();
        notificacion.setTipoSolicitud(tipoSolicitud);
        notificacion.setTipoNotificacion("NUEVA_SOLICITUD");
        notificacion.setTitulo("Nueva Solicitud");
        notificacion.setMensaje(mensaje);
        notificacion.setObjUsuario(notificacionMapper.map(usuario, Usuario.class));
        if (solicitud != null) {
            notificacion.setObjSolicitud(notificacionMapper.map(solicitud, Solicitud.class));
        }
        notificacion.setAccion("VER_SOLICITUD");
        notificacion.setUrlAccion("/solicitudes/" + idSolicitud);
        notificacion.setEsUrgente(false); // ✅ AGREGAR ESTA LÍNEA
        notificacion.setFechaCreacion(new Date()); // ✅ AGREGAR ESTA LÍNEA
        notificacion.setLeida(false); // ✅ AGREGAR ESTA LÍNEA
        
        return crearNotificacion(notificacion);
    }

    @Override
    @Transactional
    public Notificacion crearNotificacionCambioEstado(String tipoSolicitud, Integer idUsuario, Integer idSolicitud, String estadoAnterior, String estadoNuevo) {
        UsuarioEntity usuario = usuarioRepository.findById(idUsuario).orElse(null);
        SolicitudEntity solicitud = solicitudRepository.findById(idSolicitud).orElse(null);
        
        if (usuario == null) return null;
        
        Notificacion notificacion = new Notificacion();
        notificacion.setTipoSolicitud(tipoSolicitud);
        notificacion.setTipoNotificacion("CAMBIO_ESTADO");
        notificacion.setTitulo("Estado de Solicitud Actualizado");
        notificacion.setMensaje("Tu solicitud cambió de estado: " + estadoAnterior + " → " + estadoNuevo);
        notificacion.setObjUsuario(notificacionMapper.map(usuario, Usuario.class));
        if (solicitud != null) {
            notificacion.setObjSolicitud(notificacionMapper.map(solicitud, Solicitud.class));
        }
        notificacion.setAccion("VER_SOLICITUD");
        notificacion.setUrlAccion("/solicitudes/" + idSolicitud);
        notificacion.setEsUrgente(false); // ✅ AGREGAR ESTA LÍNEA
        
        return crearNotificacion(notificacion);
    }

    @Override
    @Transactional
    public Notificacion crearNotificacionAprobacion(String tipoSolicitud, Integer idUsuario, Integer idSolicitud, boolean aprobado, String motivo) {
        UsuarioEntity usuario = usuarioRepository.findById(idUsuario).orElse(null);
        SolicitudEntity solicitud = solicitudRepository.findById(idSolicitud).orElse(null);
        
        if (usuario == null) return null;
        
        Notificacion notificacion = new Notificacion();
        notificacion.setTipoSolicitud(tipoSolicitud);
        notificacion.setTipoNotificacion(aprobado ? "APROBADO" : "RECHAZADO");
        notificacion.setTitulo(aprobado ? "Solicitud Aprobada" : "Solicitud Rechazada");
        notificacion.setMensaje(aprobado ? 
            "¡Felicidades! Tu solicitud ha sido aprobada." + (motivo != null ? " Motivo: " + motivo : "") :
            "Tu solicitud ha sido rechazada." + (motivo != null ? " Motivo: " + motivo : ""));
        notificacion.setObjUsuario(notificacionMapper.map(usuario, Usuario.class));
        if (solicitud != null) {
            notificacion.setObjSolicitud(notificacionMapper.map(solicitud, Solicitud.class));
        }
        notificacion.setAccion("VER_SOLICITUD");
        notificacion.setUrlAccion("/solicitudes/" + idSolicitud);
        notificacion.setEsUrgente(false); // ✅ AGREGAR ESTA LÍNEA
        notificacion.setFechaCreacion(new Date()); // ✅ AGREGAR ESTA LÍNEA
        notificacion.setLeida(false); // ✅ AGREGAR ESTA LÍNEA
        
        return crearNotificacion(notificacion);
    }

    @Override
    @Transactional
    public Notificacion crearNotificacionAlerta(String tipoSolicitud, Integer idUsuario, String titulo, String mensaje, boolean esUrgente) {
        UsuarioEntity usuario = usuarioRepository.findById(idUsuario).orElse(null);
        
        if (usuario == null) return null;
        
        Notificacion notificacion = new Notificacion();
        notificacion.setTipoSolicitud(tipoSolicitud);
        notificacion.setTipoNotificacion("ALERTA");
        notificacion.setTitulo(titulo);
        notificacion.setMensaje(mensaje);
        notificacion.setEsUrgente(esUrgente);
        notificacion.setObjUsuario(notificacionMapper.map(usuario, Usuario.class));
        
        return crearNotificacion(notificacion);
    }
}
