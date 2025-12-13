package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudReingresoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudReingresoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

public class GestionarSolicitudReingresoCUAdapter  implements GestionarSolicitudReingresoCUIntPort {

    private final GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway;
    private final GestionarDocumentosGatewayIntPort objGestionarDocumentosGateway;
    private final GestionarEstadoSolicitudGatewayIntPort objGestionarEstadoSolicitudGateway;
    private final FormateadorResultadosIntPort objFormateadorResultados;
    private final GestionarSolicitudReingresoGatewayIntPort objGestionarSolicitudReingresoGateway;
    private final GestionarNotificacionCUIntPort objGestionarNotificacionCU;

    public GestionarSolicitudReingresoCUAdapter(GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway,
            GestionarDocumentosGatewayIntPort objGestionarDocumentosGateway,
            GestionarEstadoSolicitudGatewayIntPort objGestionarEstadoSolicitudGateway,
            FormateadorResultadosIntPort objFormateadorResultados,
            GestionarSolicitudReingresoGatewayIntPort objGestionarSolicitudReingresoGateway,
            GestionarNotificacionCUIntPort objGestionarNotificacionCU) {
        this.objGestionarUsuarioGateway = objGestionarUsuarioGateway;
        this.objGestionarDocumentosGateway = objGestionarDocumentosGateway;
        this.objGestionarEstadoSolicitudGateway = objGestionarEstadoSolicitudGateway;
        this.objFormateadorResultados = objFormateadorResultados;
        this.objGestionarSolicitudReingresoGateway = objGestionarSolicitudReingresoGateway;
        this.objGestionarNotificacionCU = objGestionarNotificacionCU;
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

        //Asociar documentos con solicitud = null
        List<Documento> documentosSinSolicitud = this.objGestionarDocumentosGateway.buscarDocumentosSinSolicitud();
        for (Documento doc : documentosSinSolicitud) {
            doc.setObjSolicitud(solicitudGuardada);           
            this.objGestionarDocumentosGateway.actualizarDocumento(doc);            
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
        
        // Crear notificación automática para el estudiante
        try {
            this.objGestionarNotificacionCU.notificarCreacionSolicitud(solicitudGuardada, "REINGRESO");
        } catch (Exception e) {
            // Log del error pero no interrumpir el flujo principal
            System.err.println("Error al crear notificación: " + e.getMessage());
            e.printStackTrace();
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
    public List<SolicitudReingreso> listarSolicitudesAprobadasToSecretaria() {
        return objGestionarSolicitudReingresoGateway.listarSolicitudesAprobadasToSecretaria();
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
        return objGestionarSolicitudReingresoGateway.buscarPorId(id).orElseThrow(() -> new RuntimeException("Solicitud de reingreso no encontrada con ID: " + id));
    }
    @Override
    public void cambiarEstadoSolicitudReingreso(Integer idSolicitud, String nuevoEstado) {
        // Obtener el estado anterior
        SolicitudReingreso solicitud = obtenerSolicitudReingresoPorId(idSolicitud);
        String estadoAnterior = solicitud.getEstadosSolicitud() != null && !solicitud.getEstadosSolicitud().isEmpty()
                ? solicitud.getEstadosSolicitud().get(solicitud.getEstadosSolicitud().size() - 1).getEstado_actual()
                : "ENVIADA";
        
        EstadoSolicitud estado = new EstadoSolicitud();
        estado.setEstado_actual(nuevoEstado);
        estado.setFecha_registro_estado(new Date());
        objGestionarSolicitudReingresoGateway.cambiarEstadoSolicitudReingreso(idSolicitud, estado);
        
        // Crear notificación automática del cambio de estado
        try {
            solicitud = obtenerSolicitudReingresoPorId(idSolicitud); // Recargar con el nuevo estado
            this.objGestionarNotificacionCU.notificarCambioEstadoSolicitud(solicitud, estadoAnterior, nuevoEstado, "REINGRESO");
        } catch (Exception e) {
            // Log del error pero no interrumpir el flujo principal
            System.err.println("Error al crear notificación de cambio de estado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    

    

    
}
