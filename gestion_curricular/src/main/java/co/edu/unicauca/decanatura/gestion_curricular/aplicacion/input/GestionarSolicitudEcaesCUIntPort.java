package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FechaEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadoSolicitudEcaes;

public interface GestionarSolicitudEcaesCUIntPort {
    SolicitudEcaes guardar(SolicitudEcaes solicitud);

    List<SolicitudEcaes> listarSolicitudes();

    SolicitudEcaes buscarPorId(Integer idSolicitud);

    void cambiarEstadoSolicitudEcaes(Integer idSolicitud, EstadoSolicitudEcaes nuevoEstado);

    FechaEcaes publicarFechasEcaes(FechaEcaes fechasEcaes);

    List<FechaEcaes> listarFechasEcaes();

}
