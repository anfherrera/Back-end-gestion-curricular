package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;


public interface SolicitudRepositoryInt extends JpaRepository<SolicitudEntity, Integer>{

    
    @Query("SELECT s.nombre_solicitud FROM SolicitudEntity s")
    Set<String> buscarNombresSolicitudes();
    
   
    @Query("SELECT COUNT(s) FROM SolicitudEntity s WHERE LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombre, '%')) ")
    Integer contarPorNombre(@Param("nombre") String nombre);

    @Query("SELECT COUNT(s) FROM SolicitudCursoVeranoInscripcionEntity s WHERE LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
    "AND s.objCursoOfertadoVerano.id_curso = :idCurso")
    Integer contarPorNombreYCursoIns(@Param("nombre") String nombre, @Param("idCurso") Integer idCurso);

    @Query("""
        SELECT COUNT(s)
        FROM SolicitudCursoVeranoInscripcionEntity s
        JOIN s.estadosSolicitud e
        WHERE LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombre, '%'))
        AND s.objCursoOfertadoVerano.id_curso = :idCurso
        AND LOWER(e.estado_actual) LIKE LOWER(CONCAT('%', :estado_actual, '%'))
        AND e.fecha_registro_estado = (
            SELECT MAX(e2.fecha_registro_estado)
            FROM EstadoSolicitudEntity e2
            WHERE e2.objSolicitud.id_solicitud = s.id_solicitud
        )
    """)
    Integer contarPorNombreCursoEstadoIns(
        @Param("nombre") String nombre,
        @Param("idCurso") Integer idCurso,
        @Param("estado_actual") String estado_actual
    );


    @Query("SELECT s FROM SolicitudCursoVeranoInscripcionEntity s WHERE s.objCursoOfertadoVerano.id_curso = :idCurso "+
    "AND s.esSeleccionado = :seleccionado")
    List<SolicitudEntity> buscarPorNombreCursoYSeleccionadoIns(@Param("idCurso") Integer idCurso, @Param("seleccionado") boolean seleccionado);
    
    @Query("SELECT s FROM SolicitudCursoVeranoPreinscripcionEntity s WHERE s.objCursoOfertadoVerano.id_curso = :idCurso "+
    "AND s.esSeleccionado = :seleccionado")
    List<SolicitudEntity> buscarPorNombreCursoYSeleccionadoPre(@Param("idCurso") Integer idCurso, @Param("seleccionado") boolean seleccionado);
    

    @Query("SELECT s FROM SolicitudCursoVeranoInscripcionEntity s WHERE s.objCursoOfertadoVerano.id_curso = :idCurso")
    List<SolicitudEntity> buscarPorNombreYCursoIns(@Param("idCurso") Integer idCurso);

    @Query("SELECT COUNT(s) FROM SolicitudCursoVeranoPreinscripcionEntity s WHERE LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
    "AND s.objCursoOfertadoVerano.id_curso = :idCurso")
    Integer contarPorNombreYCursoPre(@Param("nombre") String nombre, @Param("idCurso") Integer idCurso);

    @Query("""
        SELECT COUNT(s)
        FROM SolicitudCursoVeranoPreinscripcionEntity s
        JOIN s.estadosSolicitud e
        WHERE LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombre, '%'))
        AND s.objCursoOfertadoVerano.id_curso = :idCurso
        AND LOWER(e.estado_actual) LIKE LOWER(CONCAT('%', :estado_actual, '%'))
        AND e.fecha_registro_estado = (
            SELECT MAX(e2.fecha_registro_estado)
            FROM EstadoSolicitudEntity e2
            WHERE e2.objSolicitud.id_solicitud = s.id_solicitud
        )
    """)
    Integer contarPorNombreCursoEstadoPre(
        @Param("nombre") String nombre,
        @Param("idCurso") Integer idCurso,
        @Param("estado_actual") String estado_actual
    );

