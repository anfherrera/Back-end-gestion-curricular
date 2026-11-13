package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudReingresoEntity;

public interface SolicitudReingresoRepositoryInt extends JpaRepository<SolicitudReingresoEntity, Integer> {

    /**
     * Busca las solicitudes de reingreso cuyo último estado sea el especificado
     * y que pertenezcan a un período académico específico (todos si es null)
     */
    @Query("SELECT s FROM SolicitudReingresoEntity s " +
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
    List<SolicitudReingresoEntity> findByUltimoEstadoAndPeriodoAcademico(
        @Param("estado") String estado, 
        @Param("periodoAcademico") String periodoAcademico
    );

    /**
     * Busca las solicitudes de reingreso cuyo último estado sea el especificado
     * y que pertenezcan a un programa académico específico y período académico (todos si es null)
     */
    @Query("SELECT s FROM SolicitudReingresoEntity s " +
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
    List<SolicitudReingresoEntity> findByUltimoEstadoAndProgramaAndPeriodoAcademico(
        @Param("estado") String estado, 
        @Param("idPrograma") Integer idPrograma,
        @Param("periodoAcademico") String periodoAcademico
    );

    /**
     * Busca solicitudes que hayan tenido un estado específico en su historial (no solo como último estado)
     * Útil para historiales de funcionarios/coordinadores/secretaria
     */
    @Query("SELECT DISTINCT s FROM SolicitudReingresoEntity s " +
           "WHERE (:periodoAcademico IS NULL OR s.periodo_academico = :periodoAcademico) " +
           "AND s.id_solicitud IN (" +
           "    SELECT e.objSolicitud.id_solicitud " +
           "    FROM EstadoSolicitudEntity e " +
           "    WHERE e.estado_actual = :estado" +
           ")")
    List<SolicitudReingresoEntity> findByEstadoEnHistorialAndPeriodoAcademico(
        @Param("estado") String estado,
        @Param("periodoAcademico") String periodoAcademico
    );

    /**
     * Busca solicitudes que hayan tenido un estado específico en su historial, filtradas por programa
     */
    @Query("SELECT DISTINCT s FROM SolicitudReingresoEntity s " +
           "WHERE s.objUsuario.objPrograma.id_programa = :idPrograma " +
           "AND (:periodoAcademico IS NULL OR s.periodo_academico = :periodoAcademico) " +
           "AND s.id_solicitud IN (" +
           "    SELECT e.objSolicitud.id_solicitud " +
           "    FROM EstadoSolicitudEntity e " +
           "    WHERE e.estado_actual = :estado" +
           ")")
    List<SolicitudReingresoEntity> findByEstadoEnHistorialAndProgramaAndPeriodoAcademico(
        @Param("estado") String estado,
        @Param("idPrograma") Integer idPrograma,
        @Param("periodoAcademico") String periodoAcademico
    );

    /** Solicitudes del estudiante, del periodo académico indicado o de todos si es null. */
    @Query("SELECT s FROM SolicitudReingresoEntity s " +
           "WHERE s.objUsuario.id_usuario = :idUsuario " +
           "AND (:periodoAcademico IS NULL OR s.periodo_academico = :periodoAcademico) " +
           "ORDER BY s.id_solicitud")
    List<SolicitudReingresoEntity> findByUsuarioYPeriodo(@Param("idUsuario") Integer idUsuario,
            @Param("periodoAcademico") String periodoAcademico);
}
