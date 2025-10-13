package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.List;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;

public interface GestionarSolicitudPazYSalvoCUIntPort {
    
    SolicitudPazYSalvo guardar(SolicitudPazYSalvo solicitud);

    List<SolicitudPazYSalvo> listarSolicitudes();

    List<SolicitudPazYSalvo> listarSolicitudesToFuncionario();

    List<SolicitudPazYSalvo> listarSolicitudesToCoordinador();

    List<SolicitudPazYSalvo> listarSolicitudesToSecretaria();

    SolicitudPazYSalvo buscarPorId(Integer idSolicitud);

    void cambiarEstadoSolicitud(Integer idSolicitud, String nuevoEstado);
}