    @Query("SELECT s FROM SolicitudCursoVeranoPreinscripcionEntity s WHERE s.objCursoOfertadoVerano.id_curso = :idCurso")
    List<SolicitudEntity> buscarPorNombreYCursoPre(@Param("idCurso") Integer idCurso);

    @Query("""
        SELECT s 
        FROM SolicitudCursoVeranoInscripcionEntity s 
        JOIN s.estadosSolicitud e 
        WHERE LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombre, '%')) 
        AND s.objCursoOfertadoVerano.id_curso = :idCurso 
        AND LOWER(e.estado_actual) LIKE LOWER(CONCAT('%', :estado_actual, '%')) 
        AND e.fecha_registro_estado = (
            SELECT MAX(e2.fecha_registro_estado) 
            FROM EstadoSolicitudEntity e2 
            WHERE e2.objSolicitud.id_solicitud = s.id_solicitud
        )
    """)
    List<SolicitudEntity> buscarPorNombreCursoEstadoIns(
        @Param("nombre") String nombre, 
        @Param("idCurso") Integer idCurso, 
        @Param("estado_actual") String estado_actual
    );


    @Query("""
        SELECT s 
        FROM SolicitudCursoVeranoPreinscripcionEntity s 
        JOIN s.estadosSolicitud e 
        WHERE LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombre, '%')) 
        AND s.objCursoOfertadoVerano.id_curso = :idCurso 
        AND LOWER(e.estado_actual) LIKE LOWER(CONCAT('%', :estado_actual, '%')) 
        AND e.fecha_registro_estado = (
            SELECT MAX(e2.fecha_registro_estado) 
            FROM EstadoSolicitudEntity e2 
            WHERE e2.objSolicitud.id_solicitud = s.id_solicitud
        )
    """)
    List<SolicitudEntity> buscarPorNombreCursoEstadoPre(
        @Param("nombre") String nombre, 
        @Param("idCurso") Integer idCurso, 
        @Param("estado_actual") String estado_actual
    );


    // Consulta JPQL: contar todas las solicitudes
    @Query("SELECT COUNT(s) FROM SolicitudEntity s")
    Integer totalSolicitudes();

    @Query("""
        SELECT COUNT(e)
        FROM EstadoSolicitudEntity e
        WHERE e.estado_actual = :estado
        AND e.fecha_registro_estado = (
            SELECT MAX(e2.fecha_registro_estado)
            FROM EstadoSolicitudEntity e2
            WHERE e2.objSolicitud.id_solicitud = e.objSolicitud.id_solicitud
        )
    """)
    Integer contarSolicitudesPorUltimoEstado(@Param("estado") String estado);
    @Query("SELECT COUNT(s) FROM SolicitudEntity s WHERE s.fecha_registro_solicitud BETWEEN :fechaInicio AND :fechaFin")
    Integer contarPorRangoFechas(@Param("fechaInicio") Date fechaInicio, @Param("fechaFin") Date fechaFin);
    
    @Query(
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
        "JOIN s.estadosSolicitud est " +
        "WHERE LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombreSolicitud, '%')) " +
        "AND s.fecha_registro_solicitud BETWEEN :fechaInicio AND :fechaFin " +
        "AND LOWER(est.estado_actual) LIKE LOWER(CONCAT('%', :estado, '%')) " +
        "AND s.objUsuario.objPrograma.id_programa = :idPrograma")
    Integer contarNombreFechaEstadoYPrograma(
        @Param("nombreSolicitud") String nombreSolicitud,
        @Param("fechaInicio") Date fechaInicio, @Param("fechaFin") Date fechaFin,
        @Param("estado") String estado,
        @Param("idPrograma") Integer idPrograma);

    @Query(
    "SELECT COUNT(s) FROM SolicitudEntity s " +
    "JOIN s.estadosSolicitud est " +
    "WHERE LOWER(est.estado_actual) LIKE LOWER(CONCAT('%', :estado, '%'))")
    Integer contarEstado(
    @Param("estado") String estado);


