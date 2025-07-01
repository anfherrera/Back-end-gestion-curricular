package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.Date;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;

public interface GestionarEstadoSolicitudGatewayIntPort {
    EstadoSolicitud actualizarEstadoSolicitud(EstadoSolicitud estadoSolicitud);
    EstadoSolicitud obtenerEstadoSolicitudPorId(Integer id_estado_solicitud);
    EstadoSolicitud obtenerEstadoSolicitudPorSolicitudId(Integer id_estado_solicitud);
    List<EstadoSolicitud> obtenerEstadoSolicitudPorFecha(Date fecha_registro_solicitud);


}
