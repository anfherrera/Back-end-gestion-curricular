package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudPazYSalvoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudPazYSalvoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.controladorExcepciones.excepcionesPropias.EntidadNoExisteException;

public class GestionarSolicitudPazYSalvoCUAdapter implements GestionarSolicitudPazYSalvoCUIntPort {

    private final GestionarSolicitudPazYSalvoGatewayIntPort solicitudGateway;
    private final GestionarUsuarioGatewayIntPort usuarioGateway;
    private final GestionarDocumentosGatewayIntPort documentosGateway;
    private final GestionarEstadoSolicitudGatewayIntPort estadoSolicitudGateway;
    private final FormateadorResultadosIntPort formateadorResultados;
    private final GestionarArchivosCUIntPort gestionarArchivos;
    private final GestionarNotificacionCUIntPort objGestionarNotificacionCU;

    public GestionarSolicitudPazYSalvoCUAdapter(
            GestionarSolicitudPazYSalvoGatewayIntPort solicitudGateway,
            GestionarUsuarioGatewayIntPort usuarioGateway,
            GestionarDocumentosGatewayIntPort documentosGateway,
            GestionarEstadoSolicitudGatewayIntPort estadoSolicitudGateway,
            FormateadorResultadosIntPort formateadorResultados,
            GestionarArchivosCUIntPort gestionarArchivos,
            GestionarNotificacionCUIntPort objGestionarNotificacionCU) {
        this.solicitudGateway = solicitudGateway;
        this.usuarioGateway = usuarioGateway;
        this.documentosGateway = documentosGateway;
        this.estadoSolicitudGateway = estadoSolicitudGateway;
        this.formateadorResultados = formateadorResultados;
        this.gestionarArchivos = gestionarArchivos;
        this.objGestionarNotificacionCU = objGestionarNotificacionCU;
    }

