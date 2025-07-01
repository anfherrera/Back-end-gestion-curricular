package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.MateriaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MateriaRepositoryInt extends JpaRepository<MateriaEntity, Integer> {

    // Consulta nativa: verificar si existe una materia con cierto código
    @Query(value = "SELECT COUNT(*) FROM materias WHERE codigo = ?1", nativeQuery = true)
    Integer contarPorCodigo(String codigo);

    // Consulta JPQL: buscar materias por nombre parcial
    @Query("SELECT m FROM MateriaEntity m WHERE LOWER(m.nombre) LIKE LOWER(CONCAT('%', :nombreParcial, '%')) ORDER BY m.nombre ASC")
    List<MateriaEntity> buscarPorNombreParcial(@Param("nombreParcial") String nombreParcial);

    // Consulta JPQL: listar materias por número exacto de créditos
    @Query("SELECT m FROM MateriaEntity m WHERE m.creditos = :creditos")
    List<MateriaEntity> buscarPorCreditos(@Param("creditos") Integer creditos);

    @Query("SELECT m FROM MateriaEntity m WHERE m.codigo = :codigo")
    Optional<MateriaEntity> buscarPorCodigo(@Param("codigo") String codigo);
}
