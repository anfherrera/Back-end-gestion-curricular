package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.List;


import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;

public class GestionarSolicitudCUAdapter implements GestionarSolicitudCUIntPort {

    private final GestionarSolicitudGatewayIntPort objGestionarSolicitudGateway;
    private final GestionarUsuarioGatewayIntPort objUsuario;
    private final FormateadorResultadosIntPort objFormateadorResultados;

    public GestionarSolicitudCUAdapter(GestionarSolicitudGatewayIntPort objGestionarSolicitudGateway,
    GestionarUsuarioGatewayIntPort objUsuario, 
    FormateadorResultadosIntPort objFormateadorResultados){
        this.objUsuario = objUsuario;
        this.objFormateadorResultados = objFormateadorResultados;
        this.objGestionarSolicitudGateway = objGestionarSolicitudGateway;
    }



    @Override
    public Solicitud obtenerSolicitudPorId(Integer idSolicitud) {
        if(idSolicitud == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID de la solicitud no puede ser nulo");
        }
        return this.objGestionarSolicitudGateway.obtenerSolicitudPorId(idSolicitud);
    }

    @Override
    public boolean eliminarSolicitud(Integer idSolicitud) {
        if(idSolicitud == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID de la solicitud no puede ser nulo");
        }
        Solicitud solicitud = this.objGestionarSolicitudGateway.obtenerSolicitudPorId(idSolicitud);
        if(solicitud == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Solicitud no existe en el sistema");
        }
        return this.objGestionarSolicitudGateway.eliminarSolicitud(idSolicitud);
    }

    @Override
    public List<Solicitud> listarSolicitudes() {
        return this.objGestionarSolicitudGateway.listarSolicitudes();
    }

    @Override
    public List<Solicitud> obtenerSolicitudesPorNombre(String nombreSolicitud) {
        if (nombreSolicitud == null || nombreSolicitud.isBlank()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El nombre de la solicitud no puede ser nulo o vacío");
        }
        return this.objGestionarSolicitudGateway.obtenerSolicitudesPorNombre(nombreSolicitud);
       
    }

    @Override
    public List<Solicitud> buscarHistorial(String periodoAcademico, Integer idUsuario, String estadoActual) {
        return this.objGestionarSolicitudGateway.buscarHistorial(vacioANull(periodoAcademico), idUsuario, vacioANull(estadoActual));
    }

    @Override
    public long contarHistorial() {
        return this.objGestionarSolicitudGateway.contarHistorial();
    }

    @Override
    public long contarSolicitudesProcesadas() {
        return this.objGestionarSolicitudGateway.contarSolicitudesProcesadas();
    }

    private static String vacioANull(String texto) {
        return texto != null && !texto.trim().isEmpty() ? texto.trim() : null;
    }


    
}
