package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.Date;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;

public interface GestionarSolicitudCUIntPort {
    
    // SolicitudCursoVeranoPreinscripcion crearSolicitudCursoVeranoPreinscripcion(SolicitudCursoVeranoPreinscripcion solicitudCursoVerano);
    // SolicitudCursoVeranoIncripcion crearSolicitudCursoVeranoInscripcion(SolicitudCursoVeranoIncripcion solicitudCursoVerano);
    SolicitudEcaes crearSolicitudEcaes(SolicitudEcaes solicitudEcaes);
    SolicitudReingreso crearSolicitudReingreso(SolicitudReingreso solicitudReingreso);
    SolicitudHomologacion crearSolicitudHomologacion(SolicitudHomologacion solicitudHomologacion);
    // SolicitudPazYSalvo crearSolicitudPazYSalvo(SolicitudPazYSalvo solicitudPazYSalvo);

    Solicitud actualizarSolicitud(Solicitud solicitud, EstadoSolicitud estadoSolicitud);

    // Obtener solicitud por ID
    Solicitud obtenerSolicitudPorId(Integer idSolicitud);

    // Eliminar solicitud
    boolean eliminarSolicitud(Integer idSolicitud);

    // Verifica si existe una solicitud con cierto nombre
    boolean existeSolicitudPorNombre(String nombreSolicitud);

    Integer contarSolicitudPorNombreYCursoIns(String nombreSolicitud, Integer idCurso);

    Integer contarSolicitudPorNombreCursoEstadoIns(String nombreSolicitud, Integer idCurso, String estadoActual);

    List<Solicitud> buscarSolicitudPorNombreCursoIns(String nombreSolicitud, Integer idCurso);

    List<Solicitud> buscarSolicitudPorNombreCursoEstadoIns(String nombreSolicitud, Integer idCurso, String estadoActual);

    Integer contarSolicitudPorNombreYCursoPre(String nombreSolicitud, Integer idCurso);

    Integer contarSolicitudPorNombreCursoEstadoPre(String nombreSolicitud, Integer idCurso, String estadoActual);

    List<Solicitud> buscarSolicitudPorNombreCursoPre(String nombreSolicitud, Integer idCurso);

    List<Solicitud> buscarSolicitudPorNombreCursoEstadoPre(String nombreSolicitud, Integer idCurso, String estadoActual);

    // Buscar solicitudes por rango de fechas
    List<Solicitud> buscarSolicitudesPorFecha(Date fechaInicio, Date fechaFin);

    // Listar todas las solicitudes
    List<Solicitud> listarSolicitudes();

    List<Solicitud> obtenerSolicitudesPorNombre(String nombreSolicitud);
    
    List<Solicitud> obtenerSolicitudesPorUsuario(Integer idUsuario);
    
}
