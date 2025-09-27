package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Notificacion;

public interface GestionarNotificacionCUIntPort {
    
    // Operaciones básicas CRUD
    Notificacion crearNotificacion(Notificacion notificacion);
    Notificacion actualizarNotificacion(Notificacion notificacion);
    boolean eliminarNotificacion(Integer idNotificacion);
    Notificacion obtenerNotificacionPorId(Integer idNotificacion);
    
    // Búsquedas por usuario
    List<Notificacion> buscarPorUsuario(Integer idUsuario);
    Page<Notificacion> buscarPorUsuarioPaginado(Integer idUsuario, Pageable pageable);
    List<Notificacion> buscarNoLeidasPorUsuario(Integer idUsuario);
    Integer contarNoLeidasPorUsuario(Integer idUsuario);
    
    // Búsquedas por tipo
    List<Notificacion> buscarPorTipoSolicitud(String tipoSolicitud);
    List<Notificacion> buscarPorTipoNotificacion(String tipoNotificacion);
    List<Notificacion> buscarPorUsuarioYTipoSolicitud(Integer idUsuario, String tipoSolicitud);
    
    // Búsquedas por entidades relacionadas
    List<Notificacion> buscarPorSolicitud(Integer idSolicitud);
    List<Notificacion> buscarPorCurso(Integer idCurso);
    
    // Búsquedas especiales
    List<Notificacion> buscarUrgentes();
    List<Notificacion> buscarPorRangoFechas(Date fechaInicio, Date fechaFin);
    
    // Operaciones de estado
    boolean marcarComoLeida(Integer idNotificacion);
    boolean marcarComoNoLeida(Integer idNotificacion);
    boolean marcarTodasComoLeidas(Integer idUsuario);
    
    // Estadísticas
    Map<String, Long> estadisticasPorTipoSolicitud();
    Map<String, Long> estadisticasPorTipoNotificacion();
    Map<Integer, Long> estadisticasPorUsuario();
    
    // Dashboard y resúmenes
    Map<String, Object> obtenerDashboardUsuario(Integer idUsuario);
    Map<String, Object> obtenerEstadisticasGenerales();
    
    // Limpieza
    void eliminarNotificacionesAntiguas(Date fechaLimite);
    
    // Métodos de utilidad para crear notificaciones comunes
    Notificacion crearNotificacionNuevaSolicitud(String tipoSolicitud, Integer idUsuario, Integer idSolicitud, String mensaje);
    Notificacion crearNotificacionCambioEstado(String tipoSolicitud, Integer idUsuario, Integer idSolicitud, String estadoAnterior, String estadoNuevo);
    Notificacion crearNotificacionAprobacion(String tipoSolicitud, Integer idUsuario, Integer idSolicitud, boolean aprobado, String motivo);
    Notificacion crearNotificacionAlerta(String tipoSolicitud, Integer idUsuario, String titulo, String mensaje, boolean esUrgente);
    
    // Métodos específicos para cursos de verano
    Notificacion notificarNuevaPreinscripcion(Integer idUsuario, Integer idSolicitud);
    Notificacion notificarNuevaInscripcion(Integer idUsuario, Integer idSolicitud);
    Notificacion notificarPreinscripcionAprobada(Integer idUsuario, Integer idSolicitud);
    Notificacion notificarPreinscripcionRechazada(Integer idUsuario, Integer idSolicitud, String motivo);
    Notificacion notificarPagoValidado(Integer idUsuario, Integer idSolicitud, boolean esValido);
    Notificacion notificarInscripcionCompletada(Integer idUsuario, Integer idSolicitud);
    
    // Métodos para alertas automáticas
    Notificacion alertarCursoAltaDemanda(Integer idCurso, Integer cantidadSolicitudes);
    Notificacion alertarCursoPuntoEquilibrio(Integer idCurso, Integer cantidadSolicitudes, Integer cupoEstimado);
    Notificacion alertarFechaLimiteProxima(String tipoSolicitud, Date fechaLimite);
}
