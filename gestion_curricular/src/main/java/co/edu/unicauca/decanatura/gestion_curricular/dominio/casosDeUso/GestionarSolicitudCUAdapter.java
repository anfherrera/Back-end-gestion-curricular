package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

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
    public SolicitudEcaes crearSolicitudEcaes(SolicitudEcaes solicitudEcaes) {
        // Implementación pendiente
        return null;
    }

    @Override
    public SolicitudReingreso crearSolicitudReingreso(SolicitudReingreso solicitudReingreso) {
        // Implementación pendiente
        return null;
    }

    @Override
    public SolicitudHomologacion crearSolicitudHomologacion(SolicitudHomologacion solicitudHomologacion) {
        // Implementación pendiente
        return null;
    }


    @Override
    public Solicitud actualizarSolicitud(Solicitud solicitud, EstadoSolicitud estadoSolicitud) {
    Solicitud solicitudGuardada = null;
    Usuario usuarioBuscar = null;
    Solicitud solicitudABuscar = null;

    if (solicitud == null) {
        this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud no puede ser nula");
    }

    if(solicitud.getId_solicitud() == null) {
        this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID de la solicitud no puede ser nulo");
    }

    if (solicitud.getObjUsuario() == null) {
        this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud debe tener un usuario asociado");
    }

    if (solicitud.getObjUsuario().getId_usuario() == null) {
        this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El usuario de la solicitud no puede ser nulo");
    }

    if (estadoSolicitud == null) {
        this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El estado de la solicitud no puede ser nulo");
    }

    usuarioBuscar = this.objUsuario.obtenerUsuarioPorId(solicitud.getObjUsuario().getId_usuario());
    if (usuarioBuscar == null) {
        this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Usuario no encontrado");
    }

    solicitudABuscar = this.objGestionarSolicitudGateway.obtenerSolicitudPorId(solicitud.getId_solicitud());
    if (solicitudABuscar == null) {
        this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Solicitud no existe en el sistema");
    }

    solicitud.setObjUsuario(usuarioBuscar);
    solicitudGuardada = this.objGestionarSolicitudGateway.actualizarSolicitud(solicitud, estadoSolicitud);

    return solicitudGuardada;
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
    public boolean existeSolicitudPorNombre(String nombreSolicitud) {
        // Implementación pendiente
        return false;
    }

    @Override
    public Integer contarSolicitudPorNombreYCursoIns(String nombreSolicitud, Integer idCurso) {
        // Implementación pendiente
        return 0;
    }

    @Override
    public Integer contarSolicitudPorNombreCursoEstadoIns(String nombreSolicitud, Integer idCurso,
            String estadoActual) {
        // Implementación pendiente
        return 0;
    }

    @Override
    public List<Solicitud> buscarSolicitudPorNombreCursoIns(String nombreSolicitud, Integer idCurso) {
        // Implementación pendiente
        return new ArrayList<>();
    }

    @Override
    public List<Solicitud> buscarSolicitudPorNombreCursoEstadoIns(String nombreSolicitud, Integer idCurso,
            String estadoActual) {
        // Implementación pendiente
        return new ArrayList<>();
    }

    @Override
    public Integer contarSolicitudPorNombreYCursoPre(String nombreSolicitud, Integer idCurso) {
        // Implementación pendiente
        return 0;
    }

    @Override
    public Integer contarSolicitudPorNombreCursoEstadoPre(String nombreSolicitud, Integer idCurso,
            String estadoActual) {
        // Implementación pendiente
        return 0;
    }

    @Override
    public List<Solicitud> buscarSolicitudPorNombreCursoPre(String nombreSolicitud, Integer idCurso) {
        // Implementación pendiente
        return new ArrayList<>();
    }

    @Override
    public List<Solicitud> buscarSolicitudPorNombreCursoEstadoPre(String nombreSolicitud, Integer idCurso,
            String estadoActual) {
        // Implementación pendiente
        return new ArrayList<>();
    }

    @Override
    public List<Solicitud> buscarSolicitudesPorFecha(Date fechaInicio, Date fechaFin) {
        if(fechaInicio == null){
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La fecha de inicio no puede ser nula");
        }
        if(fechaFin == null){
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La fecha de fin no puede ser nula");
        }
        if(fechaInicio.after(fechaFin)){
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        return this.objGestionarSolicitudGateway.buscarSolicitudesPorFecha(fechaInicio, fechaFin);
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
    public List<Solicitud> obtenerSolicitudesPorUsuario(Integer idUsuario) {
        if(idUsuario == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El ID del usuario no puede ser nulo");
        }
        Usuario usuario = this.objUsuario.obtenerUsuarioPorId(idUsuario);
        if(usuario == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Usuario no encontrado");
        }
        List<Solicitud> solicitudes = this.objGestionarSolicitudGateway.obtenerSolicitudesPorUsuario(idUsuario);
        if(solicitudes == null || solicitudes.isEmpty()) {  
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontraron solicitudes para el usuario");
        }
        return solicitudes;
    }


    
}
