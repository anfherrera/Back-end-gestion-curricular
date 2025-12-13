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
}
