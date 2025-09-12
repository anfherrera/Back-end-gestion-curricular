package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;

public interface GestionarSolicitudReingresoGatewayIntPort {

    SolicitudReingreso crearSolicitudReingreso(SolicitudReingreso solicitud);

    List<SolicitudReingreso> listarSolicitudesReingreso();

    Optional<SolicitudReingreso> buscarPorId(Integer id);

    void cambiarEstadoSolicitudReingreso(Integer idSolicitud, String nuevoEstado);
}
