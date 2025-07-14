package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoSolicitudEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface EstadoSolicitudRepositoryInt extends JpaRepository<EstadoSolicitudEntity, Integer> {

    // Consulta JPQL: buscar por estado exacto
    @Query("SELECT e FROM EstadoSolicitudEntity e WHERE e.estado_actual = :estado")
    EstadoSolicitudEntity buscarPorEstado(@Param("estado") String estado);

    // Consulta JPQL: buscar por fecha de registro
    @Query("SELECT e FROM EstadoSolicitudEntity e WHERE e.fecha_registro_estado = :fecha")
    List<EstadoSolicitudEntity> buscarPorFechaRegistro(@Param("fecha") Date fecha);

    // Consulta JPQL: obtener estado por solicitud
    @Query("SELECT e FROM EstadoSolicitudEntity e WHERE e.objSolicitud.id_solicitud = :idSolicitud")
    Optional<EstadoSolicitudEntity> buscarPorSolicitud(@Param("idSolicitud") Integer idSolicitud);

    // Consulta nativa: contar cuántos estados están en un estado específico
    @Query(value = "SELECT COUNT(*) FROM estados WHERE estado_actual = ?1", nativeQuery = true)
    Integer contarPorEstado(String estado);
}
