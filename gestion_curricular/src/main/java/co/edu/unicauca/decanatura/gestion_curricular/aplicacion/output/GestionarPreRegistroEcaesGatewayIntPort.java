package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FechaEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;

/**
 * Los listados por estado, programa, período y usuario están en {@link ConsultarSolicitudesGatewayIntPort}.
 * ECAES no tiene listado de procesadas.
 */
public interface GestionarPreRegistroEcaesGatewayIntPort extends ConsultarSolicitudesGatewayIntPort<SolicitudEcaes> {

    SolicitudEcaes guardar(SolicitudEcaes solicitud);

    List<SolicitudEcaes> listar();

    Optional<SolicitudEcaes> buscarPorId(Integer id);

    Optional<SolicitudEcaes> buscarOpcionalPorId(Integer id);

    void cambiarEstadoSolicitudEcaes(Integer idSolicitud, EstadoSolicitud nuevoEstado);

    FechaEcaes publicarFechasEcaes(FechaEcaes fechasEcaes);

    List<FechaEcaes> listarFechasEcaes();

    /**
     * Buscar fechas ECAES por período académico
     */
    Optional<FechaEcaes> buscarFechasPorPeriodo(String periodoAcademico);

    /**
     * Actualizar fechas ECAES existentes
     */
    FechaEcaes actualizarFechasEcaes(FechaEcaes fechasEcaes);

}
