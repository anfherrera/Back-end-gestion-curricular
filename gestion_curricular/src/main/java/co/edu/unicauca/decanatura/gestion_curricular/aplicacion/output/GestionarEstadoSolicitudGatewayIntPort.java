package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.Date;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;

public interface GestionarEstadoSolicitudGatewayIntPort {
    EstadoSolicitud crearEstadoSolicitud(EstadoSolicitud estadoSolicitud);
    EstadoSolicitud actualizarEstadoSolicitud(EstadoSolicitud estadoSolicitud);
    EstadoSolicitud editarEstadoSolicitud(EstadoSolicitud estadoSolicitud);
    boolean eliminarEstadoSolicitud(Integer id_estado_solicitud);
    EstadoSolicitud obtenerEstadoSolicitudPorId(Integer id_estado_solicitud);
    EstadoSolicitud obtenerEstadoSolicitudPorFecha(Date fecha_registro_solicitud);
}
