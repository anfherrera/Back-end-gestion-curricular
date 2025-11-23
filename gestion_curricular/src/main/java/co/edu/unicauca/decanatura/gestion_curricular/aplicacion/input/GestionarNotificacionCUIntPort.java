package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Notificacion;

public interface GestionarNotificacionCUIntPort {

    Notificacion crearNotificacion(Notificacion notificacion);

    Notificacion obtenerNotificacionPorId(Integer idNotificacion);

    List<Notificacion> buscarPorUsuario(Integer idUsuario);

    List<Notificacion> buscarNoLeidasPorUsuario(Integer idUsuario);

    List<Notificacion> buscarUrgentesPorUsuario(Integer idUsuario);

    List<Notificacion> buscarPorTipoSolicitud(String tipoSolicitud);

    List<Notificacion> buscarPorSolicitud(Integer idSolicitud);

    Long contarNoLeidasPorUsuario(Integer idUsuario);

    void marcarTodasComoLeidas(Integer idUsuario);

    void marcarComoLeida(Integer idNotificacion);

    void eliminarAntiguas(java.util.Date fechaLimite);

    // Métodos de utilidad para crear notificaciones automáticamente
    void notificarCreacionSolicitud(co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud solicitud, String tipoSolicitud);
    
    void notificarCambioEstadoSolicitud(co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud solicitud, String estadoAnterior, String estadoNuevo, String tipoSolicitud);
}


