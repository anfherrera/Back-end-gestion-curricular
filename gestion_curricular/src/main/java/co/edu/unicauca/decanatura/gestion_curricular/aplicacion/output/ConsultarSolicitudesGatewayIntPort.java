package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;

/**
 * Consultas de listado comunes a los gateways de homologación, reingreso, paz y salvo y ECAES.
 *
 * El gateway no decide qué estado le corresponde a cada rol: recibe el texto ya decidido
 * ({@code EstadosSolicitud.textoGuardado()}) y lo compara tal cual con {@code estado_actual}.
 * Un {@code idPrograma} o un {@code periodo} null no filtran.
 */
public interface ConsultarSolicitudesGatewayIntPort<T extends Solicitud> {

    /** Solicitudes del usuario, de cualquier estado, ordenadas por id. */
    List<T> listarPorUsuario(Integer idUsuario, String periodo);

    /** Solicitudes cuyo estado más reciente (por fecha) es {@code estado}. */
    List<T> listarPorUltimoEstado(String estado, Integer idPrograma, String periodo);

    /**
     * Solicitudes que pasaron alguna vez por {@code estado}, aunque después hayan cambiado.
     * Solo homologación y reingreso tienen este listado.
     */
    default List<T> listarPorEstadoEnHistorial(String estado, Integer idPrograma, String periodo) {
        throw new UnsupportedOperationException("Este tipo de solicitud no tiene listado de procesadas");
    }
}
