package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarNotificacionGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Notificacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.CursoOfertadoVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.NotificacionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers.SolicitudMapperPersistencia;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers.UsuarioMapperPersistencia;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.CursoOfertadoVeranoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.NotificacionRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;

@Slf4j
@Service
@Transactional
public class GestionarNotificacionGatewayImplAdapter implements GestionarNotificacionGatewayIntPort {

    private final NotificacionRepositoryInt notificacionRepository;
    private final UsuarioRepositoryInt usuarioRepository;
    private final SolicitudRepositoryInt solicitudRepository;
    private final CursoOfertadoVeranoRepositoryInt cursoRepository;

    public GestionarNotificacionGatewayImplAdapter(
            NotificacionRepositoryInt notificacionRepository,
            UsuarioRepositoryInt usuarioRepository,
            SolicitudRepositoryInt solicitudRepository,
            CursoOfertadoVeranoRepositoryInt cursoRepository) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.solicitudRepository = solicitudRepository;
        this.cursoRepository = cursoRepository;
    }

    @Override
    @Transactional
    public Notificacion crearNotificacion(Notificacion notificacion) {
        try {
            NotificacionEntity entity = new NotificacionEntity();
            
            // Mapear campos básicos
            entity.setTipoSolicitud(notificacion.getTipoSolicitud());
            entity.setTipoNotificacion(notificacion.getTipoNotificacion());
            entity.setTitulo(notificacion.getTitulo());
            entity.setMensaje(notificacion.getMensaje());
            entity.setFechaCreacion(notificacion.getFechaCreacion() != null ? notificacion.getFechaCreacion() : new java.util.Date());
            entity.setLeida(notificacion.getLeida() != null ? notificacion.getLeida() : false);
            entity.setEsUrgente(notificacion.getEsUrgente() != null ? notificacion.getEsUrgente() : false);
            entity.setAccion(notificacion.getAccion());
            entity.setUrlAccion(notificacion.getUrlAccion());
            
            // Mapear relaciones - cargar entidades desde la base de datos
            if (notificacion.getObjUsuario() != null && notificacion.getObjUsuario().getIdUsuario() != null) {
                UsuarioEntity usuarioEntity = usuarioRepository.findById(notificacion.getObjUsuario().getIdUsuario())
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + notificacion.getObjUsuario().getIdUsuario()));
                entity.setObjUsuario(usuarioEntity);
            }
            
            if (notificacion.getObjSolicitud() != null && notificacion.getObjSolicitud().getIdSolicitud() != null) {
                SolicitudEntity solicitudEntity = solicitudRepository.findById(notificacion.getObjSolicitud().getIdSolicitud())
                        .orElse(null); // Puede ser null si la solicitud no existe
                entity.setObjSolicitud(solicitudEntity);
            }
            
            if (notificacion.getObjCurso() != null && notificacion.getObjCurso().getId_curso() != null) {
                CursoOfertadoVeranoEntity cursoEntity = cursoRepository.findById(notificacion.getObjCurso().getId_curso())
                        .orElse(null); // Puede ser null si el curso no existe
                entity.setObjCurso(cursoEntity);
            }
            
            NotificacionEntity saved = notificacionRepository.save(entity);
            return SolicitudMapperPersistencia.aDominio(saved);
        } catch (Exception e) {
            log.error("Error al guardar notificación en BD: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Notificacion obtenerNotificacionPorId(Integer idNotificacion) {
        return notificacionRepository.findById(idNotificacion)
                .map(entity -> SolicitudMapperPersistencia.aDominio(entity))
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> buscarPorUsuario(Integer idUsuario) {
        List<NotificacionEntity> entities = notificacionRepository.buscarPorUsuario(idUsuario);
        
        List<Notificacion> result = entities.stream()
                .map(entity -> {
                    Notificacion notificacion = new Notificacion();
                    notificacion.setIdNotificacion(entity.getId_notificacion());
                    notificacion.setTipoSolicitud(entity.getTipoSolicitud());
                    notificacion.setTipoNotificacion(entity.getTipoNotificacion());
                    notificacion.setTitulo(entity.getTitulo());
                    notificacion.setMensaje(entity.getMensaje());
                    notificacion.setFechaCreacion(entity.getFechaCreacion());
                    notificacion.setLeida(entity.getLeida());
                    notificacion.setEsUrgente(entity.getEsUrgente());
                    notificacion.setAccion(entity.getAccion());
                    notificacion.setUrlAccion(entity.getUrlAccion());
                    
                    // Mapear usuario (ya está cargado con JOIN FETCH)
                    if (entity.getObjUsuario() != null) {
                        notificacion.setObjUsuario(UsuarioMapperPersistencia.aDominio(entity.getObjUsuario()));
                    }
                    
                    if (entity.getObjSolicitud() != null && Hibernate.isInitialized(entity.getObjSolicitud())) {
                        notificacion.setObjSolicitud(SolicitudMapperPersistencia.aDominio(entity.getObjSolicitud(), Solicitud.class));
                    }
                    
                    if (entity.getObjCurso() != null && Hibernate.isInitialized(entity.getObjCurso())) {
                        notificacion.setObjCurso(SolicitudMapperPersistencia.aDominio(entity.getObjCurso()));
                    }
                    
                    return notificacion;
                })
                .toList();
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> buscarNoLeidasPorUsuario(Integer idUsuario) {
        List<NotificacionEntity> entities = notificacionRepository.buscarNoLeidasPorUsuario(idUsuario);
        
        List<Notificacion> result = entities.stream()
                .map(entity -> {
                    Notificacion notificacion = new Notificacion();
                    notificacion.setIdNotificacion(entity.getId_notificacion());
                    notificacion.setTipoSolicitud(entity.getTipoSolicitud());
                    notificacion.setTipoNotificacion(entity.getTipoNotificacion());
                    notificacion.setTitulo(entity.getTitulo());
                    notificacion.setMensaje(entity.getMensaje());
                    notificacion.setFechaCreacion(entity.getFechaCreacion());
                    notificacion.setLeida(entity.getLeida());
                    notificacion.setEsUrgente(entity.getEsUrgente());
                    notificacion.setAccion(entity.getAccion());
                    notificacion.setUrlAccion(entity.getUrlAccion());
                    
                    // Mapear usuario (ya está cargado con JOIN FETCH)
                    if (entity.getObjUsuario() != null) {
                        notificacion.setObjUsuario(UsuarioMapperPersistencia.aDominio(entity.getObjUsuario()));
                    }
                    
                    if (entity.getObjSolicitud() != null && Hibernate.isInitialized(entity.getObjSolicitud())) {
                        notificacion.setObjSolicitud(SolicitudMapperPersistencia.aDominio(entity.getObjSolicitud(), Solicitud.class));
                    }
                    
                    if (entity.getObjCurso() != null && Hibernate.isInitialized(entity.getObjCurso())) {
                        notificacion.setObjCurso(SolicitudMapperPersistencia.aDominio(entity.getObjCurso()));
                    }
                    
                    return notificacion;
                })
                .toList();
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> buscarUrgentesPorUsuario(Integer idUsuario) {
        List<NotificacionEntity> entities = notificacionRepository.buscarUrgentesPorUsuario(idUsuario);
        return entities.stream()
                .map(entity -> SolicitudMapperPersistencia.aDominio(entity))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> buscarPorTipoSolicitud(String tipoSolicitud) {
        List<NotificacionEntity> entities = notificacionRepository.buscarPorTipoSolicitud(tipoSolicitud);
        return entities.stream()
                .map(entity -> SolicitudMapperPersistencia.aDominio(entity))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> buscarPorSolicitud(Integer idSolicitud) {
        List<NotificacionEntity> entities = notificacionRepository.buscarPorSolicitud(idSolicitud);
        return entities.stream()
                .map(entity -> SolicitudMapperPersistencia.aDominio(entity))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarNoLeidasPorUsuario(Integer idUsuario) {
        return notificacionRepository.contarNoLeidasPorUsuario(idUsuario);
    }

    @Override
    @Transactional
    public void marcarTodasComoLeidas(Integer idUsuario) {
        notificacionRepository.marcarTodasComoLeidas(idUsuario);
    }

    @Override
    @Transactional
    public void marcarComoLeida(Integer idNotificacion) {
        notificacionRepository.marcarComoLeida(idNotificacion);
    }

}

