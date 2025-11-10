package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;

public interface GestionarSolicitudHomologacionCUIntPort {
    SolicitudHomologacion guardar(SolicitudHomologacion solicitud);

    List<SolicitudHomologacion> listarSolicitudes();

    List<SolicitudHomologacion> listarSolicitudesToFuncionario();

    List<SolicitudHomologacion> listarSolicitudesToCoordinador();

    List<SolicitudHomologacion> listarSolicitudesToSecretaria();

    List<SolicitudHomologacion> listarSolicitudesPorRol(String rol, Integer idUsuario);

    List<SolicitudHomologacion> listarSolicitudesAprobadasToSecretaria();
    
    SolicitudHomologacion buscarPorId(Integer idSolicitud);

    void cambiarEstadoSolicitud(Integer idSolicitud, String nuevoEstado);

    //SolicitudHomologacion obtenerSolicitudSeleccionada(Integer idSolicitud);

    


}
