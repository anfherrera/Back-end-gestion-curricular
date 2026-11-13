package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FechaEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FiltroSolicitudes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;

public interface GestionarSolicitudEcaesCUIntPort {
    SolicitudEcaes guardar(SolicitudEcaes solicitud);

    List<SolicitudEcaes> listarSolicitudes();

    SolicitudEcaes buscarPorId(Integer idSolicitud);

    void cambiarEstadoSolicitud(Integer idSolicitud, String nuevoEstado);

    FechaEcaes publicarFechasEcaes(FechaEcaes fechasEcaes);

    List<FechaEcaes> listarFechasEcaes();

    /**
     * Solicitudes de un estudiante o pendientes de un rol (ver {@link FiltroSolicitudes}). En el flujo ECAES
     * solo decide el funcionario; ECAES no tiene listado de procesadas.
     */
    List<SolicitudEcaes> listar(FiltroSolicitudes filtro);

    /**
     * Listado "por rol" con el rol en mayúsculas (ESTUDIANTE, FUNCIONARIO), sin programa ni período.
     * Cualquier otro rol devuelve una lista vacía.
     */
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
