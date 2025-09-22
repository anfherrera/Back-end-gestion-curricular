package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;

public interface GestionarSolicitudReingresoCUIntPort {

    SolicitudReingreso crearSolicitudReingreso(SolicitudReingreso solicitud);

    List<SolicitudReingreso> listarSolicitudesReingreso();

    List<SolicitudReingreso> listarSolicitudesReingresoPorRol(String rol, Integer idUsuario);

    SolicitudReingreso obtenerSolicitudReingresoPorId(Integer id);

    void cambiarEstadoSolicitudReingreso(Integer idSolicitud, String nuevoEstado);
}
