package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.RolEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RolRepositoryInt extends JpaRepository<RolEntity, Integer> {

    // Consulta nativa: contar roles por nombre exacto
    @Query(value = "SELECT COUNT(*) FROM roles WHERE nombre = ?1", nativeQuery = true)
    Integer contarPorNombre(String nombre);

    // Consulta JPQL: buscar roles por nombre parcial (LIKE)
    @Query("SELECT r FROM RolEntity r WHERE LOWER(r.nombre) LIKE LOWER(CONCAT('%', :nombreParcial, '%')) ORDER BY r.nombre ASC")
    Optional<RolEntity> buscarPorNombreParcial(@Param("nombreParcial") String nombreParcial);

    // Consulta JPQL: listar roles con usuarios
    @Query("SELECT DISTINCT r FROM RolEntity r LEFT JOIN FETCH r.usuarios")
    List<RolEntity> listarRolesConUsuarios();
}
