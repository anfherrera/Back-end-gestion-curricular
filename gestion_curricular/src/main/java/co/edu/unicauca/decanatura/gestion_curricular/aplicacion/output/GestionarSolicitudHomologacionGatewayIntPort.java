package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;

/** Los listados por estado, programa, período y usuario están en {@link ConsultarSolicitudesGatewayIntPort}. */
public interface GestionarSolicitudHomologacionGatewayIntPort extends ConsultarSolicitudesGatewayIntPort<SolicitudHomologacion> {

    SolicitudHomologacion guardar(SolicitudHomologacion solicitud);

    List<SolicitudHomologacion> listarSolicitudes();

    /** Solicitudes que pasaron alguna vez por {@code estado} (homologación sí tiene listado de procesadas). */
    @Override
    List<SolicitudHomologacion> listarPorEstadoEnHistorial(String estado, Integer idPrograma, String periodo);

    Optional<SolicitudHomologacion> buscarPorId(Integer idSolicitud);

    void cambiarEstadoSolicitud(Integer idSolicitud, EstadoSolicitud nuevoEstado);
}
