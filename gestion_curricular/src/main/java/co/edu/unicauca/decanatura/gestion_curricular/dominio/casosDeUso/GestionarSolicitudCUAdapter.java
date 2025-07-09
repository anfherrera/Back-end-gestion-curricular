package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.Date;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarCursoOfertadoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.TipoDocumentoSolicitudPazYSalvo;

public class GestionarSolicitudCUAdapter implements GestionarSolicitudCUIntPort {

    private final GestionarSolicitudGatewayIntPort objGestionarSolicitudGateway;
    private final GestionarCursoOfertadoVeranoGatewayIntPort objCursoOfertado;
    private final GestionarUsuarioGatewayIntPort objUsuario;
    private final GestionarDocumentosGatewayIntPort objDocumentosGateway;
    private final FormateadorResultadosIntPort objFormateadorResultados;

    public GestionarSolicitudCUAdapter(GestionarSolicitudGatewayIntPort objGestionarSolicitudGateway,
    GestionarCursoOfertadoVeranoGatewayIntPort objCursoOfertado,  GestionarUsuarioGatewayIntPort objUsuario, 
    GestionarDocumentosGatewayIntPort objDocumentosGateway,
    FormateadorResultadosIntPort objFormateadorResultados){
        this.objUsuario = objUsuario;
        this.objFormateadorResultados = objFormateadorResultados;
        this.objCursoOfertado = objCursoOfertado;
        this.objDocumentosGateway = objDocumentosGateway;
        this.objGestionarSolicitudGateway = objGestionarSolicitudGateway;
    }
    //Se implementan los métodos con tantas validaciones para tener en cuenta los accesos a servicios por postman o clientes similares.
    @Override
    public SolicitudCursoVeranoPreinscripcion crearSolicitudCursoVeranoPreinscripcion(
            SolicitudCursoVeranoPreinscripcion solicitudCursoVerano) {
        CursoOfertadoVerano cursoABuscar=null;
        SolicitudCursoVeranoPreinscripcion solicitudGuardada = null;
        Usuario usuarioBuscar =null;
        List<Solicitud> solicitudes = null;
        List<EstadoCursoOfertado> estadosCursos = null;
        if(solicitudCursoVerano == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud de preinscripción no puede ser nula");
        }else{
            if(solicitudCursoVerano.getObjUsuario() == null){
                this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud de preinscripción no puede ser nula");
            }else{
                usuarioBuscar = this.objUsuario.obtenerUsuarioPorId(solicitudCursoVerano.getObjUsuario().getId_usuario());
                if(usuarioBuscar !=null){
                    if(solicitudCursoVerano.getDocumentos() != null){
                        if(solicitudCursoVerano.getDocumentos().size() == 0){
                            cursoABuscar = this.objCursoOfertado.obtenerCursoPorId(solicitudCursoVerano.getObjCursoOfertado().getId_curso());
                            if(cursoABuscar != null){
                                estadosCursos = cursoABuscar.getEstadosCursoOfertados();
                                if(estadosCursos.get(estadosCursos.size()-1).getEstado_actual().equals("Publicado")){
                                    solicitudGuardada = this.objGestionarSolicitudGateway.crearSolicitudCursoVeranoPreinscripcion(solicitudCursoVerano);
                                    solicitudes = usuarioBuscar.getSolicitudes();
                                    solicitudes.add(solicitudGuardada);
                                    usuarioBuscar.setSolicitudes(solicitudes);
                                    this.objUsuario.actualizarUsuario(usuarioBuscar);
                                }else{
                                    this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El curso no esta publicado");
                                }
                            }else{
                                this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No hay curso");
                            }
                        }else{
                            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Este proceso no admite documentos");
                        }
                    }else{
                        this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("lista nula");
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
        List<Solicitud> solicitudes = null;
        Documento documento = null;
        List<EstadoCursoOfertado> estadosCursos = null;
        List<EstadoSolicitud> estadosSolicitud = null;
        if(solicitudCursoVerano == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud de preinscripción no puede ser nula");
        }else{
            if(solicitudCursoVerano.getObjUsuario() == null){
                this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud de preinscripción no puede ser nula");
            }else{
                usuarioBuscar = this.objUsuario.obtenerUsuarioPorId(solicitudCursoVerano.getObjUsuario().getId_usuario());
                if(usuarioBuscar !=null){
                    if(solicitudCursoVerano.getDocumentos() != null){
                        if(solicitudCursoVerano.getDocumentos().size() > 0 &&  solicitudCursoVerano.getDocumentos().size() <= 1){
                            documento = solicitudCursoVerano.getDocumentos().get(0);
                            cursoABuscar = this.objCursoOfertado.obtenerCursoPorId(solicitudCursoVerano.getObjCursoOfertado().getId_curso());
                            if(cursoABuscar != null){
                                estadosCursos = cursoABuscar.getEstadosCursoOfertados();
                                if(estadosCursos.get(estadosCursos.size()-1).getEstado_actual().equals("Preinscripcion")){
                                    solicitudPre = this.objGestionarSolicitudGateway.buscarSolicitudesPorUsuarioNombreSolicitudCursoPre(usuarioBuscar.getId_usuario(),SolicitudCursoVeranoPreinscripcion.class.getSimpleName(),cursoABuscar.getId_curso());
                                    estadosSolicitud = solicitudPre.getEstadosSolicitud();
                                    if(estadosSolicitud.get(estadosSolicitud.size()-1).getEstado_actual().equals("Aprobado")){
                                        solicitudGuardada = this.objGestionarSolicitudGateway.crearSolicitudCursoVeranoInscripcion(solicitudCursoVerano);
                                        documento.setObjSolicitud(solicitudGuardada);
                                        this.objDocumentosGateway.crearDocumento(documento);
                                        solicitudes = usuarioBuscar.getSolicitudes();
                                        solicitudes.add(solicitudGuardada);
                                        usuarioBuscar.setSolicitudes(solicitudes);
                                        this.objUsuario.actualizarUsuario(usuarioBuscar);
                                    }else{
                                        this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud de inscripcion no se puede crear porque la solicitud de preinscripcion no fue aprobada");
                                    }
                                }else{
                                    this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El curso no esta publicado");
                                }
                            }else{
                                this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("No hay curso");
                            }
                        }else{
                            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Documentos incompletos o excedentes");
                        }

                    }else{
                        this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Documentos incompletos o excedentes");
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
        Usuario usuarioBuscar = null;
        SolicitudPazYSalvo solicitudGuardada = null;
        List<Documento> documentos = null;
        boolean banderaPP_H = false;
        boolean banderaTI_G = false;
        List<Solicitud> solicitudes = null;
        if(solicitudPazYSalvo == null){     
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud de preinscripción no puede ser nula");
        }else{
            usuarioBuscar = this.objUsuario.obtenerUsuarioPorId(solicitudPazYSalvo.getObjUsuario().getId_usuario());
            if(usuarioBuscar != null){
                documentos = solicitudPazYSalvo.getDocumentos();
                if(documentos != null){
                    if(documentos.size() > 0 && documentos.size() <= 6){
                        for (Documento documento : documentos) {
                            if(documento.getTipoDocumentoSolicitudPazYSalvo() != null){
                                if(documento.getTipoDocumentoSolicitudPazYSalvo().name().equals(TipoDocumentoSolicitudPazYSalvo.formato_PP_H.toString())){
                                    banderaPP_H = true;
                                }
                                if(documento.getTipoDocumentoSolicitudPazYSalvo().name().equals(TipoDocumentoSolicitudPazYSalvo.formato_TI_G.toString())){
                                    banderaTI_G = true;
                                }
                            } 
                        }
                        if(banderaPP_H || banderaTI_G){
                            if(banderaPP_H && banderaTI_G){
                                this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Se ingresaron 2 formatos");
                            }else{
                                solicitudGuardada = this.objGestionarSolicitudGateway.crearSolicitudPazYSalvo(solicitudPazYSalvo);
                                for (Documento documento : documentos) {
                                    documento.setObjSolicitud(solicitudGuardada);
                                    this.objDocumentosGateway.crearDocumento(documento);
                                }
                                solicitudes = usuarioBuscar.getSolicitudes();
                                solicitudes.add(solicitudGuardada);
                                usuarioBuscar.setSolicitudes(solicitudes);
                                this.objUsuario.actualizarUsuario(usuarioBuscar);
                            }
                        }else{
                            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Se debe ingresar almenos uno de los 2 formatos");
                        }

                    }else{
                        this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Formatos incompletos");
                    }
                }else{
                   this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Formatos incompletos");
                }
            }else{
                 this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Usuario no encontrado");
            }
        }
        return solicitudGuardada;
    }

    @Override
    public Solicitud actualizarSolicitud(Solicitud solicitud, EstadoSolicitud estadoSolicitud) {
        Usuario usuarioBuscar = null;
        Solicitud solicitudABuscar = null;
        Solicitud solicitudGuardada = null;
        if(solicitud == null){
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud no puede ser nula");
        }else{
            if(estadoSolicitud == null){
                this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El estado de la solicitud no puede ser nula");
            }else{
                usuarioBuscar = this.objUsuario.obtenerUsuarioPorId(solicitud.getObjUsuario().getId_usuario());
                if(usuarioBuscar !=null){
                    solicitudABuscar = this.objGestionarSolicitudGateway.obtenerSolicitudPorId(solicitud.getId_solicitud());
                    if(solicitudABuscar!=null){
                        solicitudGuardada = this.objGestionarSolicitudGateway.actualizarSolicitud(solicitud, estadoSolicitud);
                    }else{
                        this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Solicitud no existe en el sistema");
                    }

                }else{
                    this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Usuario no encontrado");
                }
            }
        }

        return solicitudGuardada;
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
