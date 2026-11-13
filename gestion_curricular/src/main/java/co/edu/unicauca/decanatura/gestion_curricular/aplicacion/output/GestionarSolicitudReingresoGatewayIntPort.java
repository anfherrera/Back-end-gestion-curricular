package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;

/** Los listados por estado, programa, período y usuario están en {@link ConsultarSolicitudesGatewayIntPort}. */
public interface GestionarSolicitudReingresoGatewayIntPort extends ConsultarSolicitudesGatewayIntPort<SolicitudReingreso> {

    SolicitudReingreso crearSolicitudReingreso(SolicitudReingreso solicitud);

    List<SolicitudReingreso> listarSolicitudesReingreso();

    /** Solicitudes que pasaron alguna vez por {@code estado} (reingreso sí tiene listado de procesadas). */
    @Override
    List<SolicitudReingreso> listarPorEstadoEnHistorial(String estado, Integer idPrograma, String periodo);

    Optional<SolicitudReingreso> buscarPorId(Integer id);

    void cambiarEstadoSolicitudReingreso(Integer idSolicitud, EstadoSolicitud nuevoEstado);
}
