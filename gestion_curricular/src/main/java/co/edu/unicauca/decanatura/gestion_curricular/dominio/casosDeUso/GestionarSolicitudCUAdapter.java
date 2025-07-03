package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.Date;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarCursoOfertadoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

public class GestionarSolicitudCUAdapter implements GestionarSolicitudCUIntPort {

    private final GestionarSolicitudGatewayIntPort objGestionarSolicitudGateway;
    private final GestionarCursoOfertadoVeranoGatewayIntPort objCursoOfertado;
    private final GestionarUsuarioGatewayIntPort objUsuario;
    private final FormateadorResultadosIntPort objFormateadorResultados;

    public GestionarSolicitudCUAdapter(GestionarSolicitudGatewayIntPort objGestionarSolicitudGateway,GestionarCursoOfertadoVeranoGatewayIntPort objCursoOfertado,  GestionarUsuarioGatewayIntPort objUsuario, FormateadorResultadosIntPort objFormateadorResultados){
        this.objUsuario = objUsuario;
        this.objFormateadorResultados = objFormateadorResultados;
        this.objCursoOfertado = objCursoOfertado;
        this.objGestionarSolicitudGateway = objGestionarSolicitudGateway;
    }

    @Override
    public SolicitudCursoVeranoPreinscripcion crearSolicitudCursoVeranoPreinscripcion(
            SolicitudCursoVeranoPreinscripcion solicitudCursoVerano) {
        CursoOfertadoVerano cursoABuscar=null;
        SolicitudCursoVeranoPreinscripcion solicitudGuardada = null;
        Usuario usuarioBuscar =null;
        if(solicitudCursoVerano == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud de preinscripción no puede ser nula");
        }else{
            if(solicitudCursoVerano.getObjUsuario() == null){
                this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud de preinscripción no puede ser nula");
            }else{
                usuarioBuscar = this.objUsuario.obtenerUsuarioPorId(solicitudCursoVerano.getObjUsuario().getId_usuario());
                if(usuarioBuscar !=null){
                    cursoABuscar = this.objCursoOfertado.obtenerCursoPorId(solicitudCursoVerano.getObjCursoOfertado().getId_curso());
                    if(cursoABuscar != null){
                        if(cursoABuscar.getObjEstadoCursoOfertado().getEstado_actual().equals("Publicado")){
                            solicitudGuardada = this.objGestionarSolicitudGateway.crearSolicitudCursoVeranoPreinscripcion(solicitudCursoVerano);
                        }else{
                            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El curso no esta publicado");
                        }
                    }
                }else{
                    this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Usuario no encontrado");
                }
            }
        }
        return solicitudGuardada;
    }

    @Override
    public SolicitudCursoVeranoIncripcion crearSolicitudCursoVeranoInscripcion(
            SolicitudCursoVeranoIncripcion solicitudCursoVerano) {
        CursoOfertadoVerano cursoABuscar=null;
        SolicitudCursoVeranoIncripcion solicitudGuardada = null;
        Usuario usuarioBuscar = null;
        Solicitud solicitudPre = null;
        if(solicitudCursoVerano == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud de preinscripción no puede ser nula");
        }else{
            if(solicitudCursoVerano.getObjUsuario() == null){
                this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud de preinscripción no puede ser nula");
            }else{
                usuarioBuscar = this.objUsuario.obtenerUsuarioPorId(solicitudCursoVerano.getObjUsuario().getId_usuario());
                if(usuarioBuscar !=null){
                    cursoABuscar = this.objCursoOfertado.obtenerCursoPorId(solicitudCursoVerano.getObjCursoOfertado().getId_curso());
                    if(cursoABuscar != null){
                        if(cursoABuscar.getObjEstadoCursoOfertado().getEstado_actual().equals("Preinscripcion")){
                            solicitudPre = this.objGestionarSolicitudGateway.buscarSolicitudesPorUsuarioNombreSolicitudCursoPre(usuarioBuscar.getId_usuario(),SolicitudCursoVeranoPreinscripcion.class.getSimpleName(),cursoABuscar.getId_curso());
                            if(solicitudPre.getObjEstadoSolicitud().getEstado_actual().equals("Aprobado")){
                                solicitudGuardada = this.objGestionarSolicitudGateway.crearSolicitudCursoVeranoInscripcion(solicitudCursoVerano);
                            }else{
                                this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud de inscripcion no se puede crear porque la solicitud de preinscripcion no fue aprobada");
                            }
                        }else{
                            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El curso no esta publicado");
                        }
                    }
                }else{
                    this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Usuario no encontrado");
                }
            }
        }
        return solicitudGuardada;
    }

