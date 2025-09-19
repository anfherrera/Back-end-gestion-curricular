package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;

public interface GestionarSolicitudHomologacionCUIntPort {
    SolicitudHomologacion guardar(SolicitudHomologacion solicitud);

    List<SolicitudHomologacion> listarSolicitudes();

    List<SolicitudHomologacion> listarSolicitudesPorRol(String rol, Integer idUsuario);

    SolicitudHomologacion buscarPorId(Integer idSolicitud);

    void cambiarEstadoSolicitud(Integer idSolicitud, String nuevoEstado);

    //SolicitudHomologacion obtenerSolicitudSeleccionada(Integer idSolicitud);

    


}
