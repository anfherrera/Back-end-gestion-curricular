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


}
//reviar si las consultas estan correctas 