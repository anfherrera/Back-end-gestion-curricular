package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.DocenteEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocenteRepositoryInt extends JpaRepository<DocenteEntity, Integer> {

    // Consulta nativa: verificar si un docente existe por su código
    @Query(value = "SELECT COUNT(*) FROM docentes WHERE codigo_docente = ?1", nativeQuery = true)
    Integer contarPorCodigo(String codigo_docente);

    // Consulta JPQL: buscar docente por nombre parcial
    @Query("SELECT d FROM DocenteEntity d WHERE LOWER(d.nombre_docente) LIKE LOWER(CONCAT('%', :nombre, '%')) ORDER BY d.nombre_docente ASC")
    List<DocenteEntity> buscarPorNombreParcial(@Param("nombre") String nombre);

    // Consulta JPQL: buscar docente por código exacto
    @Query("SELECT d FROM DocenteEntity d WHERE d.codigo_docente = :codigo")
    DocenteEntity buscarPorCodigoExacto(@Param("codigo") String codigo);
}
