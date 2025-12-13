package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEcaesEntity;

public interface SolicitudEcaesRepositoryInt extends JpaRepository<SolicitudEcaesEntity, Integer> {

    // Consulta nativa: contar solicitudes por número de documento exacto
    @Query(value = "SELECT COUNT(*) FROM solicitudes_ecaes WHERE numero_documento = ?1", nativeQuery = true)
    Integer contarPorNumeroDocumento(String numeroDocumento);

    // Consulta JPQL: buscar solicitudes por número de documento parcial (LIKE)
    @Query("SELECT s FROM SolicitudEcaesEntity s WHERE LOWER(s.numero_documento) LIKE LOWER(CONCAT('%', :numeroDocumentoParcial, '%')) ORDER BY s.numero_documento ASC")
    List<SolicitudEcaesEntity> buscarPorNumeroDocumentoParcial(@Param("numeroDocumentoParcial") String numeroDocumentoParcial);

    // Consulta JPQL: listar solicitudes con usuarios
    @Query("SELECT DISTINCT s FROM SolicitudEcaesEntity s LEFT JOIN FETCH s.objUsuario")
    List<SolicitudEcaesEntity> listarSolicitudesConUsuarios();

    /**
     * Busca las solicitudes Ecaes cuyo último estado sea "--"
     * @return Lista de solicitudes con último estado "--"
     */
    @Query("SELECT s FROM SolicitudEcaesEntity s " +
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
    List<SolicitudEcaesEntity> findByUltimoEstado(@Param("estado") String estado);

    /**
     * Busca las solicitudes ECAES cuyo último estado sea el especificado
     * y que pertenezcan a un programa académico específico
     */
    @Query("SELECT s FROM SolicitudEcaesEntity s " +
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
    List<SolicitudEcaesEntity> findByUltimoEstadoAndPrograma(@Param("estado") String estado, @Param("idPrograma") Integer idPrograma);

}
