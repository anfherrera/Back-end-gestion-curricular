package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;

public interface GestionarSolicitudReingresoGatewayIntPort {

    SolicitudReingreso crearSolicitudReingreso(SolicitudReingreso solicitud);

    List<SolicitudReingreso> listarSolicitudesReingreso();

    List<SolicitudReingreso> listarSolicitudesReingresoToFuncionario();

    List<SolicitudReingreso> listarSolicitudesReingresoToSecretaria();

    List<SolicitudReingreso> listarSolicitudesReingresoToCoordinador();

    /**
     * Lista solicitudes de reingreso para coordinador filtradas por programa académico
     * @param idPrograma ID del programa académico del coordinador
     * @return Lista de solicitudes del programa específico
     */
    List<SolicitudReingreso> listarSolicitudesReingresoToCoordinadorPorPrograma(Integer idPrograma);

    List<SolicitudReingreso> listarSolicitudesAprobadasToSecretaria();

    Optional<SolicitudReingreso> buscarPorId(Integer id);

    void cambiarEstadoSolicitudReingreso(Integer idSolicitud, EstadoSolicitud nuevoEstado);
}
