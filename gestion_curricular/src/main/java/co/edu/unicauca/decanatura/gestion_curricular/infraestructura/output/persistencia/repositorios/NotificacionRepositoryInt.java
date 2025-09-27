package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.NotificacionEntity;

public interface NotificacionRepositoryInt extends JpaRepository<NotificacionEntity, Integer> {

    // Buscar notificaciones por usuario
    @Query("SELECT n FROM NotificacionEntity n WHERE n.objUsuario.id_usuario = :idUsuario ORDER BY n.fechaCreacion DESC")
    List<NotificacionEntity> buscarPorUsuario(@Param("idUsuario") Integer idUsuario);
    
    // Buscar notificaciones por usuario con paginación
    @Query("SELECT n FROM NotificacionEntity n WHERE n.objUsuario.id_usuario = :idUsuario ORDER BY n.fechaCreacion DESC")
    Page<NotificacionEntity> buscarPorUsuarioPaginado(@Param("idUsuario") Integer idUsuario, Pageable pageable);
    
    // Contar notificaciones no leídas por usuario
    @Query("SELECT COUNT(n) FROM NotificacionEntity n WHERE n.objUsuario.id_usuario = :idUsuario AND n.leida = false")
    Integer contarNoLeidasPorUsuario(@Param("idUsuario") Integer idUsuario);
    
    // Buscar notificaciones no leídas por usuario
    @Query("SELECT n FROM NotificacionEntity n WHERE n.objUsuario.id_usuario = :idUsuario AND n.leida = false ORDER BY n.fechaCreacion DESC")
    List<NotificacionEntity> buscarNoLeidasPorUsuario(@Param("idUsuario") Integer idUsuario);
    
    // Buscar notificaciones por tipo de solicitud
    @Query("SELECT n FROM NotificacionEntity n WHERE n.tipoSolicitud = :tipoSolicitud ORDER BY n.fechaCreacion DESC")
    List<NotificacionEntity> buscarPorTipoSolicitud(@Param("tipoSolicitud") String tipoSolicitud);
    
    // Buscar notificaciones por tipo de notificación
    @Query("SELECT n FROM NotificacionEntity n WHERE n.tipoNotificacion = :tipoNotificacion ORDER BY n.fechaCreacion DESC")
    List<NotificacionEntity> buscarPorTipoNotificacion(@Param("tipoNotificacion") String tipoNotificacion);
    
    // Buscar notificaciones urgentes
    @Query("SELECT n FROM NotificacionEntity n WHERE n.esUrgente = true ORDER BY n.fechaCreacion DESC")
    List<NotificacionEntity> buscarUrgentes();
    
    // Buscar notificaciones por rango de fechas
    @Query("SELECT n FROM NotificacionEntity n WHERE n.fechaCreacion BETWEEN :fechaInicio AND :fechaFin ORDER BY n.fechaCreacion DESC")
    List<NotificacionEntity> buscarPorRangoFechas(@Param("fechaInicio") Date fechaInicio, @Param("fechaFin") Date fechaFin);
    
    // Buscar notificaciones por usuario y tipo de solicitud
    @Query("SELECT n FROM NotificacionEntity n WHERE n.objUsuario.id_usuario = :idUsuario AND n.tipoSolicitud = :tipoSolicitud ORDER BY n.fechaCreacion DESC")
    List<NotificacionEntity> buscarPorUsuarioYTipoSolicitud(@Param("idUsuario") Integer idUsuario, @Param("tipoSolicitud") String tipoSolicitud);
    
    // Buscar notificaciones por solicitud
    @Query("SELECT n FROM NotificacionEntity n WHERE n.objSolicitud.id_solicitud = :idSolicitud ORDER BY n.fechaCreacion DESC")
    List<NotificacionEntity> buscarPorSolicitud(@Param("idSolicitud") Integer idSolicitud);
    
    // Buscar notificaciones por curso
    @Query("SELECT n FROM NotificacionEntity n WHERE n.objCurso.id_curso = :idCurso ORDER BY n.fechaCreacion DESC")
    List<NotificacionEntity> buscarPorCurso(@Param("idCurso") Integer idCurso);
    
    // Estadísticas por tipo de solicitud
    @Query("SELECT n.tipoSolicitud, COUNT(n) FROM NotificacionEntity n GROUP BY n.tipoSolicitud")
    List<Object[]> estadisticasPorTipoSolicitud();
    
    // Estadísticas por tipo de notificación
    @Query("SELECT n.tipoNotificacion, COUNT(n) FROM NotificacionEntity n GROUP BY n.tipoNotificacion")
    List<Object[]> estadisticasPorTipoNotificacion();
    
    // Estadísticas por usuario
    @Query("SELECT n.objUsuario.id_usuario, COUNT(n) FROM NotificacionEntity n GROUP BY n.objUsuario.id_usuario")
    List<Object[]> estadisticasPorUsuario();
    
    // Eliminar notificaciones antiguas (más de X días)
    @Query("DELETE FROM NotificacionEntity n WHERE n.fechaCreacion < :fechaLimite")
    void eliminarAntiguas(@Param("fechaLimite") Date fechaLimite);
}
