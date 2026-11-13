package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;


import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;

public interface GestionarEstadoSolicitudGatewayIntPort {
    EstadoSolicitud guarEstadoSolicitud(EstadoSolicitud estadoSolicitud);
}
