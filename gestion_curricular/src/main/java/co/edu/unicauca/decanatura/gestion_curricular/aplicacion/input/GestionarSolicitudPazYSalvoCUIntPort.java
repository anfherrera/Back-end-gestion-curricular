package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.List;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;

public interface GestionarSolicitudPazYSalvoCUIntPort {
    
    SolicitudPazYSalvo guardar(SolicitudPazYSalvo solicitud);

    List<SolicitudPazYSalvo> listarSolicitudes();

    List<SolicitudPazYSalvo> listarSolicitudesToFuncionario();

    List<SolicitudPazYSalvo> listarSolicitudesToCoordinador();

    /**
     * Lista solicitudes para coordinador filtradas por programa académico
     * @param idPrograma ID del programa académico del coordinador
     * @return Lista de solicitudes del programa específico
     */
    List<SolicitudPazYSalvo> listarSolicitudesToCoordinadorPorPrograma(Integer idPrograma);

    List<SolicitudPazYSalvo> listarSolicitudesToSecretaria();

    List<SolicitudPazYSalvo> listarSolicitudesAprobadasToSecretaria();

    List<SolicitudPazYSalvo> listarSolicitudesAprobadasToFuncionario();

    List<SolicitudPazYSalvo> listarSolicitudesAprobadasToCoordinador();

    /**
     * Lista solicitudes aprobadas para coordinador filtradas por programa académico
     * @param idPrograma ID del programa académico del coordinador
     * @return Lista de solicitudes aprobadas del programa específico
     */
    List<SolicitudPazYSalvo> listarSolicitudesAprobadasToCoordinadorPorPrograma(Integer idPrograma);

    List<SolicitudPazYSalvo> listarSolicitudesPorRol(String rol, Integer idUsuario);

    SolicitudPazYSalvo buscarPorId(Integer idSolicitud);

    void cambiarEstadoSolicitud(Integer idSolicitud, String nuevoEstado);
}
