package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.CursoOfertadoVeranoEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
public interface CursoOfertadoVeranoRepositoryInt extends JpaRepository<CursoOfertadoVeranoEntity, Integer> {

    // Consulta JPQL: listar cursos por grupo
    @Query("SELECT c FROM CursoOfertadoVeranoEntity c WHERE c.grupo = :grupo")
    List<CursoOfertadoVeranoEntity> buscarPorGrupo(@Param("grupo") co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.Enums.GrupoCursoVeranoEntity grupo);

    // Consulta JPQL: listar cursos por materia
    @Query("SELECT c FROM CursoOfertadoVeranoEntity c WHERE c.objMateria.id_materia = :idMateria")
    List<CursoOfertadoVeranoEntity> buscarPorMateria(@Param("idMateria") Integer idMateria);

    // Consulta JPQL: listar cursos por docente
    @Query("SELECT c FROM CursoOfertadoVeranoEntity c WHERE c.objDocente.id_docente = :idDocente")
    List<CursoOfertadoVeranoEntity> buscarPorDocente(@Param("idDocente") Integer idDocente);

    // Consulta nativa: contar cursos por salón específico
    @Query(value = "SELECT COUNT(*) FROM cursos_ofertados WHERE salon = ?1", nativeQuery = true)
    Integer contarPorSalon(String salon);
    
    @Modifying
    @Query("DELETE FROM CursoOfertadoVeranoEntity c WHERE c.id = :id")
    void eliminarPorId(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO cursosestudiantes (idCurso, idUsuario) VALUES (:idCurso, :idUsuario)", nativeQuery = true)
    int insertarCursoEstudiante(@Param("idCurso") Integer idCurso, @Param("idUsuario") Integer idUsuario);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM cursosestudiantes WHERE idCurso = :idCurso AND idUsuario = :idUsuario", nativeQuery = true)
    int eliminarEstudianteDeCurso(@Param("idCurso") Integer idCurso, @Param("idUsuario") Integer idUsuario);

    // Consulta JPQL: verificar si existe un curso con la misma materia, docente, período académico y grupo
    @Query("SELECT c FROM CursoOfertadoVeranoEntity c WHERE c.objMateria.id_materia = :idMateria AND c.objDocente.id_docente = :idDocente AND c.periodo_academico = :periodoAcademico AND c.grupo = :grupo")
    List<CursoOfertadoVeranoEntity> buscarPorMateriaDocentePeriodoGrupo(@Param("idMateria") Integer idMateria, @Param("idDocente") Integer idDocente, @Param("periodoAcademico") String periodoAcademico, @Param("grupo") co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.Enums.GrupoCursoVeranoEntity grupo);
    
    // Consulta JPQL: traer un curso por ID con los estados cargados (evita LazyInitializationException)
    @Query("""
        SELECT c FROM CursoOfertadoVeranoEntity c
        LEFT JOIN FETCH c.estadosCursoOfertados
        WHERE c.id_curso = :id
    """)
    CursoOfertadoVeranoEntity findByIdConEstados(@Param("id") Integer id);

}

