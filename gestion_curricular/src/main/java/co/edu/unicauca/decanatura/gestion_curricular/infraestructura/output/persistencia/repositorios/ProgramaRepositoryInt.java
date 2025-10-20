package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ProgramaRepositoryInt extends JpaRepository<ProgramaEntity, Integer> {

    // Se utiliza set para obtener nombres de los programas sin duplicados
    @Query("SELECT p.nombre_programa FROM ProgramaEntity p")
    Set<String> buscarNombresProgramas();

    // Consulta nativa: contar programas por código
    @Query(value = "SELECT COUNT(*) FROM programas WHERE codigo = ?1", nativeQuery = true)
    Integer contarPorCodigo(String codigo);

    // Consulta JPQL: buscar programas por nombre parcial
    @Query("SELECT p FROM ProgramaEntity p WHERE LOWER(p.nombre_programa) LIKE LOWER(CONCAT('%', :nombreParcial, '%')) ORDER BY p.nombre_programa ASC")
    ProgramaEntity buscarPorNombreParcial(@Param("nombreParcial") String nombreParcial);

    // Consulta JPQL: obtener todos los programas con sus usuarios (LEFT JOIN FETCH para evitar lazy loading)
    @Query("SELECT DISTINCT p FROM ProgramaEntity p LEFT JOIN FETCH p.usuarios")
    List<ProgramaEntity> listarConUsuarios();
}
