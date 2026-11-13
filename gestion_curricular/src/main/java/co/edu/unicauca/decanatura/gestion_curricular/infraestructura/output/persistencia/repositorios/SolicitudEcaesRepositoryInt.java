package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEcaesEntity;

public interface SolicitudEcaesRepositoryInt extends JpaRepository<SolicitudEcaesEntity, Integer> {

    // Consulta JPQL: listar solicitudes con usuarios
    @Query("SELECT DISTINCT s FROM SolicitudEcaesEntity s LEFT JOIN FETCH s.objUsuario")
    List<SolicitudEcaesEntity> listarSolicitudesConUsuarios();

    /**
     * Busca las solicitudes ECAES cuyo último estado sea el especificado
     * y que pertenezcan a un período académico específico
     */
    @Query("SELECT s FROM SolicitudEcaesEntity s " +
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
    List<SolicitudEcaesEntity> findByUltimoEstadoAndPeriodoAcademico(
        @Param("estado") String estado, 
        @Param("periodoAcademico") String periodoAcademico
    );

    /**
     * Busca las solicitudes ECAES cuyo último estado sea el especificado
     * y que pertenezcan a un programa académico específico y período académico
     */
    @Query("SELECT s FROM SolicitudEcaesEntity s " +
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
    List<SolicitudEcaesEntity> findByUltimoEstadoAndProgramaAndPeriodoAcademico(
        @Param("estado") String estado,
        @Param("idPrograma") Integer idPrograma,
        @Param("periodoAcademico") String periodoAcademico
    );

    /** Solicitudes del estudiante, del periodo académico indicado o de todos si es null. */
    @Query("SELECT s FROM SolicitudEcaesEntity s " +
           "WHERE s.objUsuario.id_usuario = :idUsuario " +
           "AND (:periodoAcademico IS NULL OR s.periodo_academico = :periodoAcademico) " +
           "ORDER BY s.id_solicitud")
    List<SolicitudEcaesEntity> findByUsuarioYPeriodo(@Param("idUsuario") Integer idUsuario,
            @Param("periodoAcademico") String periodoAcademico);
}