    // Consulta JPQL: buscar entre un rango de fechas
    @Query("SELECT s FROM SolicitudEntity s WHERE s.fecha_registro_solicitud BETWEEN :fechaInicio AND :fechaFin")
    List<SolicitudEntity> buscarPorRangoFechas(@Param("fechaInicio") Date fechaInicio, @Param("fechaFin") Date fechaFin);

    @Query("SELECT s FROM SolicitudEntity s WHERE LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<SolicitudEntity> buscarPorNombre(@Param("nombre") String nombre);

    @Query("SELECT u.solicitudes FROM UsuarioEntity u WHERE u.id_usuario = :idUsuario")
    List<SolicitudEntity> buscarSolicitudesPorUsuario(@Param("idUsuario") Integer idUsuario);

    @Query(
    """
        SELECT s 
        FROM SolicitudCursoVeranoPreinscripcionEntity s 
        JOIN s.estadosSolicitud e 
        WHERE s.objUsuario.id_usuario = :idUsuario 
        AND s.objCursoOfertadoVerano.id_curso = :idCurso 
        AND LOWER(e.estado_actual) LIKE LOWER(CONCAT('%', :estado_actual, '%')) 
        AND e.fecha_registro_estado = (
            SELECT MAX(e2.fecha_registro_estado) 
            FROM EstadoSolicitudEntity e2 
            WHERE e2.objSolicitud.id_solicitud = s.id_solicitud
        )
    """)
    SolicitudEntity buscarSolicitudesPorUsuarioCursoEstadoPre(@Param("idUsuario") Integer idUsuario, @Param("idCurso") Integer idCurso, @Param("estado_actual") String estado_actual);

    
    @Query("SELECT s FROM SolicitudCursoVeranoPreinscripcionEntity s WHERE s.objUsuario.id_usuario = :idUsuario "+
    "AND s.objCursoOfertadoVerano.id_curso = :idCurso")
    SolicitudEntity buscarSolicitudesPorUsuarioyCursoPre(@Param("idUsuario") Integer idUsuario, @Param("idCurso") Integer idCurso);

    @Query(
    """
        SELECT s 
        FROM SolicitudCursoVeranoInscripcionEntity s 
        JOIN s.estadosSolicitud e 
        WHERE s.objUsuario.id_usuario = :idUsuario 
        AND s.objCursoOfertadoVerano.id_curso = :idCurso 
        AND LOWER(e.estado_actual) LIKE LOWER(CONCAT('%', :estado_actual, '%')) 
        AND e.fecha_registro_estado = (
            SELECT MAX(e2.fecha_registro_estado) 
            FROM EstadoSolicitudEntity e2 
            WHERE e2.objSolicitud.id_solicitud = s.id_solicitud
        )
    """)
    SolicitudEntity buscarSolicitudesPorUsuarioCursoEstadoIns(@Param("idUsuario") Integer idUsuario, @Param("idCurso") Integer idCurso, @Param("estado_actual") String estado_actual);

        @Query("SELECT s FROM SolicitudCursoVeranoInscripcionEntity s WHERE s.objUsuario.id_usuario = :idUsuario "+
    "AND s.objCursoOfertadoVerano.id_curso = :idCurso")
    SolicitudEntity buscarSolicitudesPorUsuarioyCursoIns(@Param("idUsuario") Integer idUsuario, @Param("idCurso") Integer idCurso);


    @Query("SELECT s FROM SolicitudCursoVeranoInscripcionEntity s WHERE s.objUsuario.id_usuario = :idUsuario "+
    "AND LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombre, '%')) "+
    "AND s.objCursoOfertadoVerano.id_curso = :idCurso")
    SolicitudEntity buscarSolicitudesPorUsuarioNombreSolicitudCursoIns(@Param("idUsuario") Integer idUsuario, @Param("nombre") String nombre, @Param("idCurso") Integer idCurso);

    @Modifying
    @Query("DELETE FROM SolicitudEntity s WHERE s.id = :id")
    void eliminarPorId(@Param("id") Integer id);

}