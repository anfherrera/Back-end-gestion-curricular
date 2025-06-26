package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;

import java.util.Date;
import java.util.List;

public interface SolicitudRepositoryInt extends JpaRepository<SolicitudEntity, Integer>{

    // Consulta nativa: contar solicitudes con cierto nombre
    @Query(value = "SELECT COUNT(*) FROM solicitudes WHERE nombre_solicitud = ?1", nativeQuery = true)
    Integer contarPorNombre(String nombre_solicitud);

    // Consulta JPQL: contar todas las solicitudes
    @Query("SELECT COUNT(s) FROM SolicitudEntity s")
    Integer totalSolicitudes();

    // Consulta JPQL: buscar entre un rango de fechas
    @Query("SELECT s FROM SolicitudEntity s WHERE s.fecha_registro_solicitud BETWEEN :fechaInicio AND :fechaFin")
    List<SolicitudEntity> buscarPorRangoFechas(@Param("fechaInicio") Date fechaInicio, @Param("fechaFin") Date fechaFin);


}