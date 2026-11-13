package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FiltroSolicitudes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;

public interface GestionarSolicitudReingresoCUIntPort {

    SolicitudReingreso crearSolicitudReingreso(SolicitudReingreso solicitud);

    List<SolicitudReingreso> listarSolicitudesReingreso();

    /** Solicitudes de un estudiante, o pendientes o procesadas por un rol (ver {@link FiltroSolicitudes}). */
    List<SolicitudReingreso> listar(FiltroSolicitudes filtro);

    /**
     * Listado "por rol" con el rol en mayúsculas (ESTUDIANTE, FUNCIONARIO, COORDINADOR), sin programa ni
     * período. Cualquier otro rol, incluido SECRETARIA, devuelve una lista vacía (BUG-9).
     */
    List<SolicitudReingreso> listarSolicitudesReingresoPorRol(String rol, Integer idUsuario);

    SolicitudReingreso obtenerSolicitudReingresoPorId(Integer id);

    void cambiarEstadoSolicitudReingreso(Integer idSolicitud, String nuevoEstado);
}
