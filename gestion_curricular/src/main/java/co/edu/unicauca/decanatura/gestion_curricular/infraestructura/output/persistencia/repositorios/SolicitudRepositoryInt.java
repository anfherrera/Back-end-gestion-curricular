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
    @Query("SELECT COUNT(s) FROM SolicitudEntity s WHERE LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombre, '%')) ")
    Integer contarPorNombre(@Param("nombre") String nombre);

    @Query("SELECT COUNT(s) FROM SolicitudCursoVeranoInscripcionEntity s WHERE LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
    "AND s.objCursoOfertado.id_curso = :idCurso")
    Integer contarPorNombreYCursoIns(@Param("nombre") String nombre, @Param("idCurso") Integer idCurso);

    @Query("SELECT COUNT(s) FROM SolicitudCursoVeranoInscripcionEntity s WHERE LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
    "AND s.objCursoOfertado.id_curso = :idCurso " +
    "AND LOWER(s.objEstadoSolicitud.estado_actual) LIKE LOWER(CONCAT('%', :estado_actual, '%'))")
    Integer contarPorNombreCursoEstadoIns(@Param("nombre") String nombre, @Param("idCurso") Integer idCurso, @Param("estado_actual") String estado_actual);

    @Query("SELECT s FROM SolicitudCursoVeranoInscripcionEntity s WHERE LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
    "AND s.objCursoOfertado.id_curso = :idCurso")
    List<SolicitudEntity> buscarPorNombreYCursoIns(@Param("nombre") String nombre, @Param("idCurso") Integer idCurso);

    @Query("SELECT COUNT(s) FROM SolicitudCursoVeranoPreinscripcionEntity s WHERE LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
    "AND s.objCursoOfertado.id_curso = :idCurso")
    Integer contarPorNombreYCursoPre(@Param("nombre") String nombre, @Param("idCurso") Integer idCurso);

    @Query("SELECT COUNT(s) FROM SolicitudCursoVeranoPreinscripcionEntity s WHERE LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
    "AND s.objCursoOfertado.id_curso = :idCurso " +
    "AND LOWER(s.objEstadoSolicitud.estado_actual) LIKE LOWER(CONCAT('%', :estado_actual, '%'))")
    Integer contarPorNombreCursoEstadoPre(@Param("nombre") String nombre, @Param("idCurso") Integer idCurso, @Param("estado_actual") String estado_actual);

    @Query("SELECT s FROM SolicitudCursoVeranoPreinscripcionEntity s WHERE LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
    "AND s.objCursoOfertado.id_curso = :idCurso")
    List<SolicitudEntity> buscarPorNombreYCursoPre(@Param("nombre") String nombre, @Param("idCurso") Integer idCurso);

    @Query("SELECT s FROM SolicitudCursoVeranoInscripcionEntity s WHERE LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
    "AND s.objCursoOfertado.id_curso = :idCurso " +
    "AND LOWER(s.objEstadoSolicitud.estado_actual) LIKE LOWER(CONCAT('%', :estado_actual, '%')) ")
    List<SolicitudEntity> buscarPorNombreCursoEstadoIns(@Param("nombre") String nombre, @Param("idCurso") Integer idCurso, @Param("estado_actual") String estado_actual);

    @Query("SELECT s FROM SolicitudCursoVeranoInscripcionEntity s WHERE LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
    "AND s.objCursoOfertado.id_curso = :idCurso " +
    "AND LOWER(s.objEstadoSolicitud.estado_actual) LIKE LOWER(CONCAT('%', :estado_actual, '%')) ")
    List<SolicitudEntity> buscarPorNombreCursoEstadoPre(@Param("nombre") String nombre, @Param("idCurso") Integer idCurso, @Param("estado_actual") String estado_actual);

    // Consulta JPQL: contar todas las solicitudes
    @Query("SELECT COUNT(s) FROM SolicitudEntity s")
    Integer totalSolicitudes();

    // Consulta JPQL: buscar solicitudes por estado
    @Query("SELECT COUNT(s) FROM SolicitudEntity s WHERE s.objEstadoSolicitud.id_estado = :idEstado")
    Integer contarPorEstado(@Param("idEstado") Integer idEstado);


    @Query("SELECT COUNT(s) FROM SolicitudEntity s WHERE s.fecha_registro_solicitud BETWEEN :fechaInicio AND :fechaFin")
    Integer contarPorRangoFechas(@Param("fechaInicio") Date fechaInicio, @Param("fechaFin") Date fechaFin);
    
    @Query(value =
    "SELECT COUNT(s) FROM SolicitudEntity s " +
    "WHERE LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombreSolicitud, '%')) " +
    "AND s.fecha_registro_solicitud BETWEEN :fechaInicio AND :fechaFin " +
    "AND s.objUsuario.objPrograma.id_programa = :idPrograma")
    Integer contarNombreFechaYPrograma(
    @Param("nombreSolicitud") String nombreSolicitud,
    @Param("fechaInicio") Date fechaInicio, @Param("fechaFin") Date fechaFin,
    @Param("idPrograma") Integer idPrograma);

    @Query(value =
    "SELECT COUNT(s) FROM SolicitudEntity s " +
    "WHERE LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombreSolicitud, '%')) " +
    "AND s.fecha_registro_solicitud BETWEEN :fechaInicio AND :fechaFin " +
    "AND s.objEstadoSolicitud.id_estado = :idEstado " +
    "AND s.objUsuario.objPrograma.id_programa = :idPrograma")

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

    @Query("SELECT u.solicitudes FROM UsuarioEntity u WHERE u.id_usuario = :idUsuario")
    List<SolicitudEntity> buscarSolicitudesPorUsuario(@Param("idUsuario") Integer idUsuario);

    @Query("SELECT s FROM SolicitudCursoVeranoPreinscripcionEntity s WHERE s.objUsuario.id_usuario = :idUsuario "+
    "AND LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombre, '%')) "+
    "AND s.objCursoOfertado.id_curso = :idCurso")
    SolicitudEntity buscarSolicitudesPorUsuarioNombreSolicitudCursoPre(@Param("idUsuario") Integer idUsuario, @Param("nombre") String nombre, @Param("idCurso") Integer idCurso);

    @Query("SELECT s FROM SolicitudCursoVeranoInscripcionEntity s WHERE s.objUsuario.id_usuario = :idUsuario "+
    "AND LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombre, '%')) "+
    "AND s.objCursoOfertado.id_curso = :idCurso")
    SolicitudEntity buscarSolicitudesPorUsuarioNombreSolicitudCursoIns(@Param("idUsuario") Integer idUsuario, @Param("nombre") String nombre, @Param("idCurso") Integer idCurso);



}