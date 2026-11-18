package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadYaExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.ReglaNegocioExcepcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;

public class GestionarSolicitudCUAdapter implements GestionarSolicitudCUIntPort {

    private final GestionarSolicitudGatewayIntPort gestionarSolicitudGateway;
    private final GestionarUsuarioGatewayIntPort usuario;

    public GestionarSolicitudCUAdapter(GestionarSolicitudGatewayIntPort gestionarSolicitudGateway,
    GestionarUsuarioGatewayIntPort usuario){
        this.usuario = usuario;
        this.gestionarSolicitudGateway = gestionarSolicitudGateway;
    }



    @Override
    public Solicitud obtenerSolicitudPorId(Integer idSolicitud) {
        if(idSolicitud == null) {
            throw new ReglaNegocioExcepcion("El ID de la solicitud no puede ser nulo");
        }
        return this.gestionarSolicitudGateway.obtenerSolicitudPorId(idSolicitud);
    }

    @Override
    public boolean eliminarSolicitud(Integer idSolicitud) {
        if(idSolicitud == null) {
            throw new ReglaNegocioExcepcion("El ID de la solicitud no puede ser nulo");
        }
        Solicitud solicitud = this.gestionarSolicitudGateway.obtenerSolicitudPorId(idSolicitud);
        if(solicitud == null) {
            throw new EntidadYaExisteException("Solicitud no existe en el sistema");
        }
        return this.gestionarSolicitudGateway.eliminarSolicitud(idSolicitud);
    }

    @Override
    public List<Solicitud> listarSolicitudes() {
        return this.gestionarSolicitudGateway.listarSolicitudes();
    }

    @Override
    public List<Solicitud> obtenerSolicitudesPorNombre(String nombreSolicitud) {
        if (nombreSolicitud == null || nombreSolicitud.isBlank()) {
            throw new ReglaNegocioExcepcion("El nombre de la solicitud no puede ser nulo o vacío");
        }
        return this.gestionarSolicitudGateway.obtenerSolicitudesPorNombre(nombreSolicitud);
       
    }

    @Override
    public List<Solicitud> buscarHistorial(String periodoAcademico, Integer idUsuario, String estadoActual) {
        return this.gestionarSolicitudGateway.buscarHistorial(vacioANull(periodoAcademico), idUsuario, vacioANull(estadoActual));
    }

    @Override
    public long contarHistorial() {
        return this.gestionarSolicitudGateway.contarHistorial();
    }

    @Override
    public long contarSolicitudesProcesadas() {
        return this.gestionarSolicitudGateway.contarSolicitudesProcesadas();
    }

    private static String vacioANull(String texto) {
        return texto != null && !texto.trim().isEmpty() ? texto.trim() : null;
    }


    
}
