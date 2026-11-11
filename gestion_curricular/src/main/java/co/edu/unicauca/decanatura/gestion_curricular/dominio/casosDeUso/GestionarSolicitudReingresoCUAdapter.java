package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudReingresoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.AsociadorDocumentosPendientes;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudReingresoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
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
    public List<SolicitudReingreso> listarSolicitudesReingresoToFuncionario() {
        return objGestionarSolicitudReingresoGateway.listarSolicitudesReingresoToFuncionario();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesReingresoToSecretaria() {
        return objGestionarSolicitudReingresoGateway.listarSolicitudesReingresoToSecretaria();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesReingresoToCoordinador() {
        return objGestionarSolicitudReingresoGateway.listarSolicitudesReingresoToCoordinador();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesReingresoToCoordinadorPorPrograma(Integer idPrograma) {
        return objGestionarSolicitudReingresoGateway.listarSolicitudesReingresoToCoordinadorPorPrograma(idPrograma);
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesReingresoToFuncionarioPorPeriodo(String periodoAcademico) {
        return objGestionarSolicitudReingresoGateway.listarSolicitudesReingresoToFuncionarioPorPeriodo(periodoAcademico);
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesReingresoToCoordinadorPorProgramaYPeriodo(Integer idPrograma, String periodoAcademico) {
        return objGestionarSolicitudReingresoGateway.listarSolicitudesReingresoToCoordinadorPorProgramaYPeriodo(idPrograma, periodoAcademico);
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesReingresoToSecretariaPorPeriodo(String periodoAcademico) {
        return objGestionarSolicitudReingresoGateway.listarSolicitudesReingresoToSecretariaPorPeriodo(periodoAcademico);
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesAprobadasToSecretariaPorPeriodo(String periodoAcademico) {
        return objGestionarSolicitudReingresoGateway.listarSolicitudesAprobadasToSecretariaPorPeriodo(periodoAcademico);
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesReingresoPorUsuarioYPeriodo(Integer idUsuario, String periodoAcademico) {
        return objGestionarSolicitudReingresoGateway.listarSolicitudesReingresoPorUsuarioYPeriodo(idUsuario, periodoAcademico);
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesAprobadasToSecretaria() {
        return objGestionarSolicitudReingresoGateway.listarSolicitudesAprobadasToSecretaria();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesAprobadasToFuncionario() {
        return objGestionarSolicitudReingresoGateway.listarSolicitudesAprobadasToFuncionario();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesAprobadasToFuncionarioPorPeriodo(String periodoAcademico) {
        return objGestionarSolicitudReingresoGateway.listarSolicitudesAprobadasToFuncionarioPorPeriodo(periodoAcademico);
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesAprobadasToCoordinador() {
        return objGestionarSolicitudReingresoGateway.listarSolicitudesAprobadasToCoordinador();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesAprobadasToCoordinadorPorPrograma(Integer idPrograma) {
        return objGestionarSolicitudReingresoGateway.listarSolicitudesAprobadasToCoordinadorPorPrograma(idPrograma);
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesAprobadasToCoordinadorPorProgramaYPeriodo(Integer idPrograma, String periodoAcademico) {
        return objGestionarSolicitudReingresoGateway.listarSolicitudesAprobadasToCoordinadorPorProgramaYPeriodo(idPrograma, periodoAcademico);
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesReingresoPorRol(String rol, Integer idUsuario) {
       List<SolicitudReingreso> todas = objGestionarSolicitudReingresoGateway.listarSolicitudesReingreso();

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
