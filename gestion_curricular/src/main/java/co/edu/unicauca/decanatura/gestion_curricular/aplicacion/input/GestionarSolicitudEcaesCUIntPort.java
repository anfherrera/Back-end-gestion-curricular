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
