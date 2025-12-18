package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEntity;

@Repository
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

    // Consulta JPQL: contar todas las solicitudes por programa
    @Query("SELECT COUNT(s) FROM SolicitudEntity s WHERE s.objUsuario.objPrograma.id_programa = :idPrograma")
    Integer contarSolicitudesPorPrograma(@Param("idPrograma") Integer idPrograma);

    // Consulta JPQL: contar solicitudes con filtros combinados
    @Query("SELECT COUNT(s) FROM SolicitudEntity s WHERE " +
           "(:proceso IS NULL OR LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :proceso, '%'))) AND " +
           "(:idPrograma IS NULL OR s.objUsuario.objPrograma.id_programa = :idPrograma) AND " +
           "(:fechaInicio IS NULL OR s.fecha_registro_solicitud >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR s.fecha_registro_solicitud <= :fechaFin)")
    Integer contarSolicitudesConFiltros(@Param("proceso") String proceso, 
                                       @Param("idPrograma") Integer idPrograma,
                                       @Param("fechaInicio") Date fechaInicio, 
                                       @Param("fechaFin") Date fechaFin);

    // Consulta JPQL: contar solicitudes por estado con filtros
    @Query("SELECT COUNT(e) FROM EstadoSolicitudEntity e " +
           "JOIN e.objSolicitud s " +
           "WHERE e.estado_actual = :estado AND " +
           "e.fecha_registro_estado = (SELECT MAX(e2.fecha_registro_estado) FROM EstadoSolicitudEntity e2 WHERE e2.objSolicitud.id_solicitud = s.id_solicitud) AND " +
           "(:proceso IS NULL OR LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :proceso, '%'))) AND " +
           "(:idPrograma IS NULL OR s.objUsuario.objPrograma.id_programa = :idPrograma) AND " +
           "(:fechaInicio IS NULL OR s.fecha_registro_solicitud >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR s.fecha_registro_solicitud <= :fechaFin)")
    Integer contarSolicitudesPorEstadoConFiltros(@Param("estado") String estado,
                                                @Param("proceso") String proceso,
                                                @Param("idPrograma") Integer idPrograma,
                                                @Param("fechaInicio") Date fechaInicio,
                                                @Param("fechaFin") Date fechaFin);

    // Consulta JPQL: contar solicitudes por programa con filtros
    @Query("SELECT COUNT(s) FROM SolicitudEntity s WHERE s.objUsuario.objPrograma.id_programa = :idPrograma AND " +
           "(:proceso IS NULL OR LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :proceso, '%'))) AND " +
           "(:fechaInicio IS NULL OR s.fecha_registro_solicitud >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR s.fecha_registro_solicitud <= :fechaFin)")
    Integer contarSolicitudesPorProgramaConFiltros(@Param("idPrograma") Integer idPrograma,
                                                  @Param("proceso") String proceso,
                                                  @Param("fechaInicio") Date fechaInicio,
                                                  @Param("fechaFin") Date fechaFin);

    // Consulta JPQL: buscar nombres de solicitudes con filtros
    @Query("SELECT DISTINCT s.nombre_solicitud FROM SolicitudEntity s WHERE " +
           "(:proceso IS NULL OR LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :proceso, '%'))) AND " +
           "(:idPrograma IS NULL OR s.objUsuario.objPrograma.id_programa = :idPrograma) AND " +
           "(:fechaInicio IS NULL OR s.fecha_registro_solicitud >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR s.fecha_registro_solicitud <= :fechaFin)")
    Set<String> buscarNombresSolicitudesConFiltros(@Param("proceso") String proceso,
                                                   @Param("idPrograma") Integer idPrograma,
                                                   @Param("fechaInicio") Date fechaInicio,
                                                   @Param("fechaFin") Date fechaFin);

    // Consulta JPQL: contar por nombre con filtros
    @Query("SELECT COUNT(s) FROM SolicitudEntity s WHERE LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombre, '%')) AND " +
           "(:proceso IS NULL OR LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :proceso, '%'))) AND " +
           "(:idPrograma IS NULL OR s.objUsuario.objPrograma.id_programa = :idPrograma) AND " +
           "(:fechaInicio IS NULL OR s.fecha_registro_solicitud >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR s.fecha_registro_solicitud <= :fechaFin)")
    Integer contarPorNombreConFiltros(@Param("nombre") String nombre,
                                     @Param("proceso") String proceso,
                                     @Param("idPrograma") Integer idPrograma,
                                     @Param("fechaInicio") Date fechaInicio,
                                     @Param("fechaFin") Date fechaFin);

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
    "SELECT COUNT(DISTINCT s) FROM SolicitudEntity s " +
    "JOIN s.estadosSolicitud est " +
    "WHERE UPPER(est.estado_actual) = UPPER(:estado) " +
    "AND est.fecha_registro_estado = (" +
    "   SELECT MAX(e2.fecha_registro_estado) " +
    "   FROM EstadoSolicitudEntity e2 " +
    "   WHERE e2.objSolicitud.id_solicitud = s.id_solicitud" +
    ")")
    Integer contarEstado(
    @Param("estado") String estado);


    // Consulta JPQL: buscar entre un rango de fechas
    @Query("SELECT s FROM SolicitudEntity s WHERE s.fecha_registro_solicitud BETWEEN :fechaInicio AND :fechaFin")
    List<SolicitudEntity> buscarPorRangoFechas(@Param("fechaInicio") Date fechaInicio, @Param("fechaFin") Date fechaFin);

    @Query("SELECT s FROM SolicitudEntity s WHERE LOWER(s.nombre_solicitud) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<SolicitudEntity> buscarPorNombre(@Param("nombre") String nombre);

    @Query("SELECT s FROM SolicitudCursoVeranoPreinscripcionEntity s WHERE s.objUsuario.id_usuario = :idUsuario")
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

    // Métodos para cursos de verano
    @Query("SELECT s FROM SolicitudCursoVeranoPreinscripcionEntity s WHERE s.objCursoOfertadoVerano.id_curso = :idCurso")
    List<SolicitudEntity> buscarSolicitudesPorCurso(@Param("idCurso") Integer idCurso);
    
    @Query("SELECT s FROM SolicitudCursoVeranoInscripcionEntity s WHERE s.objCursoOfertadoVerano.id_curso = :idCurso")
    List<SolicitudEntity> buscarInscripcionesPorCurso(@Param("idCurso") Integer idCurso);

    @Query("SELECT COUNT(s) FROM SolicitudEntity s WHERE s.objCursoOfertadoVerano.id_curso = :idCurso")
    Integer contarSolicitudesPorCurso(@Param("idCurso") Integer idCurso);

    @Query("""
        SELECT s 
        FROM SolicitudCursoVeranoPreinscripcionEntity s 
        WHERE s.objCursoOfertadoVerano.id_curso IN (
            SELECT c.id_curso 
            FROM CursoOfertadoVeranoEntity c 
            WHERE (
                SELECT COUNT(s2) 
                FROM SolicitudCursoVeranoPreinscripcionEntity s2 
                WHERE s2.objCursoOfertadoVerano.id_curso = c.id_curso
            ) >= :limiteMinimo
        )
    """)
    List<SolicitudEntity> buscarCursosConAltaDemanda(@Param("limiteMinimo") Integer limiteMinimo);

    // Consultas para validaciones de seguridad
    @Query("SELECT s FROM SolicitudCursoVeranoInscripcionEntity s WHERE s.objUsuario.id_usuario = :idUsuario AND s.objCursoOfertadoVerano.id_curso = :idCurso")
    List<SolicitudEntity> buscarInscripcionesPorUsuarioYCurso(@Param("idUsuario") Integer idUsuario, @Param("idCurso") Integer idCurso);
    
    @Query("SELECT s FROM SolicitudCursoVeranoInscripcionEntity s WHERE s.objUsuario.id_usuario = :idUsuario")
    List<SolicitudEntity> buscarInscripcionesPorUsuario(@Param("idUsuario") Integer idUsuario);

    @Query("""
        SELECT COUNT(s) 
        FROM SolicitudCursoVeranoInscripcionEntity s 
        JOIN s.estadosSolicitud e 
        WHERE s.objCursoOfertadoVerano.id_curso = :idCurso 
        AND e.estado_actual = :estado
        AND e.fecha_registro_estado = (
            SELECT MAX(e2.fecha_registro_estado)
            FROM EstadoSolicitudEntity e2
            WHERE e2.objSolicitud.id_solicitud = s.id_solicitud
        )
    """)
    Integer contarInscripcionesPorEstado(@Param("idCurso") Integer idCurso, @Param("estado") String estado);

    @Query("""
    SELECT s FROM SolicitudEntity s
    JOIN FETCH s.objUsuario u
    LEFT JOIN FETCH s.estadosSolicitud es
    LEFT JOIN FETCH s.objCursoOfertadoVerano c
    LEFT JOIN FETCH c.objMateria m
    """)
    List<SolicitudEntity> findAllWithJoins();

    /**
     * Busca solicitudes con historial completo aplicando filtros opcionales en SQL
     * Esta consulta optimizada filtra en la base de datos en lugar de cargar todo en memoria
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P")
     * @param idUsuario ID del usuario opcional
     * @param estadoActual Estado actual de la solicitud opcional
     * @return Lista de solicitudes que cumplen los filtros
     */
    @Query("""
        SELECT DISTINCT s FROM SolicitudEntity s
        LEFT JOIN FETCH s.objUsuario u
        LEFT JOIN FETCH s.estadosSolicitud es
        LEFT JOIN FETCH s.objCursoOfertadoVerano c
        LEFT JOIN FETCH c.objMateria m
        WHERE (:periodoAcademico IS NULL OR s.periodo_academico = :periodoAcademico)
        AND (:idUsuario IS NULL OR u.id_usuario = :idUsuario)
        AND (:estadoActual IS NULL OR EXISTS (
            SELECT 1 FROM EstadoSolicitudEntity e
            WHERE e.objSolicitud.id_solicitud = s.id_solicitud
            AND e.estado_actual = :estadoActual
            AND e.fecha_registro_estado = (
                SELECT MAX(e2.fecha_registro_estado)
                FROM EstadoSolicitudEntity e2
                WHERE e2.objSolicitud.id_solicitud = s.id_solicitud
            )
        ))
        ORDER BY s.fecha_registro_solicitud DESC
    """)
    List<SolicitudEntity> buscarHistorialConFiltros(
        @Param("periodoAcademico") String periodoAcademico,
        @Param("idUsuario") Integer idUsuario,
        @Param("estadoActual") String estadoActual
    );

    /**
     * Cuenta el total de solicitudes que cumplen los filtros (sin cargar todas en memoria)
     */
    @Query("""
        SELECT COUNT(DISTINCT s) FROM SolicitudEntity s
        LEFT JOIN s.objUsuario u
        WHERE (:periodoAcademico IS NULL OR s.periodo_academico = :periodoAcademico)
        AND (:idUsuario IS NULL OR u.id_usuario = :idUsuario)
        AND (:estadoActual IS NULL OR EXISTS (
            SELECT 1 FROM EstadoSolicitudEntity e
            WHERE e.objSolicitud.id_solicitud = s.id_solicitud
            AND e.estado_actual = :estadoActual
            AND e.fecha_registro_estado = (
                SELECT MAX(e2.fecha_registro_estado)
                FROM EstadoSolicitudEntity e2
                WHERE e2.objSolicitud.id_solicitud = s.id_solicitud
            )
        ))
    """)
    Long contarHistorialConFiltros(
        @Param("periodoAcademico") String periodoAcademico,
        @Param("idUsuario") Integer idUsuario,
        @Param("estadoActual") String estadoActual
    );

    /**
     * Cuenta solicitudes procesadas (que tienen al menos un estado)
     */
    @Query("""
        SELECT COUNT(DISTINCT s) FROM SolicitudEntity s
        LEFT JOIN s.estadosSolicitud es
        WHERE es IS NOT NULL
        AND (:periodoAcademico IS NULL OR s.periodo_academico = :periodoAcademico)
        AND (:idUsuario IS NULL OR s.objUsuario.id_usuario = :idUsuario)
    """)
    Long contarSolicitudesProcesadas(
        @Param("periodoAcademico") String periodoAcademico,
        @Param("idUsuario") Integer idUsuario
    );


}