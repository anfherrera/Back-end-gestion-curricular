package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudPazYSalvoEntity;

public interface SolicitudPazYSalvoRepositoryInt extends JpaRepository<SolicitudPazYSalvoEntity, Integer> {

    /**
     * Busca las solicitudes de paz y salvo cuyo último estado sea "Enviada"
     * @return Lista de solicitudes con último estado "Enviada"
     */
    @Query("SELECT s FROM SolicitudPazYSalvoEntity s " +
           "WHERE s.id_solicitud IN (" +
           "    SELECT e.objSolicitud.id_solicitud " +
           "    FROM EstadoSolicitudEntity e " +
           "    WHERE e.estado_actual = :estado " +
           "    AND e.fecha_registro_estado = (" +
           "        SELECT MAX(e2.fecha_registro_estado) " +
           "        FROM EstadoSolicitudEntity e2 " +
           "        WHERE e2.objSolicitud.id_solicitud = e.objSolicitud.id_solicitud" +
           "    )" +
           ")")
    List<SolicitudPazYSalvoEntity> findByUltimoEstado(@Param("estado") String estado);

    /**
     * Busca las solicitudes de paz y salvo cuyo último estado sea el especificado
     * y que pertenezcan a un período académico específico
     * @param estado Estado de la solicitud
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes filtradas por estado y período académico
     */
    @Query("SELECT s FROM SolicitudPazYSalvoEntity s " +
           "WHERE (:periodoAcademico IS NULL OR s.periodo_academico = :periodoAcademico) " +
           "AND s.id_solicitud IN (" +
           "    SELECT e.objSolicitud.id_solicitud " +
           "    FROM EstadoSolicitudEntity e " +
           "    WHERE e.estado_actual = :estado " +
           "    AND e.fecha_registro_estado = (" +
           "        SELECT MAX(e2.fecha_registro_estado) " +
           "        FROM EstadoSolicitudEntity e2 " +
           "        WHERE e2.objSolicitud.id_solicitud = e.objSolicitud.id_solicitud" +
           "    )" +
           ")")
    List<SolicitudPazYSalvoEntity> findByUltimoEstadoAndPeriodoAcademico(
        @Param("estado") String estado, 
        @Param("periodoAcademico") String periodoAcademico
    );

    /**
     * Busca las solicitudes de paz y salvo cuyo último estado sea el especificado
     * y que pertenezcan a un programa académico específico
     * @param estado Estado de la solicitud
     * @param idPrograma ID del programa académico
     * @return Lista de solicitudes filtradas por estado y programa
     */
    @Query("SELECT s FROM SolicitudPazYSalvoEntity s " +
           "WHERE s.objUsuario.objPrograma.id_programa = :idPrograma " +
           "AND s.id_solicitud IN (" +
           "    SELECT e.objSolicitud.id_solicitud " +
           "    FROM EstadoSolicitudEntity e " +
           "    WHERE e.estado_actual = :estado " +
           "    AND e.fecha_registro_estado = (" +
           "        SELECT MAX(e2.fecha_registro_estado) " +
           "        FROM EstadoSolicitudEntity e2 " +
           "        WHERE e2.objSolicitud.id_solicitud = e.objSolicitud.id_solicitud" +
           "    )" +
           ")")
    List<SolicitudPazYSalvoEntity> findByUltimoEstadoAndPrograma(@Param("estado") String estado, @Param("idPrograma") Integer idPrograma);

    /**
     * Busca las solicitudes de paz y salvo cuyo último estado sea el especificado
     * y que pertenezcan a un programa académico específico y período académico
     * @param estado Estado de la solicitud
     * @param idPrograma ID del programa académico
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes filtradas por estado, programa y período académico
     */
    @Query("SELECT s FROM SolicitudPazYSalvoEntity s " +
           "WHERE s.objUsuario.objPrograma.id_programa = :idPrograma " +
           "AND (:periodoAcademico IS NULL OR s.periodo_academico = :periodoAcademico) " +
           "AND s.id_solicitud IN (" +
           "    SELECT e.objSolicitud.id_solicitud " +
           "    FROM EstadoSolicitudEntity e " +
           "    WHERE e.estado_actual = :estado " +
           "    AND e.fecha_registro_estado = (" +
           "        SELECT MAX(e2.fecha_registro_estado) " +
           "        FROM EstadoSolicitudEntity e2 " +
           "        WHERE e2.objSolicitud.id_solicitud = e.objSolicitud.id_solicitud" +
           "    )" +
           ")")
    List<SolicitudPazYSalvoEntity> findByUltimoEstadoAndProgramaAndPeriodoAcademico(
        @Param("estado") String estado, 
        @Param("idPrograma") Integer idPrograma,
        @Param("periodoAcademico") String periodoAcademico
    );

    /**
     * Busca las solicitudes de paz y salvo cuyo último estado sea el especificado
     * y que pertenezcan a un usuario específico y período académico
     * @param estado Estado de la solicitud
     * @param idUsuario ID del usuario
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes filtradas por estado, usuario y período académico
     */
    @Query("SELECT s FROM SolicitudPazYSalvoEntity s " +
           "WHERE s.objUsuario.id_usuario = :idUsuario " +
           "AND (:periodoAcademico IS NULL OR s.periodo_academico = :periodoAcademico) " +
           "AND s.id_solicitud IN (" +
           "    SELECT e.objSolicitud.id_solicitud " +
           "    FROM EstadoSolicitudEntity e " +
           "    WHERE e.estado_actual = :estado " +
           "    AND e.fecha_registro_estado = (" +
           "        SELECT MAX(e2.fecha_registro_estado) " +
           "        FROM EstadoSolicitudEntity e2 " +
           "        WHERE e2.objSolicitud.id_solicitud = e.objSolicitud.id_solicitud" +
           "    )" +
           ")")
    List<SolicitudPazYSalvoEntity> findByUltimoEstadoAndUsuarioAndPeriodoAcademico(
        @Param("estado") String estado, 
        @Param("idUsuario") Integer idUsuario,
        @Param("periodoAcademico") String periodoAcademico
    );

    /**
     * Busca una solicitud de paz y salvo por ID con el usuario y sus relaciones cargadas (JOIN FETCH)
     * Nota: Solo se hace JOIN FETCH del usuario y sus relaciones para evitar problemas con múltiples colecciones
     * @param idSolicitud ID de la solicitud
     * @return Optional con la solicitud si existe, vacío si no
     */
    @Query("SELECT s FROM SolicitudPazYSalvoEntity s " +
           "LEFT JOIN FETCH s.objUsuario u " +
           "LEFT JOIN FETCH u.objPrograma p " +
           "LEFT JOIN FETCH u.objRol r " +
           "WHERE s.id_solicitud = :idSolicitud")
    java.util.Optional<SolicitudPazYSalvoEntity> findByIdWithRelations(@Param("idSolicitud") Integer idSolicitud);
}
