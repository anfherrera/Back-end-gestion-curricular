package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.List;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;

public interface GestionarSolicitudCursoVeranoGatewayIntPort {
    SolicitudCursoVeranoPreinscripcion crearSolicitudCursoVeranoPreinscripcion(SolicitudCursoVeranoPreinscripcion solicitudCursoVerano);
    SolicitudCursoVeranoIncripcion crearSolicitudCursoVeranoInscripcion(SolicitudCursoVeranoIncripcion solicitudCursoVerano);
    Solicitud buscarSolicitudesPorUsuarioYCursoPre(Integer idUsuario, Integer idCurso);
    
    // Métodos para seguimiento de solicitudes
    List<SolicitudCursoVeranoPreinscripcion> buscarSolicitudesPorUsuario(Integer idUsuario);
    SolicitudCursoVeranoPreinscripcion buscarSolicitudPorId(Integer idSolicitud);
    SolicitudCursoVeranoIncripcion buscarSolicitudInscripcionPorId(Integer idSolicitud);
    
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
    
    // Método para buscar inscripción por usuario y curso
    SolicitudCursoVeranoIncripcion buscarSolicitudInscripcionPorUsuarioYCurso(Integer idUsuario, Integer idCurso);
    
    // Método para actualizar solicitud de curso verano
    SolicitudCursoVeranoPreinscripcion actualizarSolicitudCursoVerano(SolicitudCursoVeranoPreinscripcion solicitud);
    
    // Métodos para validaciones de seguridad
    List<SolicitudCursoVeranoIncripcion> buscarInscripcionesPorUsuarioYCurso(Integer idUsuario, Integer idCurso);
    Integer contarInscripcionesAceptadasPorCurso(Integer idCurso);
    Integer contarInscripcionesPorEstado(Integer idCurso, String estado);
}
