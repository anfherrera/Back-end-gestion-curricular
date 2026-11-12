package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;

public interface GestionarSolicitudCUIntPort {
    
    SolicitudReingreso crearSolicitudReingreso(SolicitudReingreso solicitudReingreso);
    SolicitudHomologacion crearSolicitudHomologacion(SolicitudHomologacion solicitudHomologacion);
    // SolicitudPazYSalvo crearSolicitudPazYSalvo(SolicitudPazYSalvo solicitudPazYSalvo);

    Solicitud actualizarSolicitud(Solicitud solicitud, EstadoSolicitud estadoSolicitud);

    // Obtener solicitud por ID
    Solicitud obtenerSolicitudPorId(Integer idSolicitud);

    // Eliminar solicitud
    boolean eliminarSolicitud(Integer idSolicitud);

    List<Solicitud> buscarSolicitudPorNombreCursoIns(String nombreSolicitud, Integer idCurso);

    List<Solicitud> buscarSolicitudPorNombreCursoPre(String nombreSolicitud, Integer idCurso);

    // Listar todas las solicitudes
    List<Solicitud> listarSolicitudes();

    List<Solicitud> obtenerSolicitudesPorNombre(String nombreSolicitud);
    
    /** Historial de solicitudes con filtros opcionales (null o vacío = sin filtro). */
    List<Solicitud> buscarHistorial(String periodoAcademico, Integer idUsuario, String estadoActual);

    /** Total de solicitudes del sistema que cuenta el historial. */
    long contarHistorial();

    /** Solicitudes ya procesadas (aprobadas o rechazadas). */
    long contarSolicitudesProcesadas();
}
