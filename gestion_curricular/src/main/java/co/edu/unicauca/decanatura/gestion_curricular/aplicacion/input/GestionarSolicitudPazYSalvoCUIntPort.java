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

    /**
     * Lista solicitudes para funcionario filtradas por período académico
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes del período específico
     */
    List<SolicitudPazYSalvo> listarSolicitudesToFuncionarioPorPeriodo(String periodoAcademico);

    /**
     * Lista solicitudes aprobadas para funcionario filtradas por período académico
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes aprobadas del período específico
     */
    List<SolicitudPazYSalvo> listarSolicitudesAprobadasToFuncionarioPorPeriodo(String periodoAcademico);

    /**
     * Lista solicitudes para coordinador filtradas por programa y período académico
     * @param idPrograma ID del programa académico del coordinador
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes del programa y período específicos
     */
    List<SolicitudPazYSalvo> listarSolicitudesToCoordinadorPorProgramaYPeriodo(Integer idPrograma, String periodoAcademico);

    /**
     * Lista solicitudes aprobadas para coordinador filtradas por programa y período académico
     * @param idPrograma ID del programa académico del coordinador
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes aprobadas del programa y período específicos
     */
    List<SolicitudPazYSalvo> listarSolicitudesAprobadasToCoordinadorPorProgramaYPeriodo(Integer idPrograma, String periodoAcademico);

    /**
     * Lista solicitudes para secretaría filtradas por período académico
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes del período específico
     */
    List<SolicitudPazYSalvo> listarSolicitudesToSecretariaPorPeriodo(String periodoAcademico);

    /**
     * Lista solicitudes aprobadas para secretaría filtradas por período académico
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes aprobadas del período específico
     */
    List<SolicitudPazYSalvo> listarSolicitudesAprobadasToSecretariaPorPeriodo(String periodoAcademico);

    /**
     * Lista solicitudes para estudiante filtradas por usuario y período académico
     * @param idUsuario ID del usuario (estudiante)
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes del usuario y período específicos
     */
    List<SolicitudPazYSalvo> listarSolicitudesPorUsuarioYPeriodo(Integer idUsuario, String periodoAcademico);

    List<SolicitudPazYSalvo> listarSolicitudesPorRol(String rol, Integer idUsuario);

    SolicitudPazYSalvo buscarPorId(Integer idSolicitud);

    void cambiarEstadoSolicitud(Integer idSolicitud, String nuevoEstado);

    /**
     * Lista todas las solicitudes procesadas (historial verdadero - que han cambiado de estado desde "Enviada")
     * @return Lista de solicitudes procesadas
     */
    List<SolicitudPazYSalvo> listarSolicitudesProcesadas();

    /**
     * Lista solicitudes procesadas filtradas por período académico
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes procesadas del período específico
     */
    List<SolicitudPazYSalvo> listarSolicitudesProcesadasPorPeriodo(String periodoAcademico);

    /**
     * Lista solicitudes procesadas filtradas por programa y período académico
     * @param idPrograma ID del programa académico
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes procesadas del programa y período específicos
     */
    List<SolicitudPazYSalvo> listarSolicitudesProcesadasPorProgramaYPeriodo(Integer idPrograma, String periodoAcademico);
}
