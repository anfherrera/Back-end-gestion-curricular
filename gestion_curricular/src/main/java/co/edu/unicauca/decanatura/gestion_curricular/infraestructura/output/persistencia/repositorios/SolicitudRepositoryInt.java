package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface SolicitudRepositoryInt extends JpaRepository<SolicitudEntity, Integer>{

    // Se utiliza set para obtener nombres de los procesos/solicitudes sin duplicados
    @Query("SELECT s.nombre_solicitud FROM SolicitudEntity s")
    Set<String> buscarNombresSolicitudes();
    
    // Consulta nativa: contar solicitudes con cierto nombre
    @Query("SELECT COUNT(s) FROM SolicitudEntity s WHERE LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    Integer contarPorNombre(@Param("nombre") String nombre);

    // Consulta JPQL: contar todas las solicitudes
    @Query("SELECT COUNT(s) FROM SolicitudEntity s")
    Integer totalSolicitudes();

    // Consulta JPQL: buscar solicitudes por estado
    @Query("SELECT COUNT(s) FROM SolicitudEntity s WHERE s.objEstadoSolicitud.id_estado = :idEstado")
    Integer contarPorEstado(@Param("idEstado") Integer idEstado);


    @Query("SELECT COUNT(s) FROM SolicitudEntity s WHERE s.fecha_registro_solicitud BETWEEN :fechaInicio AND :fechaFin")
    Integer contarPorRangoFechas(@Param("fechaInicio") Date fechaInicio, @Param("fechaFin") Date fechaFin);
    
    @Query(value =
    "SELECT COUNT(*) FROM (" +
    "SELECT s.nombre_solicitud AS nombreSolicitud, " +
    "s.fecha_registro_solicitud AS fechaRegistro," +
    "u.idfkPrograma AS idPrograma " +
    "FROM solicitudes s " +
    "LEFT JOIN estados e ON e.idfkSolicitud = s.idSolicitud " +
    "LEFT JOIN usuariosolicitudes us ON s.idSolicitud = us.idSolicitud " +
    "LEFT JOIN usuarios u ON u.idUsuario = us.idUsuario " +

    "UNION " +

    "SELECT s.nombre_solicitud AS nombreSolicitud, " +
    "s.fecha_registro_solicitud AS fechaRegistro," +
    "u.idfkPrograma AS idPrograma " +
    "FROM usuarios u " +
    "LEFT JOIN usuariosolicitudes us ON u.idUsuario = us.idUsuario " +
    "LEFT JOIN solicitudes s ON s.idSolicitud = us.idSolicitud " +
    "LEFT JOIN estados e ON e.idfkSolicitud = s.idSolicitud" +
    ") AS union_resultado " +
    "WHERE LOWER(nombreSolicitud) LIKE LOWER(CONCAT('%', :nombreSolicitud, '%')) " +
    "AND fechaRegistro BETWEEN :fechaInicio AND :fechaFin " +
    "AND idPrograma = :idPrograma",
    nativeQuery = true)

    Integer contarNombreFechaYPrograma(
    @Param("nombreSolicitud") String nombreSolicitud,
    @Param("fechaInicio") Date fechaInicio, @Param("fechaFin") Date fechaFin,
    @Param("idPrograma") Integer idPrograma);

    @Query(value =
    "SELECT COUNT(*) FROM (" +
    "SELECT s.nombre_solicitud AS nombreSolicitud, " +
    "s.fecha_registro_solicitud AS fechaRegistro, e.idEstado AS idEstado, " +
    "u.idfkPrograma AS idPrograma " +
    "FROM solicitudes s " +
    "LEFT JOIN estados e ON e.idfkSolicitud = s.idSolicitud " +
    "LEFT JOIN usuariosolicitudes us ON s.idSolicitud = us.idSolicitud " +
    "LEFT JOIN usuarios u ON u.idUsuario = us.idUsuario " +

    "UNION " +

    "SELECT s.nombre_solicitud AS nombreSolicitud, " +
    "s.fecha_registro_solicitud AS fechaRegistro, e.idEstado AS idEstado, " +
    "u.idfkPrograma AS idPrograma " +
    "FROM usuarios u " +
    "LEFT JOIN usuariosolicitudes us ON u.idUsuario = us.idUsuario " +
    "LEFT JOIN solicitudes s ON s.idSolicitud = us.idSolicitud " +
    "LEFT JOIN estados e ON e.idfkSolicitud = s.idSolicitud" +
    ") AS union_resultado " +
    "WHERE LOWER(nombreSolicitud) LIKE LOWER(CONCAT('%', :nombreSolicitud, '%')) " +
    "AND fechaRegistro BETWEEN :fechaInicio AND :fechaFin " +
    "AND idEstado = :idEstado " +
    "AND idPrograma = :idPrograma",
    nativeQuery = true)

    Integer contarNombreFechaEstadoYPrograma(
    @Param("nombreSolicitud") String nombreSolicitud,
    @Param("fechaInicio") Date fechaInicio, @Param("fechaFin") Date fechaFin,
    @Param("idEstado") Integer idEstado,
    @Param("idPrograma") Integer idPrograma);

    // Consulta JPQL: buscar entre un rango de fechas
    @Query("SELECT s FROM SolicitudEntity s WHERE s.fecha_registro_solicitud BETWEEN :fechaInicio AND :fechaFin")
    List<SolicitudEntity> buscarPorRangoFechas(@Param("fechaInicio") Date fechaInicio, @Param("fechaFin") Date fechaFin);

    @Query("SELECT s FROM SolicitudEntity s WHERE LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<SolicitudEntity> buscarPorNombre(@Param("nombre") String nombre);

    @Query(value = "SELECT * FROM usuariosolicitudes us LEFT JOIN Solicitudes ON us.idSolicitud = Solicitudes.idSolicitud WHERE us.IdUsuario = :IdUsuario", nativeQuery = true)
    List<SolicitudEntity> buscarSolicitudesPorUsuario(@Param("IdUsuario") Integer IdUsuario);

}