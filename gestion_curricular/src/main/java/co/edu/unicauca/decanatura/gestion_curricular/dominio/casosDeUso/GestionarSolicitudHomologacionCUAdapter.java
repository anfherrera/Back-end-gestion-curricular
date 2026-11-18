package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudHomologacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudHomologacionGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadYaExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.ReglaNegocioExcepcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FiltroSolicitudes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FlujoEstadosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.EstadosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.AsociadorDocumentosPendientes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.ListadorSolicitudes;

public class GestionarSolicitudHomologacionCUAdapter implements GestionarSolicitudHomologacionCUIntPort {

    private static final FlujoEstadosSolicitud FLUJO = FlujoEstadosSolicitud.TRES_APROBACIONES;

    private final GestionarSolicitudHomologacionGatewayIntPort gestionarSolicitudHomologacionGateway;
    private final GestionarUsuarioGatewayIntPort gestionarUsuarioGateway;
    private final GestionarDocumentosGatewayIntPort gestionarDocumentosGateway;
    private final GestionarEstadoSolicitudGatewayIntPort gestionarEstadoSolicitudGateway;
    private final GestionarNotificacionCUIntPort gestionarNotificacionCU;
    private final AsociadorDocumentosPendientes asociadorDocumentos;
    private final ListadorSolicitudes<SolicitudHomologacion> listador;

    public GestionarSolicitudHomologacionCUAdapter(GestionarSolicitudHomologacionGatewayIntPort gestionarSolicitudHomologacionGateway
    ,GestionarUsuarioGatewayIntPort gestionarUsuarioGateway, GestionarDocumentosGatewayIntPort gestionarDocumentosGateway, GestionarEstadoSolicitudGatewayIntPort gestionarEstadoSolicitudGateway,
    GestionarNotificacionCUIntPort gestionarNotificacionCU, GestionarArchivosCUIntPort gestionarArchivos) {
        this.gestionarSolicitudHomologacionGateway = gestionarSolicitudHomologacionGateway;
        this.gestionarUsuarioGateway = gestionarUsuarioGateway;
        this.gestionarDocumentosGateway = gestionarDocumentosGateway;
        this.gestionarEstadoSolicitudGateway = gestionarEstadoSolicitudGateway;
        this.gestionarNotificacionCU = gestionarNotificacionCU;
        this.asociadorDocumentos = new AsociadorDocumentosPendientes(gestionarDocumentosGateway, gestionarArchivos);
        this.listador = new ListadorSolicitudes<>(FLUJO, gestionarSolicitudHomologacionGateway);
    }
    @Override
    public SolicitudHomologacion guardar(SolicitudHomologacion solicitud) {
       if(solicitud == null) {
            throw new ReglaNegocioExcepcion("Solicitud de homologación no puede ser nula");
        }
        
        if(solicitud.getId_solicitud()!=null){
            Optional<SolicitudHomologacion> solicitudExistente = gestionarSolicitudHomologacionGateway.buscarPorId(solicitud.getId_solicitud());
            if (solicitudExistente.isPresent()) {
                throw new EntidadYaExisteException("Ya existe una solicitud con el ID: "
                + solicitud.getId_solicitud());
            }
        }

        if(solicitud.getObjUsuario() == null || solicitud.getObjUsuario().getId_usuario() == null){
            throw new EntidadYaExisteException("El usuario no puede ser nulo o no tener ID");
        }
        Integer idUsuario = solicitud.getObjUsuario().getId_usuario();
        Optional<Usuario> usuarioOpt = gestionarUsuarioGateway.buscarUsuarioPorId(idUsuario);
        if (usuarioOpt.isEmpty()) {
            throw new ReglaNegocioExcepcion("Usuario ID: " + idUsuario + " no encontrado");
        }

        SolicitudHomologacion solicitudGuardada = gestionarSolicitudHomologacionGateway.guardar(solicitud);

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
        this.gestionarEstadoSolicitudGateway.guarEstadoSolicitud(estadoInicial);
        // No se guarda el usuario: la relación usuario-solicitudes es mappedBy (la solicitud ya
        // lleva su usuario) y, por orphanRemoval, guardar una lista incompleta podría borrar solicitudes.

        // Crear notificación automática para el estudiante
        try {
            this.gestionarNotificacionCU.notificarCreacionSolicitud(solicitudGuardada, "HOMOLOGACION");
        } catch (Exception e) {
        }

        return solicitudGuardada;
        
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudes() {
        return gestionarSolicitudHomologacionGateway.listarSolicitudes();
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
        return gestionarSolicitudHomologacionGateway.buscarPorId(idSolicitud).
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
            throw new ReglaNegocioExcepcion(e.getMessage());
        }

        EstadoSolicitud estado = new EstadoSolicitud();
        estado.setEstado_actual(FLUJO.normalizar(nuevoEstado));
        estado.setFecha_registro_estado(new Date());
        gestionarSolicitudHomologacionGateway.cambiarEstadoSolicitud(idSolicitud, estado);
        
        // Crear notificación automática del cambio de estado
        try {
            solicitud = buscarPorId(idSolicitud); // Recargar con el nuevo estado
            this.gestionarNotificacionCU.notificarCambioEstadoSolicitud(solicitud, estadoAnterior, nuevoEstado, "HOMOLOGACION");
        } catch (Exception e) {
        }
    }
    
    

}
