package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudHomologacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudHomologacionGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

@Slf4j
public class GestionarSolicitudHomologacionCUAdapter implements GestionarSolicitudHomologacionCUIntPort {

    private final FormateadorResultadosIntPort objFormateadorResultados;
    private final GestionarSolicitudHomologacionGatewayIntPort objGestionarSolicitudHomologacionGateway;
    private final GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway;
    private final GestionarDocumentosGatewayIntPort objGestionarDocumentosGateway;
    private final GestionarEstadoSolicitudGatewayIntPort objGestionarEstadoSolicitudGateway;
    private final GestionarNotificacionCUIntPort objGestionarNotificacionCU;
    private final GestionarArchivosCUIntPort gestionarArchivos;

    public GestionarSolicitudHomologacionCUAdapter(FormateadorResultadosIntPort formateadorResultados, GestionarSolicitudHomologacionGatewayIntPort gestionarSolicitudHomologacionGateway
    ,GestionarUsuarioGatewayIntPort gestionarUsuarioGateway, GestionarDocumentosGatewayIntPort gestionarDocumentosGateway, GestionarEstadoSolicitudGatewayIntPort gestionarEstadoSolicitudGateway,
    GestionarNotificacionCUIntPort objGestionarNotificacionCU, GestionarArchivosCUIntPort gestionarArchivos) {
        this.objFormateadorResultados = formateadorResultados;
        this.objGestionarSolicitudHomologacionGateway = gestionarSolicitudHomologacionGateway;
        this.objGestionarUsuarioGateway = gestionarUsuarioGateway;
        this.objGestionarDocumentosGateway = gestionarDocumentosGateway;
        this.objGestionarEstadoSolicitudGateway = gestionarEstadoSolicitudGateway;
        this.objGestionarNotificacionCU = objGestionarNotificacionCU;
        this.gestionarArchivos = gestionarArchivos;
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

        //Asociar documentos con solicitud = null y moverlos a carpeta organizada
        List<Documento> documentosSinSolicitud = this.objGestionarDocumentosGateway.buscarDocumentosSinSolicitud();
        for (Documento doc : documentosSinSolicitud) {
            // Mover archivo a carpeta organizada si está en la raíz (si el archivo existe en disco)
            String rutaActual = doc.getRuta_documento() != null ? doc.getRuta_documento() : doc.getNombre();
            if (rutaActual != null && !rutaActual.contains("/")) {
                try {
                    String nuevaRuta = gestionarArchivos.moverArchivoAOrganizado(
                        rutaActual,
                        doc.getNombre(),
                        "homologacion",
                        solicitudGuardada.getId_solicitud()
                    );
                    if (nuevaRuta != null) {
                        doc.setRuta_documento(nuevaRuta);
                    }
                } catch (Exception e) {
                    log.warn("No se pudo mover el archivo del documento {} a carpeta organizada (archivo no encontrado o error de E/S). Se asocia el documento a la solicitud con la ruta actual. Error: {}", doc.getNombre(), e.getMessage());
                }
            }

            // Asociar documento a la solicitud (siempre, aunque no se haya podido mover el archivo)
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
    public List<SolicitudHomologacion> listarSolicitudesToFuncionario() {
        return objGestionarSolicitudHomologacionGateway.listarSolicitudesToFuncionario();
    }
    @Override
    public List<SolicitudHomologacion> listarSolicitudesToCoordinador() {
        return objGestionarSolicitudHomologacionGateway.listarSolicitudesToCoordinador();
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesToCoordinadorPorPrograma(Integer idPrograma) {
        return objGestionarSolicitudHomologacionGateway.listarSolicitudesToCoordinadorPorPrograma(idPrograma);
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesToFuncionarioPorPeriodo(String periodoAcademico) {
        return objGestionarSolicitudHomologacionGateway.listarSolicitudesToFuncionarioPorPeriodo(periodoAcademico);
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesToCoordinadorPorProgramaYPeriodo(Integer idPrograma, String periodoAcademico) {
        return objGestionarSolicitudHomologacionGateway.listarSolicitudesToCoordinadorPorProgramaYPeriodo(idPrograma, periodoAcademico);
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesToSecretariaPorPeriodo(String periodoAcademico) {
        return objGestionarSolicitudHomologacionGateway.listarSolicitudesToSecretariaPorPeriodo(periodoAcademico);
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesAprobadasToSecretariaPorPeriodo(String periodoAcademico) {
        return objGestionarSolicitudHomologacionGateway.listarSolicitudesAprobadasToSecretariaPorPeriodo(periodoAcademico);
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesPorUsuarioYPeriodo(Integer idUsuario, String periodoAcademico) {
        return objGestionarSolicitudHomologacionGateway.listarSolicitudesPorUsuarioYPeriodo(idUsuario, periodoAcademico);
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesToSecretaria() {
        return objGestionarSolicitudHomologacionGateway.listarSolicitudesToSecretaria();
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesAprobadasToSecretaria() {
        return objGestionarSolicitudHomologacionGateway.listarSolicitudesAprobadasToSecretaria();
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesAprobadasToFuncionario() {
        return objGestionarSolicitudHomologacionGateway.listarSolicitudesAprobadasToFuncionario();
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesAprobadasToFuncionarioPorPeriodo(String periodoAcademico) {
        return objGestionarSolicitudHomologacionGateway.listarSolicitudesAprobadasToFuncionarioPorPeriodo(periodoAcademico);
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesAprobadasToCoordinador() {
        return objGestionarSolicitudHomologacionGateway.listarSolicitudesAprobadasToCoordinador();
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesAprobadasToCoordinadorPorPrograma(Integer idPrograma) {
        return objGestionarSolicitudHomologacionGateway.listarSolicitudesAprobadasToCoordinadorPorPrograma(idPrograma);
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesAprobadasToCoordinadorPorProgramaYPeriodo(Integer idPrograma, String periodoAcademico) {
        return objGestionarSolicitudHomologacionGateway.listarSolicitudesAprobadasToCoordinadorPorProgramaYPeriodo(idPrograma, periodoAcademico);
    }

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


    @Override
    public SolicitudHomologacion buscarPorId(Integer idSolicitud) {
        return objGestionarSolicitudHomologacionGateway.buscarPorId(idSolicitud).
        orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
    }

    @Override
    public void cambiarEstadoSolicitud(Integer idSolicitud, String nuevoEstado) {
        // Obtener el estado anterior
        SolicitudHomologacion solicitud = buscarPorId(idSolicitud);
        String estadoAnterior = solicitud.getEstadosSolicitud() != null && !solicitud.getEstadosSolicitud().isEmpty()
                ? solicitud.getEstadosSolicitud().get(solicitud.getEstadosSolicitud().size() - 1).getEstado_actual()
                : "ENVIADA";
        
        EstadoSolicitud estado = new EstadoSolicitud();
        estado.setEstado_actual(nuevoEstado);
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
