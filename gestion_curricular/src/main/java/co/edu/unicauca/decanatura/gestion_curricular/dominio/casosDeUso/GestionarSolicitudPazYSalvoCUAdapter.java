package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudPazYSalvoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudPazYSalvoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

public class GestionarSolicitudPazYSalvoCUAdapter implements GestionarSolicitudPazYSalvoCUIntPort {

    private final GestionarSolicitudPazYSalvoGatewayIntPort solicitudGateway;
    private final GestionarUsuarioGatewayIntPort usuarioGateway;
    private final GestionarDocumentosGatewayIntPort documentosGateway;
    private final GestionarEstadoSolicitudGatewayIntPort estadoSolicitudGateway;
    private final FormateadorResultadosIntPort formateadorResultados;

    public GestionarSolicitudPazYSalvoCUAdapter(
            GestionarSolicitudPazYSalvoGatewayIntPort solicitudGateway,
            GestionarUsuarioGatewayIntPort usuarioGateway,
            GestionarDocumentosGatewayIntPort documentosGateway,
            GestionarEstadoSolicitudGatewayIntPort estadoSolicitudGateway,
            FormateadorResultadosIntPort formateadorResultados) {
        this.solicitudGateway = solicitudGateway;
        this.usuarioGateway = usuarioGateway;
        this.documentosGateway = documentosGateway;
        this.estadoSolicitudGateway = estadoSolicitudGateway;
        this.formateadorResultados = formateadorResultados;
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

        // Asociar documentos sin solicitud
        List<Documento> documentosSinSolicitud = documentosGateway.buscarDocumentosSinSolicitud();
        for (Documento doc : documentosSinSolicitud) {
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
    public SolicitudPazYSalvo buscarPorId(Integer idSolicitud) {
        return solicitudGateway.buscarPorId(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud de Paz y Salvo no encontrada"));
    }

    @Override
    public void cambiarEstadoSolicitud(Integer idSolicitud, String nuevoEstado) {
        EstadoSolicitud estado = new EstadoSolicitud();
        estado.setEstado_actual(nuevoEstado);
        estado.setFecha_registro_estado(new Date());
        solicitudGateway.cambiarEstadoSolicitud(idSolicitud, estado);
    }

    
}
