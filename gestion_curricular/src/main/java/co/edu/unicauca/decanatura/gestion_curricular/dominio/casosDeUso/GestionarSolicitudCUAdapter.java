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
        Solicitud preinscripcionBuscar = null;
        if(solicitudCursoVerano == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud de preinscripción no puede ser nula");
        }
        if(solicitudCursoVerano.getObjUsuario() == null){
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud de preinscripción no puede ser nula");
        }
        if(solicitudCursoVerano.getObjUsuario().getId_usuario() == null){
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El usuario de la solicitud no puede ser nulo");
        }
        usuarioBuscar = this.objUsuario.obtenerUsuarioPorId(solicitudCursoVerano.getObjUsuario().getId_usuario());
        if(usuarioBuscar == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Usuario no encontrado");
        }
        if(!solicitudCursoVerano.getDocumentos().isEmpty()){
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("No se debe adjuntar documentos en la solicitud de preinscripción");
        }
        if(solicitudCursoVerano.getObjCursoOfertadoVerano() == null){
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Debe seleccionar un curso válido");
        }
        if(solicitudCursoVerano.getObjCursoOfertadoVerano().getId_curso() == null){
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Debe seleccionar un curso válido");
        }   
        cursoABuscar = this.objCursoOfertado.obtenerCursoPorId(solicitudCursoVerano.getObjCursoOfertadoVerano().getId_curso());
        if(cursoABuscar == null){   
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("No se encontró el curso");
        }
        if(cursoABuscar.getEstadosCursoOfertados().isEmpty()){
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El curso no tiene estados asociados");
        }
        if(!cursoABuscar.getEstadosCursoOfertados().get(cursoABuscar.getEstadosCursoOfertados().size() - 1).getEstado_actual().equals("Publicado")){
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El curso no está en estado de publicado");
        }

        if(solicitudCursoVerano.getCodicion_solicitud() == null){
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Debe seleccionar una condición de solicitud válida");
        }

        preinscripcionBuscar = this.objGestionarSolicitudGateway.buscarSolicitudesPorUsuarioYCursoPre(usuarioBuscar.getId_usuario(), cursoABuscar.getId_curso());

        if(preinscripcionBuscar != null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Ya existe una solicitud de preinscripción para este curso");
        }

        usuarioBuscar.getSolicitudes().add(solicitudCursoVerano);
        this.objUsuario.actualizarUsuario(usuarioBuscar);

        solicitudCursoVerano.setObjUsuario(usuarioBuscar);

        solicitudGuardada = this.objGestionarSolicitudGateway.crearSolicitudCursoVeranoPreinscripcion(solicitudCursoVerano);

        return solicitudGuardada;
    }


    @Override
    public SolicitudCursoVeranoIncripcion crearSolicitudCursoVeranoInscripcion(SolicitudCursoVeranoIncripcion solicitudCursoVerano) {
        Usuario usuarioBuscar = null;
        CursoOfertadoVerano cursoABuscar = null;
        Solicitud solicitudInscripcionBuscar = null;
        if (solicitudCursoVerano == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud de preinscripción no puede ser nula");
        }
        if (solicitudCursoVerano.getObjUsuario() == null ) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El usuario de la solicitud no puede ser nulo");
        }
        if( solicitudCursoVerano.getObjUsuario().getId_usuario() == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El usuario de la solicitud no puede ser nulo");
        }

        usuarioBuscar = this.objUsuario.obtenerUsuarioPorId(solicitudCursoVerano.getObjUsuario().getId_usuario());
        if (usuarioBuscar == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Usuario no encontrado");
        }
        if (solicitudCursoVerano.getDocumentos() == null || solicitudCursoVerano.getDocumentos().isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Debe adjuntar exactamente un documento");
        }
        if(solicitudCursoVerano.getDocumentos().size() != 1) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Debe adjuntar exactamente un documento");
        }
        if (solicitudCursoVerano.getObjCursoOfertadoVerano() == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Debe seleccionar un curso válido");
        }
        if(solicitudCursoVerano.getObjCursoOfertadoVerano().getId_curso() == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Debe seleccionar un curso válido");
        }

        cursoABuscar = this.objCursoOfertado.obtenerCursoPorId(solicitudCursoVerano.getObjCursoOfertadoVerano().getId_curso());
        if (cursoABuscar == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("No se encontró el curso");
        }

        if(cursoABuscar.getEstadosCursoOfertados().isEmpty()){
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El curso no tiene estados asociados");
        }
        if(!cursoABuscar.getEstadosCursoOfertados().get(cursoABuscar.getEstadosCursoOfertados().size()-1).getEstado_actual().equals("Preinscripcion")){
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El curso no está en estado de Preinscripcion");
        }

        solicitudInscripcionBuscar = this.objGestionarSolicitudGateway.buscarSolicitudesPorUsuarioYCursoIns(usuarioBuscar.getId_usuario(), cursoABuscar.getId_curso());

        if(solicitudInscripcionBuscar != null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Ya existe una solicitud de preinscripción para este curso");
        }

        // Crear la solicitud
        SolicitudCursoVeranoIncripcion solicitudGuardada = this.objGestionarSolicitudGateway.crearSolicitudCursoVeranoInscripcion(solicitudCursoVerano);

        // Asociar y guardar los documentos
        for (Documento doc : solicitudGuardada.getDocumentos()) {
            doc.setObjSolicitud(solicitudGuardada);
            this.objDocumentosGateway.actualizarDocumento(doc);
        }

        // Actualizar usuario con la nueva solicitud
        usuarioBuscar.getSolicitudes().add(solicitudGuardada);
  
        this.objUsuario.actualizarUsuario(usuarioBuscar);

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


    
    // @Override
    // public SolicitudPazYSalvo crearSolicitudPazYSalvo(SolicitudPazYSalvo solicitudPazYSalvo) {
    //     if (solicitudPazYSalvo == null) {
    //         this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud no puede ser nula");
    //     }

    //     if (solicitudPazYSalvo.getObjUsuario() == null) {
    //         this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El usuario no puede ser nulo");
    //     }
    //     if(solicitudPazYSalvo.getObjUsuario().getId_usuario() == null) {
    //         this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("El usuario no puede ser nulo");

    //     }
    //     Usuario usuarioBuscar = this.objUsuario.obtenerUsuarioPorId(solicitudPazYSalvo.getObjUsuario().getId_usuario());
    //     if (usuarioBuscar == null) {
    //         this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Usuario no encontrado");
    //     }

    //     List<Documento> documentos = solicitudPazYSalvo.getDocumentos();
    //     if (documentos == null || documentos.isEmpty() || documentos.size() > 6) {
    //         this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Se deben adjuntar entre 1 y 6 documentos");
    //     }

    //     boolean contienePP_H = false;
    //     boolean contieneTI_G = false;

    //     for (Documento doc : documentos) {
    //         if(doc.getTipoDocumentoSolicitudPazYSalvo() == null){
    //             this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("No hay un tipo de documento");
    //         }
    //         String tipo = doc.getTipoDocumentoSolicitudPazYSalvo().name();
    //         if (tipo.equals(TipoDocumentoSolicitudPazYSalvo.formato_PP_H.name())) {
    //             contienePP_H = true;
    //         }
    //         if (tipo.equals(TipoDocumentoSolicitudPazYSalvo.formato_TI_G.name())) {
    //             contieneTI_G = true;
    //         }

    //     }

    //     if (!contienePP_H && !contieneTI_G) {
    //         this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Se debe ingresar al menos uno de los dos formatos: PP_H o TI_G");
    //     }

    //     if (contienePP_H && contieneTI_G) {
    //         this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Se ingresaron ambos formatos. Solo se debe adjuntar uno");
    //     }

    //     // Crear la solicitud
    //     SolicitudPazYSalvo solicitudGuardada = this.objGestionarSolicitudGateway.crearSolicitudPazYSalvo(solicitudPazYSalvo);

    //     // Asociar y guardar los documentos
    //     for (Documento doc : solicitudGuardada.getDocumentos()) {
    //         doc.setObjSolicitud(solicitudGuardada);
    //         this.objDocumentosGateway.actualizarDocumento(doc);
    //     }

    //     // Asociar solicitud al usuario
    //     usuarioBuscar.getSolicitudes().add(solicitudGuardada);
    //     this.objUsuario.actualizarUsuario(usuarioBuscar);

    //     return solicitudGuardada;
    // }

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
