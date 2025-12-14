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

    /**
     * Lista solicitudes de reingreso para funcionario filtradas por período académico
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes del período específico
     */
    List<SolicitudReingreso> listarSolicitudesReingresoToFuncionarioPorPeriodo(String periodoAcademico);

    /**
     * Lista solicitudes de reingreso para coordinador filtradas por programa y período académico
     * @param idPrograma ID del programa académico del coordinador
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes del programa y período específicos
     */
    List<SolicitudReingreso> listarSolicitudesReingresoToCoordinadorPorProgramaYPeriodo(Integer idPrograma, String periodoAcademico);

    /**
     * Lista solicitudes de reingreso para secretaría filtradas por período académico
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes del período específico
     */
    List<SolicitudReingreso> listarSolicitudesReingresoToSecretariaPorPeriodo(String periodoAcademico);

    /**
     * Lista solicitudes aprobadas de reingreso para secretaría filtradas por período académico
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes aprobadas del período específico
     */
    List<SolicitudReingreso> listarSolicitudesAprobadasToSecretariaPorPeriodo(String periodoAcademico);

    /**
     * Lista solicitudes de reingreso para estudiante filtradas por usuario y período académico
     * @param idUsuario ID del usuario (estudiante)
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes del usuario y período específicos
     */
    List<SolicitudReingreso> listarSolicitudesReingresoPorUsuarioYPeriodo(Integer idUsuario, String periodoAcademico);

    List<SolicitudReingreso> listarSolicitudesAprobadasToSecretaria();

    Optional<SolicitudReingreso> buscarPorId(Integer id);

    void cambiarEstadoSolicitudReingreso(Integer idSolicitud, EstadoSolicitud nuevoEstado);
}
