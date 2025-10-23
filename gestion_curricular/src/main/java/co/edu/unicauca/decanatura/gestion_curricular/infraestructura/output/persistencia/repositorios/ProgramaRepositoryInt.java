package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProgramaRepositoryInt extends JpaRepository<ProgramaEntity, Integer> {

    @Query(value = "SELECT COUNT(*) FROM programas WHERE codigo = ?1", nativeQuery = true)
    Integer contarPorCodigo(String codigo);

    @Query("SELECT p FROM ProgramaEntity p WHERE LOWER(p.nombre_programa) LIKE LOWER(CONCAT('%', :nombreParcial, '%')) ORDER BY p.nombre_programa ASC")
    List<ProgramaEntity> buscarPorNombreParcial(@Param("nombreParcial") String nombreParcial);

    @Query("SELECT p FROM ProgramaEntity p WHERE p.codigo = :codigo")
    Optional<ProgramaEntity> buscarPorCodigo(@Param("codigo") String codigo);

    @Modifying
    @Query("DELETE FROM ProgramaEntity p WHERE p.id_programa = :id")
    void eliminarPorId(@Param("id") Integer id);

    @Query(value = "SELECT nombre_programa FROM programas", nativeQuery = true)
    List<String> buscarNombresProgramas();
}
