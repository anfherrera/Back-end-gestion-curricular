package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;

public interface GestionarSolicitudHomologacionGatewayIntPort {

    SolicitudHomologacion guardar(SolicitudHomologacion solicitud);

    List<SolicitudHomologacion> listarSolicitudes();

    List<SolicitudHomologacion> listarSolicitudesToFuncionario();

    List<SolicitudHomologacion> listarSolicitudesToCoordinador();

    /**
     * Lista solicitudes de homologación para coordinador filtradas por programa académico
     * @param idPrograma ID del programa académico del coordinador
     * @return Lista de solicitudes del programa específico
     */
    List<SolicitudHomologacion> listarSolicitudesToCoordinadorPorPrograma(Integer idPrograma);

    /**
     * Lista solicitudes de homologación para funcionario filtradas por período académico
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes del período específico
     */
    List<SolicitudHomologacion> listarSolicitudesToFuncionarioPorPeriodo(String periodoAcademico);

    /**
     * Lista solicitudes de homologación para coordinador filtradas por programa y período académico
     * @param idPrograma ID del programa académico del coordinador
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes del programa y período específicos
     */
    List<SolicitudHomologacion> listarSolicitudesToCoordinadorPorProgramaYPeriodo(Integer idPrograma, String periodoAcademico);

    /**
     * Lista solicitudes de homologación para secretaría filtradas por período académico
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes del período específico
     */
    List<SolicitudHomologacion> listarSolicitudesToSecretariaPorPeriodo(String periodoAcademico);

    /**
     * Lista solicitudes aprobadas de homologación para secretaría filtradas por período académico
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes aprobadas del período específico
     */
    List<SolicitudHomologacion> listarSolicitudesAprobadasToSecretariaPorPeriodo(String periodoAcademico);

    /**
     * Lista solicitudes de homologación para estudiante filtradas por usuario y período académico
     * @param idUsuario ID del usuario (estudiante)
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes del usuario y período específicos
     */
    List<SolicitudHomologacion> listarSolicitudesPorUsuarioYPeriodo(Integer idUsuario, String periodoAcademico);

    List<SolicitudHomologacion> listarSolicitudesToSecretaria();

    List<SolicitudHomologacion> listarSolicitudesAprobadasToSecretaria();

    /**
     * Lista solicitudes aprobadas de homologación para funcionario (estado APROBADA_FUNCIONARIO)
     * @return Lista de solicitudes aprobadas por el funcionario
     */
    List<SolicitudHomologacion> listarSolicitudesAprobadasToFuncionario();

    /**
     * Lista solicitudes aprobadas de homologación para funcionario filtradas por período académico
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes aprobadas del período específico
     */
    List<SolicitudHomologacion> listarSolicitudesAprobadasToFuncionarioPorPeriodo(String periodoAcademico);

    /**
     * Lista solicitudes aprobadas de homologación para coordinador (estado APROBADA_COORDINADOR)
     * @return Lista de solicitudes aprobadas por el coordinador
     */
    List<SolicitudHomologacion> listarSolicitudesAprobadasToCoordinador();

    /**
     * Lista solicitudes aprobadas de homologación para coordinador filtradas por programa académico
     * @param idPrograma ID del programa académico del coordinador
     * @return Lista de solicitudes aprobadas del programa específico
     */
    List<SolicitudHomologacion> listarSolicitudesAprobadasToCoordinadorPorPrograma(Integer idPrograma);

    /**
     * Lista solicitudes aprobadas de homologación para coordinador filtradas por programa y período académico
     * @param idPrograma ID del programa académico del coordinador
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes aprobadas del programa y período específicos
     */
    List<SolicitudHomologacion> listarSolicitudesAprobadasToCoordinadorPorProgramaYPeriodo(Integer idPrograma, String periodoAcademico);

    Optional<SolicitudHomologacion> buscarPorId(Integer idSolicitud);

    void cambiarEstadoSolicitud(Integer idSolicitud, EstadoSolicitud nuevoEstado);
}
