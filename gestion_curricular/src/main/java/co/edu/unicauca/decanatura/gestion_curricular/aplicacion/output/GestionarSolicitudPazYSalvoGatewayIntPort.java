package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;

public interface GestionarSolicitudPazYSalvoGatewayIntPort {

    SolicitudPazYSalvo guardar(SolicitudPazYSalvo solicitud);

    List<SolicitudPazYSalvo> listarSolicitudes();

    List<SolicitudPazYSalvo> listarSolicitudesToFuncionario();

    List<SolicitudPazYSalvo> listarSolicitudesToCoordinador();

    List<SolicitudPazYSalvo> listarSolicitudesToSecretaria();

    List<SolicitudPazYSalvo> listarSolicitudesAprobadasToSecretaria();

    Optional<SolicitudPazYSalvo> buscarPorId(Integer idSolicitud);

    void cambiarEstadoSolicitud(Integer idSolicitud, EstadoSolicitud nuevoEstado);
}
