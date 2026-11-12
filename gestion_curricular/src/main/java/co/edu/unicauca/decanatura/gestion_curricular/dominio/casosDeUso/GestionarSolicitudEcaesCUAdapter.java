package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudEcaesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.AsociadorDocumentosPendientes;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarPreRegistroEcaesGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FlujoEstadosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FechaEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class GestionarSolicitudEcaesCUAdapter implements GestionarSolicitudEcaesCUIntPort {

    private final GestionarPreRegistroEcaesGatewayIntPort objGestionarSolicitudEcaesGateway;
    private final FormateadorResultadosIntPort objFormateadorResultados;
    private final GestionarDocumentosGatewayIntPort objDocumentosGateway;
    private final GestionarEstadoSolicitudGatewayIntPort objGestionarEstadoSolicitudGateway;
    private final GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway;
    private final GestionarNotificacionCUIntPort objGestionarNotificacionCU;
    private final AsociadorDocumentosPendientes asociadorDocumentos;


    public GestionarSolicitudEcaesCUAdapter(GestionarPreRegistroEcaesGatewayIntPort objGestionarSolicitudEcaesGateway,
            FormateadorResultadosIntPort objFormateadorResultados,
            GestionarDocumentosGatewayIntPort objDocumentosGateway,
            GestionarEstadoSolicitudGatewayIntPort objGestionarEstadoSolicitudGateway,
            GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway,
            GestionarNotificacionCUIntPort objGestionarNotificacionCU,
            GestionarArchivosCUIntPort gestionarArchivos
            ) {
        this.objGestionarSolicitudEcaesGateway = objGestionarSolicitudEcaesGateway;
        this.objFormateadorResultados = objFormateadorResultados;
        this.objDocumentosGateway = objDocumentosGateway;   
        this.objGestionarEstadoSolicitudGateway = objGestionarEstadoSolicitudGateway;
        this.objGestionarUsuarioGateway = objGestionarUsuarioGateway;
        this.objGestionarNotificacionCU = objGestionarNotificacionCU;
        this.asociadorDocumentos = new AsociadorDocumentosPendientes(objDocumentosGateway, gestionarArchivos);
    
    }

   
        @Override
        public SolicitudEcaes guardar(SolicitudEcaes solicitud) {
            if(solicitud == null) {
                this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud no puede ser nula");
            }

            if(solicitud.getId_solicitud()!=null){
                Optional<SolicitudEcaes> solicitudExistente = objGestionarSolicitudEcaesGateway.buscarOpcionalPorId(solicitud.getId_solicitud());
                if (solicitudExistente.isPresent()) {
                    this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Ya existe una solicitud con el ID: "
                    + solicitud.getId_solicitud());
                    //return null;
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


            SolicitudEcaes solicitudGuardada = this.objGestionarSolicitudEcaesGateway.guardar(solicitud);

            EstadoSolicitud estadoInicial = new EstadoSolicitud();
            estadoInicial.setEstado_actual("Enviada");//Se pone por defecto el estado de Enviada
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
                this.objGestionarNotificacionCU.notificarCreacionSolicitud(solicitudGuardada, "ECAES");
            } catch (Exception e) {
            }

            // Asociar y mover documentos DESPUÉS de estado/usuario/notificación: si algo falla antes,
            // Mover a la carpeta de la solicitud y asociar los documentos que este estudiante subió antes de enviarla
            asociadorDocumentos.asociar(solicitudGuardada, idUsuario, "ecaes");

            return solicitudGuardada;
    
        }

    @Override
    public List<SolicitudEcaes> listarSolicitudes() {
        return objGestionarSolicitudEcaesGateway.listar();
    }
    @Override
    public SolicitudEcaes buscarPorId(Integer idSolicitud) {
        return objGestionarSolicitudEcaesGateway.buscarPorId(idSolicitud)
            .orElseThrow(() -> new EntidadNoExisteException("Solicitud no encontrada con ID: " + idSolicitud));    
    }
    @Override
    public void cambiarEstadoSolicitud(Integer idSolicitud, String nuevoEstado) {
        // Obtener el estado anterior
        SolicitudEcaes solicitud = buscarPorId(idSolicitud);
        String estadoAnterior = FlujoEstadosSolicitud.ultimoEstado(solicitud.getEstadosSolicitud());

        // Solo se permiten los pasos del flujo (ver FlujoEstadosSolicitud.ECAES)
        try {
            FlujoEstadosSolicitud.ECAES.validarYObtenerRolResponsable(estadoAnterior, nuevoEstado);
        } catch (IllegalArgumentException e) {
            this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio(e.getMessage());
        }

        EstadoSolicitud nuevo = new EstadoSolicitud();
        nuevo.setEstado_actual(FlujoEstadosSolicitud.ECAES.normalizar(nuevoEstado));
        nuevo.setFecha_registro_estado(new Date());

        objGestionarSolicitudEcaesGateway.cambiarEstadoSolicitudEcaes(idSolicitud, nuevo);
        
        // Crear notificación automática del cambio de estado
        try {
            solicitud = buscarPorId(idSolicitud); // Recargar con el nuevo estado
            this.objGestionarNotificacionCU.notificarCambioEstadoSolicitud(solicitud, estadoAnterior, nuevoEstado, "ECAES");
        } catch (Exception e) {
        }
    }

    @Override
    public FechaEcaes publicarFechasEcaes(FechaEcaes fechasEcaes) {
        return objGestionarSolicitudEcaesGateway.publicarFechasEcaes(fechasEcaes);
    }

    @Override
    public List<FechaEcaes> listarFechasEcaes() {
        return objGestionarSolicitudEcaesGateway.listarFechasEcaes();
    }

    @Override
    public List<SolicitudEcaes> listarSolicitudesToFuncionario() {
        return objGestionarSolicitudEcaesGateway.listarSolicitudesToFuncionario();
    }

    @Override
    public List<SolicitudEcaes> listarSolicitudesToFuncionarioPorPeriodo(String periodoAcademico) {
        return objGestionarSolicitudEcaesGateway.listarSolicitudesToFuncionarioPorPeriodo(periodoAcademico);
    }

    @Override
    public List<SolicitudEcaes> listarSolicitudesToCoordinadorPorProgramaYPeriodo(Integer idPrograma, String periodoAcademico) {
        return objGestionarSolicitudEcaesGateway.listarSolicitudesToCoordinadorPorProgramaYPeriodo(idPrograma, periodoAcademico);
    }

    @Override
    public List<SolicitudEcaes> listarSolicitudesPorUsuarioYPeriodo(Integer idUsuario, String periodoAcademico) {
        return objGestionarSolicitudEcaesGateway.listarSolicitudesPorUsuarioYPeriodo(idUsuario, periodoAcademico);
    }

    @Override
    public List<SolicitudEcaes> listarSolicitudesPorRol(String rol, Integer idUsuario) {
        List<SolicitudEcaes> todas = objGestionarSolicitudEcaesGateway.listar();

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
                default:
                    return false;
            }
        }).toList();
    }

    @Override
    public Optional<FechaEcaes> buscarFechasPorPeriodo(String periodoAcademico) {
        return objGestionarSolicitudEcaesGateway.buscarFechasPorPeriodo(periodoAcademico);
    }

    @Override
    public FechaEcaes actualizarFechasEcaes(FechaEcaes fechasEcaes) {
        return objGestionarSolicitudEcaesGateway.actualizarFechasEcaes(fechasEcaes);
    }

}
