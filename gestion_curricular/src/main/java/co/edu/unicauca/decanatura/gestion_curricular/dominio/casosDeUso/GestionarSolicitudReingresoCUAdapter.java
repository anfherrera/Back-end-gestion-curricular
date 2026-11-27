package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudReingresoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudReingresoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadYaExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.ReglaNegocioExcepcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FiltroSolicitudes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FlujoEstadosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.AsociadorDocumentosPendientes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.ListadorSolicitudes;

public class GestionarSolicitudReingresoCUAdapter  implements GestionarSolicitudReingresoCUIntPort {

    private static final FlujoEstadosSolicitud FLUJO = FlujoEstadosSolicitud.TRES_APROBACIONES;

    private final GestionarUsuarioGatewayIntPort gestionarUsuarioGateway;
    private final GestionarEstadoSolicitudGatewayIntPort gestionarEstadoSolicitudGateway;
    private final GestionarSolicitudReingresoGatewayIntPort gestionarSolicitudReingresoGateway;
    private final GestionarNotificacionCUIntPort gestionarNotificacionCU;
    private final AsociadorDocumentosPendientes asociadorDocumentos;
    private final ListadorSolicitudes<SolicitudReingreso> listador;

    public GestionarSolicitudReingresoCUAdapter(GestionarUsuarioGatewayIntPort gestionarUsuarioGateway,
            GestionarDocumentosGatewayIntPort gestionarDocumentosGateway,
            GestionarEstadoSolicitudGatewayIntPort gestionarEstadoSolicitudGateway,
            GestionarSolicitudReingresoGatewayIntPort gestionarSolicitudReingresoGateway,
            GestionarNotificacionCUIntPort gestionarNotificacionCU,
            GestionarArchivosCUIntPort gestionarArchivos) {
        this.gestionarUsuarioGateway = gestionarUsuarioGateway;
        this.gestionarEstadoSolicitudGateway = gestionarEstadoSolicitudGateway;
        this.gestionarSolicitudReingresoGateway = gestionarSolicitudReingresoGateway;
        this.gestionarNotificacionCU = gestionarNotificacionCU;
        this.asociadorDocumentos = new AsociadorDocumentosPendientes(gestionarDocumentosGateway, gestionarArchivos);
        this.listador = new ListadorSolicitudes<>(FLUJO, gestionarSolicitudReingresoGateway);
    }

    @Override
    public SolicitudReingreso crearSolicitudReingreso(SolicitudReingreso solicitud) {
        if(solicitud == null) {
            throw new ReglaNegocioExcepcion("Solicitud de reingreso no puede ser nula");
        }
        if(solicitud.getIdSolicitud()!=null){
            Optional<SolicitudReingreso> solicitudExistente = gestionarSolicitudReingresoGateway.buscarPorId(solicitud.getIdSolicitud());
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

        SolicitudReingreso solicitudGuardada = gestionarSolicitudReingresoGateway.crearSolicitudReingreso(solicitud);

        // Mover a la carpeta de la solicitud y asociar los documentos que este estudiante subió antes de enviarla
        asociadorDocumentos.asociar(solicitudGuardada, idUsuario, "reingreso");

        this.gestionarEstadoSolicitudGateway.guarEstadoSolicitud(solicitudGuardada.registrarEstadoInicial());
        // No se guarda el usuario: la relación usuario-solicitudes es mappedBy (la solicitud ya
        // lleva su usuario) y, por orphanRemoval, guardar una lista incompleta podría borrar solicitudes.

        // Crear notificación automática para el estudiante
        try {
            this.gestionarNotificacionCU.notificarCreacionSolicitud(solicitudGuardada, "REINGRESO");
        } catch (Exception e) {
        }

        return solicitudGuardada;
    }
    @Override
    public List<SolicitudReingreso> listar(FiltroSolicitudes filtro) {
        return listador.listar(filtro);
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesReingresoPorRol(String rol, Integer idUsuario) {
        return listador.listarPorRol(rol, idUsuario);
    }

    @Override
    public SolicitudReingreso obtenerSolicitudReingresoPorId(Integer id) {
        return gestionarSolicitudReingresoGateway.buscarPorId(id).orElseThrow(() -> new EntidadNoExisteException(
                "Solicitud de reingreso no encontrada con ID: " + id));
    }
    @Override
    public void cambiarEstadoSolicitudReingreso(Integer idSolicitud, String nuevoEstado) {
        // Obtener el estado anterior
        SolicitudReingreso solicitud = obtenerSolicitudReingresoPorId(idSolicitud);

        // Solo se permiten los pasos del flujo (ver FlujoEstadosSolicitud)
        FlujoEstadosSolicitud.Paso paso;
        try {
            paso = solicitud.pasoA(FLUJO, nuevoEstado, null);
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioExcepcion(e.getMessage());
        }

        gestionarSolicitudReingresoGateway.cambiarEstadoSolicitudReingreso(idSolicitud, paso.registro());

        // Crear notificación automática del cambio de estado
        try {
            solicitud = obtenerSolicitudReingresoPorId(idSolicitud); // Recargar con el nuevo estado
            this.gestionarNotificacionCU.notificarCambioEstadoSolicitud(solicitud, paso.estadoAnterior(), nuevoEstado, "REINGRESO");
        } catch (Exception e) {
        }
    }

    

    

    
}
