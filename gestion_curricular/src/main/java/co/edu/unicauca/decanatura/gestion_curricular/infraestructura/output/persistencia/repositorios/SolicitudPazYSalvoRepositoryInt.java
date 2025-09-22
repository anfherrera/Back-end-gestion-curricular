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
}
