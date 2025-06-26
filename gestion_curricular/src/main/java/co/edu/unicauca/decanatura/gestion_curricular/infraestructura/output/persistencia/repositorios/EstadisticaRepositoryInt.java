package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadisticaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface EstadisticaRepositoryInt extends JpaRepository<EstadisticaEntity, Integer> {

    // Consulta JPQL: buscar estadísticas por periodo académico
    @Query("SELECT e FROM EstadisticaEntity e WHERE e.periodo_academico = :periodo")
    List<EstadisticaEntity> buscarPorPeriodo(@Param("periodo") Date periodo);

    // Consulta JPQL: estadísticas por programa
    @Query("SELECT e FROM EstadisticaEntity e WHERE e.objPrograma.id_programa = :idPrograma")
    List<EstadisticaEntity> buscarPorPrograma(@Param("idPrograma") Integer idPrograma);

    // Consulta JPQL: estadísticas por solicitud
    @Query("SELECT e FROM EstadisticaEntity e WHERE e.objSolicitud.id_solicitud = :idSolicitud")
    List<EstadisticaEntity> buscarPorSolicitud(@Param("idSolicitud") Integer idSolicitud);

    // Consulta nativa: contar total de registros por nombre de estadística
    @Query(value = "SELECT COUNT(*) FROM estadisticas WHERE nombre = ?1", nativeQuery = true)
    Integer contarPorNombre(String nombre);

    // Consulta nativa: buscar estadísticas por rango de fechas
    @Query("SELECT e FROM EstadisticaEntity e WHERE e.periodo_academico BETWEEN :fechaInicio AND :fechaFin")
    List<EstadisticaEntity> buscarPorRangoFechas(@Param("fechaInicio") Date fechaInicio, @Param("fechaFin") Date fechaFin);

}
