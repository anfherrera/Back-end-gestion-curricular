package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;

public interface GestionarSolicitudCursoVeranoGatewayIntPort {
    SolicitudCursoVeranoPreinscripcion crearSolicitudCursoVeranoPreinscripcion(SolicitudCursoVeranoPreinscripcion solicitudCursoVerano);
    SolicitudCursoVeranoInscripcion crearSolicitudCursoVeranoInscripcion(SolicitudCursoVeranoInscripcion solicitudCursoVerano);
    Solicitud buscarSolicitudesPorUsuarioYCursoPre(Integer idUsuario, Integer idCurso);
    
    // Métodos para seguimiento de solicitudes
    List<SolicitudCursoVeranoPreinscripcion> buscarSolicitudesPorUsuario(Integer idUsuario);
    SolicitudCursoVeranoPreinscripcion buscarSolicitudPorId(Integer idSolicitud);
    SolicitudCursoVeranoInscripcion buscarSolicitudInscripcionPorId(Integer idSolicitud);
    
    // Métodos para gestión de funcionarios
    List<SolicitudCursoVeranoPreinscripcion> buscarPreinscripcionesPorCurso(Integer idCurso);
    List<SolicitudCursoVeranoInscripcion> buscarInscripcionesPorCurso(Integer idCurso);
    
    List<Solicitud> listarSolicitudesDeVerano(Integer idCurso);

    // Métodos para gestión de funcionarios - Aprobar/Rechazar
    SolicitudCursoVeranoPreinscripcion aprobarPreinscripcion(Integer idSolicitud, String comentarios);
    SolicitudCursoVeranoPreinscripcion rechazarPreinscripcion(Integer idSolicitud, String motivo);
    SolicitudCursoVeranoInscripcion validarPago(Integer idSolicitud, boolean esValido, String observaciones);
    
    // Método para actualizar solicitud de curso verano
    SolicitudCursoVeranoPreinscripcion actualizarSolicitudCursoVerano(SolicitudCursoVeranoPreinscripcion solicitud);
    
    // Métodos para validaciones de seguridad
    List<SolicitudCursoVeranoInscripcion> buscarInscripcionesPorUsuarioYCurso(Integer idUsuario, Integer idCurso);
    List<SolicitudCursoVeranoInscripcion> buscarInscripcionesPorUsuario(Integer idUsuario);
    Integer contarInscripcionesAceptadasPorCurso(Integer idCurso);
}
