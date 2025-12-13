package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudHomologacionEntity;

public interface SolicitudHomologacionRepositoryInt extends JpaRepository<SolicitudHomologacionEntity, Integer> {

    /**
     * Busca las solicitudes de Homologacion cuyo último estado sea "--"
     * @return Lista de solicitudes con último estado "--"
     */
    @Query("SELECT s FROM SolicitudHomologacionEntity s " +
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
    List<SolicitudHomologacionEntity> findByUltimoEstado(@Param("estado") String estado);

    /**
     * Busca las solicitudes de homologación cuyo último estado sea el especificado
     * y que pertenezcan a un programa académico específico
     */
    @Query("SELECT s FROM SolicitudHomologacionEntity s " +
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
    List<SolicitudHomologacionEntity> findByUltimoEstadoAndPrograma(@Param("estado") String estado, @Param("idPrograma") Integer idPrograma);
}
