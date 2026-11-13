package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadosSolicitud;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudHomologacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.AsociadorDocumentosPendientes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.ListadorSolicitudes;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudHomologacionGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FiltroSolicitudes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FlujoEstadosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

public class GestionarSolicitudHomologacionCUAdapter implements GestionarSolicitudHomologacionCUIntPort {

    private static final FlujoEstadosSolicitud FLUJO = FlujoEstadosSolicitud.TRES_APROBACIONES;

    private final FormateadorResultadosIntPort objFormateadorResultados;
    private final GestionarSolicitudHomologacionGatewayIntPort objGestionarSolicitudHomologacionGateway;
    private final GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway;
    private final GestionarDocumentosGatewayIntPort objGestionarDocumentosGateway;
    private final GestionarEstadoSolicitudGatewayIntPort objGestionarEstadoSolicitudGateway;
    private final GestionarNotificacionCUIntPort objGestionarNotificacionCU;
    private final AsociadorDocumentosPendientes asociadorDocumentos;
    private final ListadorSolicitudes<SolicitudHomologacion> listador;

    public GestionarSolicitudHomologacionCUAdapter(FormateadorResultadosIntPort formateadorResultados, GestionarSolicitudHomologacionGatewayIntPort gestionarSolicitudHomologacionGateway
    ,GestionarUsuarioGatewayIntPort gestionarUsuarioGateway, GestionarDocumentosGatewayIntPort gestionarDocumentosGateway, GestionarEstadoSolicitudGatewayIntPort gestionarEstadoSolicitudGateway,
    GestionarNotificacionCUIntPort objGestionarNotificacionCU, GestionarArchivosCUIntPort gestionarArchivos) {
        this.objFormateadorResultados = formateadorResultados;
        this.objGestionarSolicitudHomologacionGateway = gestionarSolicitudHomologacionGateway;
        this.objGestionarUsuarioGateway = gestionarUsuarioGateway;
        this.objGestionarDocumentosGateway = gestionarDocumentosGateway;
        this.objGestionarEstadoSolicitudGateway = gestionarEstadoSolicitudGateway;
        this.objGestionarNotificacionCU = objGestionarNotificacionCU;
        this.asociadorDocumentos = new AsociadorDocumentosPendientes(gestionarDocumentosGateway, gestionarArchivos);
        this.listador = new ListadorSolicitudes<>(FLUJO, gestionarSolicitudHomologacionGateway);
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

        // Mover a la carpeta de la solicitud y asociar los documentos que este estudiante subió antes de enviarla
        asociadorDocumentos.asociar(solicitudGuardada, idUsuario, "homologacion");

        EstadoSolicitud estadoInicial = new EstadoSolicitud();
        estadoInicial.setEstado_actual(EstadosSolicitud.ENVIADA.textoGuardado());//Se pone por defecto el estado de Enviada
        estadoInicial.setFecha_registro_estado(new Date());
        estadoInicial.setObjSolicitud(solicitudGuardada); // establecer vínculo
            
        if(solicitudGuardada.getEstadosSolicitud() == null) {
            solicitudGuardada.setEstadosSolicitud(new ArrayList<>());
        }
        solicitudGuardada.getEstadosSolicitud().add(estadoInicial);
        this.objGestionarEstadoSolicitudGateway.guarEstadoSolicitud(estadoInicial);
        // No se guarda el usuario: la relación usuario-solicitudes es mappedBy (la solicitud ya
        // lleva su usuario) y, por orphanRemoval, guardar una lista incompleta podría borrar solicitudes.

        // Crear notificación automática para el estudiante
        try {
            this.objGestionarNotificacionCU.notificarCreacionSolicitud(solicitudGuardada, "HOMOLOGACION");
        } catch (Exception e) {
        }

        return solicitudGuardada;
        
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudes() {
        return objGestionarSolicitudHomologacionGateway.listarSolicitudes();
    }

    @Override
    public List<SolicitudHomologacion> listar(FiltroSolicitudes filtro) {
        return listador.listar(filtro);
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesPorRol(String rol, Integer idUsuario) {
        // BUG-9: este listado nunca incluyó a la secretaría (paz y salvo sí); se conserva hasta decidirlo
        if ("SECRETARIA".equals(rol)) {
            return List.of();
        }
        return listador.listarPorRol(rol, idUsuario);
    }

    @Override
    public SolicitudHomologacion buscarPorId(Integer idSolicitud) {
        return objGestionarSolicitudHomologacionGateway.buscarPorId(idSolicitud).
        orElseThrow(() -> new EntidadNoExisteException(
                "Solicitud de homologación no encontrada con ID: " + idSolicitud));
    }

    @Override
    public void cambiarEstadoSolicitud(Integer idSolicitud, String nuevoEstado) {
        // Obtener el estado anterior
        SolicitudHomologacion solicitud = buscarPorId(idSolicitud);
        String estadoAnterior = FlujoEstadosSolicitud.ultimoEstado(solicitud.getEstadosSolicitud());

        // Solo se permiten los pasos del flujo (ver FlujoEstadosSolicitud)
        try {
            FLUJO.validarYObtenerRolResponsable(estadoAnterior, nuevoEstado);
        } catch (IllegalArgumentException e) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio(e.getMessage());
        }

        EstadoSolicitud estado = new EstadoSolicitud();
        estado.setEstado_actual(FLUJO.normalizar(nuevoEstado));
        estado.setFecha_registro_estado(new Date());
        objGestionarSolicitudHomologacionGateway.cambiarEstadoSolicitud(idSolicitud, estado);
        
        // Crear notificación automática del cambio de estado
        try {
            solicitud = buscarPorId(idSolicitud); // Recargar con el nuevo estado
            this.objGestionarNotificacionCU.notificarCambioEstadoSolicitud(solicitud, estadoAnterior, nuevoEstado, "HOMOLOGACION");
        } catch (Exception e) {
        }
    }
    
    

}
