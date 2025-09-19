package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudHomologacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudHomologacionGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

public class GestionarSolicitudHomologacionCUAdapter implements GestionarSolicitudHomologacionCUIntPort {

    private final FormateadorResultadosIntPort objFormateadorResultados;
    private final GestionarSolicitudHomologacionGatewayIntPort objGestionarSolicitudHomologacionGateway;
    private final GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway;
    private final GestionarDocumentosGatewayIntPort objGestionarDocumentosGateway;
    private final GestionarEstadoSolicitudGatewayIntPort objGestionarEstadoSolicitudGateway;

    public GestionarSolicitudHomologacionCUAdapter(FormateadorResultadosIntPort formateadorResultados, GestionarSolicitudHomologacionGatewayIntPort gestionarSolicitudHomologacionGateway
    ,GestionarUsuarioGatewayIntPort gestionarUsuarioGateway, GestionarDocumentosGatewayIntPort gestionarDocumentosGateway, GestionarEstadoSolicitudGatewayIntPort gestionarEstadoSolicitudGateway) {
        this.objFormateadorResultados = formateadorResultados;
        this.objGestionarSolicitudHomologacionGateway = gestionarSolicitudHomologacionGateway;
        this.objGestionarUsuarioGateway = gestionarUsuarioGateway;
        this.objGestionarDocumentosGateway = gestionarDocumentosGateway;
        this.objGestionarEstadoSolicitudGateway = gestionarEstadoSolicitudGateway;
    }
    @Override
    public SolicitudHomologacion guardar(SolicitudHomologacion solicitud) {
       if(solicitud == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Solicitud de homologación no puede ser nula");
        }
        
        if(solicitud.getId_solicitud()!=null){
            Optional<SolicitudHomologacion> solicitudExistente = objGestionarSolicitudHomologacionGateway.buscarPorId(solicitud.getId_solicitud());
            if (solicitudExistente.isPresent()) {
                this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Ya existe una solicitud con el ID: "
                + solicitud.getId_solicitud());
            }
        }

        if(solicitud.getObjUsuario() == null || solicitud.getObjUsuario().getId_usuario() == null){
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("El usuario no puede ser nulo o no tener ID");
        }
        Integer idUsuario = solicitud.getObjUsuario().getId_usuario();
        Optional<Usuario> usuarioOpt = objGestionarUsuarioGateway.buscarUsuarioPorId(idUsuario);
        if (usuarioOpt.isEmpty()) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Usuario ID: " + idUsuario + " no encontrado");
        }

        SolicitudHomologacion solicitudGuardada = objGestionarSolicitudHomologacionGateway.guardar(solicitud);

        //Asociar documentos con solicitud = null
        List<Documento> documentosSinSolicitud = this.objGestionarDocumentosGateway.buscarDocumentoSinSolicitud();
        for (Documento doc : documentosSinSolicitud) {
            //Integer i = 0;
            //Integer idsDocumentos = solicitudGuardada.getDocumentos().get(i).getId_documento();
            doc.setObjSolicitud(solicitudGuardada);
            // if(solicitud.getDocumentos() != null) {
            //     //verificar si necesito el contador i, lo uso para agregar el comentario
            //     doc.setComentario(solicitud.getDocumentos().get(i).getComentario());
            //     //doc.setTipoDocumentoSolicitudPazYSalvo(solicitud.getDocumentos().get(i).getTipoDocumentoSolicitudPazYSalvo()); ;
            // }
                
            this.objGestionarDocumentosGateway.actualizarDocumento(doc);
            //i++;
                
        }

        EstadoSolicitud estadoInicial = new EstadoSolicitud();
        estadoInicial.setEstado_actual("Enviada");//Se pone por defecto el estado de Enviada
        estadoInicial.setFecha_registro_estado(new Date());
        estadoInicial.setObjSolicitud(solicitudGuardada); // establecer vínculo
            
        if(solicitudGuardada.getEstadosSolicitud() == null) {
            solicitudGuardada.setEstadosSolicitud(new ArrayList<>());
        }
        solicitudGuardada.getEstadosSolicitud().add(estadoInicial);
        this.objGestionarEstadoSolicitudGateway.guarEstadoSolicitud(estadoInicial);

        usuarioOpt.get().getSolicitudes().add(solicitudGuardada);
        this.objGestionarUsuarioGateway.actualizarUsuario(usuarioOpt.get());
        

        return solicitudGuardada;
        
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudes() {
        return objGestionarSolicitudHomologacionGateway.listarSolicitudes();
    }

    //========================

    @Override
    public List<SolicitudHomologacion> listarSolicitudesPorRol(String rol, Integer idUsuario) {
        List<SolicitudHomologacion> todas = objGestionarSolicitudHomologacionGateway.listarSolicitudes();

        return todas.stream().filter(solicitud -> {
            List<EstadoSolicitud> estados = solicitud.getEstadosSolicitud();
            EstadoSolicitud ultimoEstado = estados != null && !estados.isEmpty()
                ? estados.get(estados.size() - 1)
                : null;

            switch (rol) {
                case "ESTUDIANTE":
                    return solicitud.getObjUsuario().getId_usuario().equals(idUsuario);
                case "FUNCIONARIO":
                    return ultimoEstado != null && "ENVIADA".equals(ultimoEstado.getEstado_actual());
                case "COORDINADOR":
                    return ultimoEstado != null && "APROBADA_FUNCIONARIO".equals(ultimoEstado.getEstado_actual());
                default:
                    return false;
            }
        }).toList();
    }

    //=======================


    @Override
    public SolicitudHomologacion buscarPorId(Integer idSolicitud) {
        return objGestionarSolicitudHomologacionGateway.buscarPorId(idSolicitud).
        orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
    }

    @Override
    public void cambiarEstadoSolicitud(Integer idSolicitud, String nuevoEstado) {
        EstadoSolicitud estado = new EstadoSolicitud();
        estado.setEstado_actual(nuevoEstado);
        estado.setFecha_registro_estado(new Date());
        objGestionarSolicitudHomologacionGateway.cambiarEstadoSolicitud(idSolicitud, estado);
    }

}
