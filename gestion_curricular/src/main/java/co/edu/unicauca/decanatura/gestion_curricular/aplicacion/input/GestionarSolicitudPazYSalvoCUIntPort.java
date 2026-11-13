package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FiltroSolicitudes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;

public interface GestionarSolicitudPazYSalvoCUIntPort {
    
    SolicitudPazYSalvo guardar(SolicitudPazYSalvo solicitud);

    List<SolicitudPazYSalvo> listarSolicitudes();

    /**
     * Solicitudes de un estudiante o pendientes de un rol (ver {@link FiltroSolicitudes}).
     * Paz y salvo no tiene listado de procesadas.
     */
    List<SolicitudPazYSalvo> listar(FiltroSolicitudes filtro);

    /**
     * Listado "por rol" con el rol en mayúsculas (ESTUDIANTE, FUNCIONARIO, COORDINADOR, SECRETARIA), sin
     * programa ni período. Cualquier otro rol devuelve una lista vacía.
     */
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

    /** Documentos de la solicitud que tienen nombre, en su orden, cada uno con su tipo según el nombre. */
    List<DocumentoConTipo> documentosConTipo(Integer idSolicitud);

    record DocumentoConTipo(Documento documento, String tipo) {
    }

    /** Oficios y resoluciones en PDF de la solicitud, en su orden. */
    List<Documento> oficios(Integer idSolicitud);

    record DatosDocumento(String nombreArchivo, String nombreEstudiante, String codigoEstudiante, String programa,
                          Object fechaSolicitud, String cedula, String titulo, String director) {
    }

    Optional<DatosDocumento> prepararDocumento(Integer id, String cedula, String titulo, String director,
                                               boolean fechaLocal);
}
