package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Notificacion;

public interface GestionarNotificacionGatewayIntPort {

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
}


