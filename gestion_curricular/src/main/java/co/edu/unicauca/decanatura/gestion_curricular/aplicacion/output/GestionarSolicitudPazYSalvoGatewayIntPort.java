package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;

/**
 * Los listados por estado, programa, período y usuario están en {@link ConsultarSolicitudesGatewayIntPort}.
 */
public interface GestionarSolicitudPazYSalvoGatewayIntPort extends ConsultarSolicitudesGatewayIntPort<SolicitudPazYSalvo> {

    /** Solicitudes que pasaron por {@code estado} alguna vez: el historial de procesadas de un rol (BUG-45). */
    @Override
    List<SolicitudPazYSalvo> listarPorEstadoEnHistorial(String estado, Integer idPrograma, String periodo);

    SolicitudPazYSalvo guardar(SolicitudPazYSalvo solicitud);

    List<SolicitudPazYSalvo> listarSolicitudes();

    Optional<SolicitudPazYSalvo> buscarPorId(Integer idSolicitud);

    void cambiarEstadoSolicitud(Integer idSolicitud, EstadoSolicitud nuevoEstado);
}
