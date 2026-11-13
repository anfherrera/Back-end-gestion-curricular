package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadosSolicitud;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudReingresoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.AsociadorDocumentosPendientes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.ListadorSolicitudes;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudReingresoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FiltroSolicitudes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FlujoEstadosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

public class GestionarSolicitudReingresoCUAdapter  implements GestionarSolicitudReingresoCUIntPort {

    private static final FlujoEstadosSolicitud FLUJO = FlujoEstadosSolicitud.TRES_APROBACIONES;

    private final GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway;
    private final GestionarDocumentosGatewayIntPort objGestionarDocumentosGateway;
    private final GestionarEstadoSolicitudGatewayIntPort objGestionarEstadoSolicitudGateway;
    private final FormateadorResultadosIntPort objFormateadorResultados;
    private final GestionarSolicitudReingresoGatewayIntPort objGestionarSolicitudReingresoGateway;
    private final GestionarNotificacionCUIntPort objGestionarNotificacionCU;
    private final AsociadorDocumentosPendientes asociadorDocumentos;
    private final ListadorSolicitudes<SolicitudReingreso> listador;

    public GestionarSolicitudReingresoCUAdapter(GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway,
            GestionarDocumentosGatewayIntPort objGestionarDocumentosGateway,
            GestionarEstadoSolicitudGatewayIntPort objGestionarEstadoSolicitudGateway,
            FormateadorResultadosIntPort objFormateadorResultados,
            GestionarSolicitudReingresoGatewayIntPort objGestionarSolicitudReingresoGateway,
            GestionarNotificacionCUIntPort objGestionarNotificacionCU,
            GestionarArchivosCUIntPort gestionarArchivos) {
        this.objGestionarUsuarioGateway = objGestionarUsuarioGateway;
        this.objGestionarDocumentosGateway = objGestionarDocumentosGateway;
        this.objGestionarEstadoSolicitudGateway = objGestionarEstadoSolicitudGateway;
        this.objFormateadorResultados = objFormateadorResultados;
        this.objGestionarSolicitudReingresoGateway = objGestionarSolicitudReingresoGateway;
        this.objGestionarNotificacionCU = objGestionarNotificacionCU;
        this.asociadorDocumentos = new AsociadorDocumentosPendientes(objGestionarDocumentosGateway, gestionarArchivos);
        this.listador = new ListadorSolicitudes<>(FLUJO, objGestionarSolicitudReingresoGateway);
    }

    @Override
    public SolicitudReingreso crearSolicitudReingreso(SolicitudReingreso solicitud) {
        if(solicitud == null) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Solicitud de homologación no puede ser nula");
        }
        if(solicitud.getId_solicitud()!=null){
            Optional<SolicitudReingreso> solicitudExistente = objGestionarSolicitudReingresoGateway.buscarPorId(solicitud.getId_solicitud());
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

        SolicitudReingreso solicitudGuardada = objGestionarSolicitudReingresoGateway.crearSolicitudReingreso(solicitud);

        // Mover a la carpeta de la solicitud y asociar los documentos que este estudiante subió antes de enviarla
        asociadorDocumentos.asociar(solicitudGuardada, idUsuario, "reingreso");

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
            this.objGestionarNotificacionCU.notificarCreacionSolicitud(solicitudGuardada, "REINGRESO");
        } catch (Exception e) {
        }

        return solicitudGuardada;
    }
    @Override
    public List<SolicitudReingreso> listarSolicitudesReingreso() {
        return objGestionarSolicitudReingresoGateway.listarSolicitudesReingreso();
    }

    @Override
    public List<SolicitudReingreso> listar(FiltroSolicitudes filtro) {
        return listador.listar(filtro);
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesReingresoPorRol(String rol, Integer idUsuario) {
        // BUG-9: este listado nunca incluyó a la secretaría (paz y salvo sí); se conserva hasta decidirlo
        if ("SECRETARIA".equals(rol)) {
            return List.of();
        }
        return listador.listarPorRol(rol, idUsuario);
    }

    @Override
    public SolicitudReingreso obtenerSolicitudReingresoPorId(Integer id) {
        return objGestionarSolicitudReingresoGateway.buscarPorId(id).orElseThrow(() -> new EntidadNoExisteException(
                "Solicitud de reingreso no encontrada con ID: " + id));
    }
    @Override
    public void cambiarEstadoSolicitudReingreso(Integer idSolicitud, String nuevoEstado) {
        // Obtener el estado anterior
        SolicitudReingreso solicitud = obtenerSolicitudReingresoPorId(idSolicitud);
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
        objGestionarSolicitudReingresoGateway.cambiarEstadoSolicitudReingreso(idSolicitud, estado);
        
        // Crear notificación automática del cambio de estado
        try {
            solicitud = obtenerSolicitudReingresoPorId(idSolicitud); // Recargar con el nuevo estado
            this.objGestionarNotificacionCU.notificarCambioEstadoSolicitud(solicitud, estadoAnterior, nuevoEstado, "REINGRESO");
        } catch (Exception e) {
        }
    }

    

    

    
}
