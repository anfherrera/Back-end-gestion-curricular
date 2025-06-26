package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.CursoOfertadoVeranoEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
public interface CursoOfertadoVeranoRepositoryInt extends JpaRepository<CursoOfertadoVeranoEntity, Integer> {

    // Consulta JPQL: listar cursos por grupo
    @Query("SELECT c FROM CursoOfertadoVeranoEntity c WHERE c.grupo = :grupo")
    List<CursoOfertadoVeranoEntity> buscarPorGrupo(@Param("grupo") co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.Enums.GrupoCursoVerano grupo);

    // Consulta JPQL: listar cursos por materia
    @Query("SELECT c FROM CursoOfertadoVeranoEntity c WHERE c.objMateria.id_materia = :idMateria")
    List<CursoOfertadoVeranoEntity> buscarPorMateria(@Param("idMateria") Integer idMateria);

    // Consulta JPQL: listar cursos por docente
    @Query("SELECT c FROM CursoOfertadoVeranoEntity c WHERE c.objDocente.id_docente = :idDocente")
    List<CursoOfertadoVeranoEntity> buscarPorDocente(@Param("idDocente") Integer idDocente);

    // Consulta nativa: contar cursos por salón específico
    @Query(value = "SELECT COUNT(*) FROM cursos_ofertados WHERE salon = ?1", nativeQuery = true)
    Integer contarPorSalon(String salon);
}