    @Override
    public SolicitudPazYSalvo guardar(SolicitudPazYSalvo solicitud) {
        if (solicitud == null) {
            formateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud no puede ser nula");
        }

        if (solicitud.getObjUsuario() == null || solicitud.getObjUsuario().getId_usuario() == null) {
            formateadorResultados.retornarRespuestaErrorReglaDeNegocio("El usuario no puede ser nulo o sin ID");
        }

        Usuario usuario = usuarioGateway.obtenerUsuarioPorId(solicitud.getObjUsuario().getId_usuario());
        if (usuario == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Usuario no encontrado");
        }

        // Guardar solicitud
        SolicitudPazYSalvo solicitudGuardada = solicitudGateway.guardar(solicitud);

        // Asociar documentos sin solicitud y moverlos a carpeta organizada
        List<Documento> documentosSinSolicitud = documentosGateway.buscarDocumentosSinSolicitud();
        for (Documento doc : documentosSinSolicitud) {
            // Mover archivo a carpeta organizada si está en la raíz
            String rutaActual = doc.getRuta_documento() != null ? doc.getRuta_documento() : doc.getNombre();
            if (rutaActual != null && !rutaActual.contains("/")) {
                // El archivo está en la raíz, moverlo a carpeta organizada
                String nuevaRuta = gestionarArchivos.moverArchivoAOrganizado(
                    rutaActual, 
                    doc.getNombre(), 
                    "pazysalvo", 
                    solicitudGuardada.getId_solicitud()
                );
                if (nuevaRuta != null) {
                    doc.setRuta_documento(nuevaRuta);
                }
            }
            
            // Asociar documento a la solicitud
            doc.setObjSolicitud(solicitudGuardada);
            documentosGateway.actualizarDocumento(doc);
        }

        // Crear estado inicial
        EstadoSolicitud estadoInicial = new EstadoSolicitud();
        estadoInicial.setEstado_actual("Enviada");
        estadoInicial.setFecha_registro_estado(new Date());
        estadoInicial.setObjSolicitud(solicitudGuardada);

        if (solicitudGuardada.getEstadosSolicitud() == null) {
            solicitudGuardada.setEstadosSolicitud(new ArrayList<>());
        }
        solicitudGuardada.getEstadosSolicitud().add(estadoInicial);
        estadoSolicitudGateway.guarEstadoSolicitud(estadoInicial);

        // Asociar solicitud al usuario
        usuario.getSolicitudes().add(solicitudGuardada);
        usuarioGateway.actualizarUsuario(usuario);

        // Crear notificación automática para el estudiante
        try {
            this.objGestionarNotificacionCU.notificarCreacionSolicitud(solicitudGuardada, "PAZ_Y_SALVO");
        } catch (Exception e) {
            // Log del error pero no interrumpir el flujo principal
            System.err.println("Error al crear notificación: " + e.getMessage());
            e.printStackTrace();
        }

        return solicitudGuardada;
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudes() {
        return solicitudGateway.listarSolicitudes();
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudesToFuncionario() {
       return solicitudGateway.listarSolicitudesToFuncionario();
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudesToCoordinador() {
        return solicitudGateway.listarSolicitudesToCoordinador();
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudesToSecretaria() {
        return solicitudGateway.listarSolicitudesToSecretaria();
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudesAprobadasToSecretaria() {
        return solicitudGateway.listarSolicitudesAprobadasToSecretaria();
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudesAprobadasToFuncionario() {
        return solicitudGateway.listarSolicitudesAprobadasToFuncionario();
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudesAprobadasToCoordinador() {
        return solicitudGateway.listarSolicitudesAprobadasToCoordinador();
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudesPorRol(String rol, Integer idUsuario) {
        List<SolicitudPazYSalvo> todas = solicitudGateway.listarSolicitudes();

        return todas.stream().filter(solicitud -> {
            List<EstadoSolicitud> estados = solicitud.getEstadosSolicitud();
            EstadoSolicitud ultimoEstado = estados != null && !estados.isEmpty()
                ? estados.get(estados.size() - 1)
                : null;

            switch (rol) {
                case "ESTUDIANTE":
                    return solicitud.getObjUsuario().getId_usuario().equals(idUsuario);
                case "FUNCIONARIO":
                    return ultimoEstado != null && "Enviada".equals(ultimoEstado.getEstado_actual());
                case "COORDINADOR":
                    return ultimoEstado != null && "APROBADA_FUNCIONARIO".equals(ultimoEstado.getEstado_actual());
                case "SECRETARIA":
                    return ultimoEstado != null && "APROBADA_COORDINADOR".equals(ultimoEstado.getEstado_actual());
                default:
                    return false;
            }
        }).toList();
    }

    @Override
    public SolicitudPazYSalvo buscarPorId(Integer idSolicitud) {
        return solicitudGateway.buscarPorId(idSolicitud)
                .orElseThrow(() -> new EntidadNoExisteException("Solicitud de Paz y Salvo no encontrada con ID: " + idSolicitud));
    }

    @Override
    public void cambiarEstadoSolicitud(Integer idSolicitud, String nuevoEstado) {
        // Obtener el estado anterior
        SolicitudPazYSalvo solicitud = buscarPorId(idSolicitud);
        String estadoAnterior = solicitud.getEstadosSolicitud() != null && !solicitud.getEstadosSolicitud().isEmpty()
                ? solicitud.getEstadosSolicitud().get(solicitud.getEstadosSolicitud().size() - 1).getEstado_actual()
                : "ENVIADA";
        
        EstadoSolicitud estado = new EstadoSolicitud();
        estado.setEstado_actual(nuevoEstado);
        estado.setFecha_registro_estado(new Date());
        solicitudGateway.cambiarEstadoSolicitud(idSolicitud, estado);
        
        // Crear notificación automática del cambio de estado
        try {
            solicitud = buscarPorId(idSolicitud); // Recargar con el nuevo estado
            this.objGestionarNotificacionCU.notificarCambioEstadoSolicitud(solicitud, estadoAnterior, nuevoEstado, "PAZ_Y_SALVO");
        } catch (Exception e) {
            // Log del error pero no interrumpir el flujo principal
            System.err.println("Error al crear notificación de cambio de estado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
}
