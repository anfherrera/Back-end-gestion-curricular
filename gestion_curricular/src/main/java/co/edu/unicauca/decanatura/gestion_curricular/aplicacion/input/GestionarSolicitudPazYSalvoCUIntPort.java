package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
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

    /**
     * Lista solicitudes para coordinador filtradas por programa y período académico
     * @param idPrograma ID del programa académico del coordinador
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes del programa y período específicos
     */
    List<SolicitudPazYSalvo> listarSolicitudesToCoordinadorPorProgramaYPeriodo(Integer idPrograma, String periodoAcademico);

    /**
     * Lista solicitudes para secretaría filtradas por período académico
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes del período específico
     */
    List<SolicitudPazYSalvo> listarSolicitudesToSecretariaPorPeriodo(String periodoAcademico);

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
     * Cambia el estado de una solicitud con un comentario opcional
     * @param idSolicitud ID de la solicitud
     * @param nuevoEstado Nuevo estado de la solicitud
     * @param comentario Comentario o razón del cambio de estado (especialmente para rechazos)
     */
    void cambiarEstadoSolicitud(Integer idSolicitud, String nuevoEstado, String comentario);

    /** Aplica un paso solo si el actor puede actuar como el rol responsable. */
    Optional<RechazoCambioEstado> cambiarEstadoSolicitud(Integer idSolicitud, String nuevoEstado, String comentario,
            Predicate<String> puedeActuarComo);

    record RechazoCambioEstado(Motivo motivo, String mensaje, String estadoActual) {
        public enum Motivo {
            TRANSICION_NO_PERMITIDA, ROL_NO_AUTORIZADO
        }
    }
}
