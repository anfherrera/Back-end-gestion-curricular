package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

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
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FiltroSolicitudes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FlujoEstadosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
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
        
        if(solicitud.getIdSolicitud()!=null){
            Optional<SolicitudHomologacion> solicitudExistente = gestionarSolicitudHomologacionGateway.buscarPorId(solicitud.getIdSolicitud());
            if (solicitudExistente.isPresent()) {
                throw new EntidadYaExisteException("Ya existe una solicitud con el ID: "
                + solicitud.getIdSolicitud());
            }
        }

        if(solicitud.getObjUsuario() == null || solicitud.getObjUsuario().getIdUsuario() == null){
            throw new ReglaNegocioExcepcion("El usuario no puede ser nulo o no tener ID");
        }
        Integer idUsuario = solicitud.getObjUsuario().getIdUsuario();
        Optional<Usuario> usuarioOpt = gestionarUsuarioGateway.buscarUsuarioPorId(idUsuario);
        if (usuarioOpt.isEmpty()) {
            throw new EntidadNoExisteException("Usuario ID: " + idUsuario + " no encontrado");
        }

        SolicitudHomologacion solicitudGuardada = gestionarSolicitudHomologacionGateway.guardar(solicitud);

        // Mover a la carpeta de la solicitud y asociar los documentos que este estudiante subió antes de enviarla
        asociadorDocumentos.asociar(solicitudGuardada, idUsuario, "homologacion");

        this.gestionarEstadoSolicitudGateway.guarEstadoSolicitud(solicitudGuardada.registrarEstadoInicial());
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
    public List<SolicitudHomologacion> listar(FiltroSolicitudes filtro) {
        return listador.listar(filtro);
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesPorRol(String rol, Integer idUsuario) {
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

        // Solo se permiten los pasos del flujo (ver FlujoEstadosSolicitud)
        FlujoEstadosSolicitud.Paso paso;
        try {
            paso = solicitud.pasoA(FLUJO, nuevoEstado, null);
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioExcepcion(e.getMessage());
        }

        gestionarSolicitudHomologacionGateway.cambiarEstadoSolicitud(idSolicitud, paso.registro());

        // Crear notificación automática del cambio de estado
        try {
            solicitud = buscarPorId(idSolicitud); // Recargar con el nuevo estado
            this.gestionarNotificacionCU.notificarCambioEstadoSolicitud(solicitud, paso.estadoAnterior(), nuevoEstado, "HOMOLOGACION");
        } catch (Exception e) {
        }
    }
    
    

}
