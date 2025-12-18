package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.NotificacionEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificacionRepositoryInt extends JpaRepository<NotificacionEntity, Integer> {

    @Query("SELECT DISTINCT n FROM NotificacionEntity n " +
           "LEFT JOIN FETCH n.objUsuario " +
           "WHERE n.objUsuario.id_usuario = :idUsuario " +
           "ORDER BY n.fechaCreacion DESC")
    List<NotificacionEntity> buscarPorUsuario(@Param("idUsuario") Integer idUsuario);

    @Query("SELECT DISTINCT n FROM NotificacionEntity n " +
           "LEFT JOIN FETCH n.objUsuario " +
           "WHERE n.objUsuario.id_usuario = :idUsuario AND n.leida = false " +
           "ORDER BY n.fechaCreacion DESC")
    List<NotificacionEntity> buscarNoLeidasPorUsuario(@Param("idUsuario") Integer idUsuario);

    // Buscar notificaciones urgentes por usuario
    @Query("SELECT n FROM NotificacionEntity n WHERE n.objUsuario.id_usuario = :idUsuario AND n.esUrgente = true ORDER BY n.fechaCreacion DESC")
    List<NotificacionEntity> buscarUrgentesPorUsuario(@Param("idUsuario") Integer idUsuario);

    // Buscar notificaciones por tipo de solicitud
    @Query("SELECT n FROM NotificacionEntity n WHERE n.tipoSolicitud = :tipoSolicitud ORDER BY n.fechaCreacion DESC")
    List<NotificacionEntity> buscarPorTipoSolicitud(@Param("tipoSolicitud") String tipoSolicitud);

    // Buscar notificaciones por solicitud
    @Query("SELECT n FROM NotificacionEntity n WHERE n.objSolicitud.id_solicitud = :idSolicitud ORDER BY n.fechaCreacion DESC")
    List<NotificacionEntity> buscarPorSolicitud(@Param("idSolicitud") Integer idSolicitud);

    // Contar notificaciones no leídas por usuario
    @Query("SELECT COUNT(n) FROM NotificacionEntity n WHERE n.objUsuario.id_usuario = :idUsuario AND n.leida = false")
    Long contarNoLeidasPorUsuario(@Param("idUsuario") Integer idUsuario);

    // Marcar todas las notificaciones de un usuario como leídas
    @Modifying
    @Query("UPDATE NotificacionEntity n SET n.leida = true WHERE n.objUsuario.id_usuario = :idUsuario AND n.leida = false")
    void marcarTodasComoLeidas(@Param("idUsuario") Integer idUsuario);

    // Marcar una notificación específica como leída
    @Modifying
    @Query("UPDATE NotificacionEntity n SET n.leida = true WHERE n.id_notificacion = :idNotificacion")
    void marcarComoLeida(@Param("idNotificacion") Integer idNotificacion);

    // Eliminar notificaciones antiguas (más de X días)
    @Modifying
    @Query("DELETE FROM NotificacionEntity n WHERE n.fechaCreacion < :fechaLimite AND n.leida = true")
    void eliminarAntiguas(@Param("fechaLimite") java.util.Date fechaLimite);
}