    @Override
    public SolicitudEcaes crearSolicitudEcaes(SolicitudEcaes solicitudEcaes) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'crearSolicitudEcaes'");
    }

    @Override
    public SolicitudReingreso crearSolicitudReingreso(SolicitudReingreso solicitudReingreso) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'crearSolicitudReingreso'");
    }

    @Override
    public SolicitudHomologacion crearSolicitudHomologacion(SolicitudHomologacion solicitudHomologacion) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'crearSolicitudHomologacion'");
    }

    @Override
    public SolicitudPazYSalvo crearSolicitudPazYSalvo(SolicitudPazYSalvo solicitudPazYSalvo) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'crearSolicitudPazYSalvo'");
    }

    @Override
    public Solicitud actualizarSolicitud(Solicitud solicitud, EstadoSolicitud estadoSolicitud) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'actualizarSolicitud'");
    }

    @Override
    public Solicitud obtenerSolicitudPorId(Integer idSolicitud) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'obtenerSolicitudPorId'");
    }

    @Override
    public boolean eliminarSolicitud(Integer idSolicitud) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'eliminarSolicitud'");
    }

    @Override
    public boolean existeSolicitudPorNombre(String nombreSolicitud) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'existeSolicitudPorNombre'");
    }

    @Override
    public Integer contarSolicitudPorNombreYCursoIns(String nombreSolicitud, Integer idCurso) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'contarSolicitudPorNombreYCursoIns'");
    }

    @Override
    public Integer contarSolicitudPorNombreCursoEstadoIns(String nombreSolicitud, Integer idCurso,
            String estadoActual) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'contarSolicitudPorNombreCursoEstadoIns'");
    }

    @Override
    public List<Solicitud> buscarSolicitudPorNombreCursoIns(String nombreSolicitud, Integer idCurso) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buscarSolicitudPorNombreCursoIns'");
    }

    @Override
    public List<Solicitud> buscarSolicitudPorNombreCursoEstadoIns(String nombreSolicitud, Integer idCurso,
            String estadoActual) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buscarSolicitudPorNombreCursoEstadoIns'");
    }

    @Override
    public Integer contarSolicitudPorNombreYCursoPre(String nombreSolicitud, Integer idCurso) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'contarSolicitudPorNombreYCursoPre'");
    }

    @Override
    public Integer contarSolicitudPorNombreCursoEstadoPre(String nombreSolicitud, Integer idCurso,
            String estadoActual) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'contarSolicitudPorNombreCursoEstadoPre'");
    }

    @Override
    public List<Solicitud> buscarSolicitudPorNombreCursoPre(String nombreSolicitud, Integer idCurso) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buscarSolicitudPorNombreCursoPre'");
    }

    @Override
    public List<Solicitud> buscarSolicitudPorNombreCursoEstadoPre(String nombreSolicitud, Integer idCurso,
            String estadoActual) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buscarSolicitudPorNombreCursoEstadoPre'");
    }

    @Override
    public List<Solicitud> buscarSolicitudesPorFecha(Date fechaInicio, Date fechaFin) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buscarSolicitudesPorFecha'");
    }

    @Override
    public List<Solicitud> listarSolicitudes() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listarSolicitudes'");
    }

    @Override
    public List<Solicitud> obtenerSolicitudesPorNombre(String nombreSolicitud) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'obtenerSolicitudesPorNombre'");
    }

    @Override
    public List<Solicitud> obtenerSolicitudesPorUsuario(Integer idUsuario) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'obtenerSolicitudesPorUsuario'");
    }


    
}
