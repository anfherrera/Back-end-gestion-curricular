package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

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
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

public interface GestionarSolicitudGatewayIntPort {
    
    SolicitudCursoVeranoPreinscripcion crearSolicitudCursoVeranoPreinscripcion(SolicitudCursoVeranoPreinscripcion solicitudCursoVerano);
    SolicitudCursoVeranoIncripcion crearSolicitudCursoVeranoInscripcion(SolicitudCursoVeranoIncripcion solicitudCursoVerano);
    SolicitudEcaes crearSolicitudEcaes(SolicitudEcaes solicitudEcaes);
    SolicitudReingreso crearSolicitudReingreso(SolicitudReingreso solicitudReingreso);
    SolicitudHomologacion crearSolicitudHomologacion(SolicitudHomologacion solicitudHomologacion);
    SolicitudPazYSalvo crearSolicitudPazYSalvo(SolicitudPazYSalvo solicitudPazYSalvo);

    Solicitud actualizarSolicitud(Solicitud solicitud, EstadoSolicitud estadoSolicitud);

    // Asocia usuario a solicitud
    Usuario asociarUsuarioASolicitud(Integer idUsuario, Integer idSolicitud);
    ///Solicitud guardarSolicitud(Solicitud solicitud);

    // Obtener solicitud por ID
    Solicitud obtenerSolicitudPorId(Integer idSolicitud);

    // Eliminar solicitud
    boolean eliminarSolicitud(Integer idSolicitud);

    // Verifica si existe una solicitud con cierto nombre
    boolean existeSolicitudPorNombre(String nombreSolicitud);

    // Contar total de solicitudes
    Integer contarSolicitudes();

    // Buscar solicitudes por rango de fechas
    List<Solicitud> buscarSolicitudesPorFecha(Date fechaInicio, Date fechaFin);

    // Listar todas las solicitudes
    List<Solicitud> listarSolicitudes();

    List<Solicitud> obtenerSolicitudesPorNombre(String nombreSolicitud);
    
    List<Solicitud> obtenerSolicitudesPorUsuario(Integer idUsuario);
    
}
