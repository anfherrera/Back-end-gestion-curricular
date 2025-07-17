package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

public interface GestionarPreRegistroEcaesGatewayIntPort {

    SolicitudEcaes guardar(SolicitudEcaes solicitud);

    List<SolicitudEcaes> listar();

    Optional<SolicitudEcaes> buscarPorId(Integer id);

    Optional<SolicitudEcaes> buscarOpcionalPorId(Integer id);

    Optional<Usuario> buscarUsuarioPorId(Integer idUsuario);

    void cambiarEstadoSolicitudEcaes(Integer idSolicitud, EstadoSolicitud nuevoEstado);


}
