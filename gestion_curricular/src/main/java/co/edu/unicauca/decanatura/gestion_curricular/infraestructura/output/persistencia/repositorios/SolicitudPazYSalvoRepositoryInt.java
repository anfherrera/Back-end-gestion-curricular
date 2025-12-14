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
     * Busca las solicitudes de paz y salvo que han sido procesadas (tienen más de un estado o estado diferente a "Enviada")
     * Una solicitud se considera procesada si ha pasado por al menos un cambio de estado desde "Enviada"
     * @return Lista de solicitudes procesadas
     */
    @Query("SELECT DISTINCT s FROM SolicitudPazYSalvoEntity s " +
           "WHERE s.id_solicitud IN (" +
           "    SELECT DISTINCT e.objSolicitud.id_solicitud " +
           "    FROM EstadoSolicitudEntity e " +
           "    WHERE e.objSolicitud.id_solicitud = s.id_solicitud " +
           "    AND (e.estado_actual != 'Enviada' " +
           "         OR (SELECT COUNT(e2) FROM EstadoSolicitudEntity e2 WHERE e2.objSolicitud.id_solicitud = s.id_solicitud) > 1)" +
           ")")
    List<SolicitudPazYSalvoEntity> findSolicitudesProcesadas();

    /**
     * Busca las solicitudes de paz y salvo procesadas filtradas por período académico
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes procesadas filtradas por período académico
     */
    @Query("SELECT DISTINCT s FROM SolicitudPazYSalvoEntity s " +
           "WHERE (:periodoAcademico IS NULL OR s.periodo_academico = :periodoAcademico) " +
           "AND s.id_solicitud IN (" +
           "    SELECT DISTINCT e.objSolicitud.id_solicitud " +
           "    FROM EstadoSolicitudEntity e " +
           "    WHERE e.objSolicitud.id_solicitud = s.id_solicitud " +
           "    AND (e.estado_actual != 'Enviada' " +
           "         OR (SELECT COUNT(e2) FROM EstadoSolicitudEntity e2 WHERE e2.objSolicitud.id_solicitud = s.id_solicitud) > 1)" +
           ")")
    List<SolicitudPazYSalvoEntity> findSolicitudesProcesadasPorPeriodo(@Param("periodoAcademico") String periodoAcademico);

    /**
     * Busca las solicitudes de paz y salvo procesadas filtradas por programa y período académico
     * @param idPrograma ID del programa académico
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes procesadas filtradas por programa y período académico
     */
    @Query("SELECT DISTINCT s FROM SolicitudPazYSalvoEntity s " +
           "WHERE s.objUsuario.objPrograma.id_programa = :idPrograma " +
           "AND (:periodoAcademico IS NULL OR s.periodo_academico = :periodoAcademico) " +
           "AND s.id_solicitud IN (" +
           "    SELECT DISTINCT e.objSolicitud.id_solicitud " +
           "    FROM EstadoSolicitudEntity e " +
           "    WHERE e.objSolicitud.id_solicitud = s.id_solicitud " +
           "    AND (e.estado_actual != 'Enviada' " +
           "         OR (SELECT COUNT(e2) FROM EstadoSolicitudEntity e2 WHERE e2.objSolicitud.id_solicitud = s.id_solicitud) > 1)" +
           ")")
    List<SolicitudPazYSalvoEntity> findSolicitudesProcesadasPorProgramaYPeriodo(
        @Param("idPrograma") Integer idPrograma,
        @Param("periodoAcademico") String periodoAcademico
    );
}
