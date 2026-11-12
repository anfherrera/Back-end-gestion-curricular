package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;

public interface GestionarSolicitudCUIntPort {
    
    // SolicitudPazYSalvo crearSolicitudPazYSalvo(SolicitudPazYSalvo solicitudPazYSalvo);

    // Obtener solicitud por ID
    Solicitud obtenerSolicitudPorId(Integer idSolicitud);

    // Eliminar solicitud
    boolean eliminarSolicitud(Integer idSolicitud);

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
