package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;

/**
 * Los listados por estado, programa, período y usuario están en {@link ConsultarSolicitudesGatewayIntPort}.
 * Paz y salvo no tiene listado de procesadas.
 */
public interface GestionarSolicitudPazYSalvoGatewayIntPort extends ConsultarSolicitudesGatewayIntPort<SolicitudPazYSalvo> {

    SolicitudPazYSalvo guardar(SolicitudPazYSalvo solicitud);

    List<SolicitudPazYSalvo> listarSolicitudes();

    Optional<SolicitudPazYSalvo> buscarPorId(Integer idSolicitud);

    void cambiarEstadoSolicitud(Integer idSolicitud, EstadoSolicitud nuevoEstado);
}
