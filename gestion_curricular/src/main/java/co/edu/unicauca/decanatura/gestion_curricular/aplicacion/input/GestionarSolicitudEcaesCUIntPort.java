package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FechaEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadosSolicitud;

public interface GestionarSolicitudEcaesCUIntPort {
    SolicitudEcaes guardar(SolicitudEcaes solicitud);

    List<SolicitudEcaes> listarSolicitudes();

    SolicitudEcaes buscarPorId(Integer idSolicitud);

    void cambiarEstadoSolicitudEcaes(Integer idSolicitud, EstadosSolicitud nuevoEstado);

    void cambiarEstadoSolicitud(Integer idSolicitud, String nuevoEstado);

    FechaEcaes publicarFechasEcaes(FechaEcaes fechasEcaes);

    List<FechaEcaes> listarFechasEcaes();

    List<SolicitudEcaes> listarSolicitudesToFuncionario();

    /**
     * Lista solicitudes ECAES para funcionario filtradas por período académico
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes del período específico
     */
    List<SolicitudEcaes> listarSolicitudesToFuncionarioPorPeriodo(String periodoAcademico);

    /**
     * Lista solicitudes ECAES para coordinador filtradas por programa y período académico
     * @param idPrograma ID del programa académico del coordinador
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes del programa y período específicos
     */
    List<SolicitudEcaes> listarSolicitudesToCoordinadorPorProgramaYPeriodo(Integer idPrograma, String periodoAcademico);

    /**
     * Lista solicitudes ECAES para estudiante filtradas por usuario y período académico
     * @param idUsuario ID del usuario (estudiante)
     * @param periodoAcademico Período académico (formato: "YYYY-P", ej: "2025-2")
     * @return Lista de solicitudes del usuario y período específicos
     */
    List<SolicitudEcaes> listarSolicitudesPorUsuarioYPeriodo(Integer idUsuario, String periodoAcademico);

    List<SolicitudEcaes> listarSolicitudesPorRol(String rol, Integer idUsuario);

    /**
     * Buscar fechas ECAES por período académico
     */
    Optional<FechaEcaes> buscarFechasPorPeriodo(String periodoAcademico);

    /**
     * Actualizar fechas ECAES existentes
     */
    FechaEcaes actualizarFechasEcaes(FechaEcaes fechasEcaes);

}
