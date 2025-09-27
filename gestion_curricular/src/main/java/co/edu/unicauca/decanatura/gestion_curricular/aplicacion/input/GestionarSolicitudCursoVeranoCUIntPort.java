package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.List;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;

public interface GestionarSolicitudCursoVeranoCUIntPort {
    SolicitudCursoVeranoPreinscripcion crearSolicitudCursoVeranoPreinscripcion(SolicitudCursoVeranoPreinscripcion solicitudCursoVerano);
    SolicitudCursoVeranoIncripcion crearSolicitudCursoVeranoInscripcion(SolicitudCursoVeranoIncripcion solicitudCursoVerano);
    
    // Métodos para seguimiento de solicitudes
    List<SolicitudCursoVeranoPreinscripcion> buscarSolicitudesPorUsuario(Integer idUsuario);
    SolicitudCursoVeranoPreinscripcion buscarSolicitudPorId(Integer idSolicitud);
    
    // Métodos para gestión de funcionarios
    List<SolicitudCursoVeranoPreinscripcion> buscarPreinscripcionesPorCurso(Integer idCurso);
    List<SolicitudCursoVeranoIncripcion> buscarInscripcionesPorCurso(Integer idCurso);
    
    // Métodos para estadísticas
    Integer contarSolicitudesPorCurso(Integer idCurso);
    List<SolicitudCursoVeranoPreinscripcion> buscarCursosConAltaDemanda(Integer limiteMinimo);
    
    // Métodos para gestión de funcionarios - Aprobar/Rechazar
    SolicitudCursoVeranoPreinscripcion aprobarPreinscripcion(Integer idSolicitud, String comentarios);
    SolicitudCursoVeranoPreinscripcion rechazarPreinscripcion(Integer idSolicitud, String motivo);
    SolicitudCursoVeranoIncripcion validarPago(Integer idSolicitud, boolean esValido, String observaciones);
    SolicitudCursoVeranoIncripcion completarInscripcion(Integer idSolicitud);
}
