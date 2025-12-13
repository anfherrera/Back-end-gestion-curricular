package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;

public interface GestionarSolicitudReingresoCUIntPort {

    SolicitudReingreso crearSolicitudReingreso(SolicitudReingreso solicitud);

    List<SolicitudReingreso> listarSolicitudesReingreso();

    List<SolicitudReingreso> listarSolicitudesReingresoPorRol(String rol, Integer idUsuario);

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

    SolicitudReingreso obtenerSolicitudReingresoPorId(Integer id);

    void cambiarEstadoSolicitudReingreso(Integer idSolicitud, String nuevoEstado);
}
