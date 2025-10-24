package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.DocenteEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocenteRepositoryInt extends JpaRepository<DocenteEntity, Integer> {

    @Query(value = "SELECT COUNT(*) FROM docentes WHERE codigo_docente = ?1", nativeQuery = true)
    Integer contarPorCodigo(String codigo);

    @Query("SELECT d FROM DocenteEntity d WHERE LOWER(d.nombre_docente) LIKE LOWER(CONCAT('%', :nombreParcial, '%')) ORDER BY d.nombre_docente ASC")
    List<DocenteEntity> buscarPorNombreParcial(@Param("nombreParcial") String nombreParcial);

    @Query("SELECT d FROM DocenteEntity d WHERE d.codigo_docente = :codigo")
    Optional<DocenteEntity> buscarPorCodigo(@Param("codigo") String codigo);

    @Modifying
    @Query(value = "DELETE FROM Docentes WHERE idDocente = :id", nativeQuery = true)
    void eliminarPorId(@Param("id") Integer id);
}
