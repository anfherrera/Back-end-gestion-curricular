package co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.ConsultarSolicitudesGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FiltroSolicitudes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FlujoEstadosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadosSolicitud;

/**
 * Lista las solicitudes de un tipo según un {@link FiltroSolicitudes}. El estado que ve cada rol lo
 * decide el {@link FlujoEstadosSolicitud} del tipo; el gateway solo recibe el texto guardado de ese estado.
 */
public class ListadorSolicitudes<T extends Solicitud> {

    private final FlujoEstadosSolicitud flujo;
    private final ConsultarSolicitudesGatewayIntPort<T> gateway;

    public ListadorSolicitudes(FlujoEstadosSolicitud flujo, ConsultarSolicitudesGatewayIntPort<T> gateway) {
        this.flujo = flujo;
        this.gateway = gateway;
    }

    /** @return lista vacía si el rol no tiene ningún paso en el flujo */
    public List<T> listar(FiltroSolicitudes filtro) {
        if (filtro.esDeEstudiante()) {
            return gateway.listarPorUsuario(filtro.idUsuario(), filtro.periodo());
        }
        if (filtro.procesadas()) {
            return flujo.estadoAprobadoPor(filtro.rol())
                    .map(EstadosSolicitud::textoGuardado)
                    .map(estado -> gateway.listarPorEstadoEnHistorial(estado, filtro.idPrograma(), filtro.periodo()))
                    .orElse(List.of());
        }
        return flujo.estadoPendientePara(filtro.rol())
                .map(EstadosSolicitud::textoGuardado)
                .map(estado -> gateway.listarPorUltimoEstado(estado, filtro.idPrograma(), filtro.periodo()))
                .orElse(List.of());
    }

    /**
     * Listado "por rol" ({@code listarSolicitudesPorRol}): rol en mayúsculas, sin programa ni período.
     * @see FiltroSolicitudes#porRol(String, Integer)
     */
    public List<T> listarPorRol(String rol, Integer idUsuario) {
        return FiltroSolicitudes.porRol(rol, idUsuario).map(this::listar).orElse(List.of());
    }
}
