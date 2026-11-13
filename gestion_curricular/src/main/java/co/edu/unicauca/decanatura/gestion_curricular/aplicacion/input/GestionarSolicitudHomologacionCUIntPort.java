package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FiltroSolicitudes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;

public interface GestionarSolicitudHomologacionCUIntPort {
    SolicitudHomologacion guardar(SolicitudHomologacion solicitud);

    List<SolicitudHomologacion> listarSolicitudes();

    /** Solicitudes de un estudiante, o pendientes o procesadas por un rol (ver {@link FiltroSolicitudes}). */
    List<SolicitudHomologacion> listar(FiltroSolicitudes filtro);

    /**
     * Listado "por rol" con el rol en mayúsculas (ESTUDIANTE, FUNCIONARIO, COORDINADOR), sin programa ni
     * período. Cualquier otro rol, incluido SECRETARIA, devuelve una lista vacía (BUG-9).
     */
    List<SolicitudHomologacion> listarSolicitudesPorRol(String rol, Integer idUsuario);

    SolicitudHomologacion buscarPorId(Integer idSolicitud);

    void cambiarEstadoSolicitud(Integer idSolicitud, String nuevoEstado);
}
