package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import lombok.extern.slf4j.Slf4j;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudPazYSalvoCUIntPort.RechazoCambioEstado.Motivo;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudPazYSalvoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudPazYSalvoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.ReglaNegocioExcepcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.DatosDocumento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.DocumentosPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FiltroSolicitudes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FlujoEstadosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.TransicionEstadoPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.AsociadorDocumentosPendientes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.ListadorSolicitudes;

@Slf4j
public class GestionarSolicitudPazYSalvoCUAdapter implements GestionarSolicitudPazYSalvoCUIntPort {

    private final GestionarSolicitudPazYSalvoGatewayIntPort solicitudGateway;
    private final GestionarUsuarioGatewayIntPort usuarioGateway;
    private final GestionarEstadoSolicitudGatewayIntPort estadoSolicitudGateway;
    private final AsociadorDocumentosPendientes asociadorDocumentos;
    private final ListadorSolicitudes<SolicitudPazYSalvo> listador;
    private final GestionarNotificacionCUIntPort gestionarNotificacionCU;

    public GestionarSolicitudPazYSalvoCUAdapter(
            GestionarSolicitudPazYSalvoGatewayIntPort solicitudGateway,
            GestionarUsuarioGatewayIntPort usuarioGateway,
            GestionarDocumentosGatewayIntPort documentosGateway,
            GestionarEstadoSolicitudGatewayIntPort estadoSolicitudGateway,
            GestionarArchivosCUIntPort gestionarArchivos,
            GestionarNotificacionCUIntPort gestionarNotificacionCU) {
        this.solicitudGateway = solicitudGateway;
        this.usuarioGateway = usuarioGateway;
        this.estadoSolicitudGateway = estadoSolicitudGateway;
        this.asociadorDocumentos = new AsociadorDocumentosPendientes(documentosGateway, gestionarArchivos);
        this.listador = new ListadorSolicitudes<>(FlujoEstadosSolicitud.TRES_APROBACIONES, solicitudGateway);
        this.gestionarNotificacionCU = gestionarNotificacionCU;
    }

    @Override
    public SolicitudPazYSalvo guardar(SolicitudPazYSalvo solicitud) {
        if (solicitud == null) {
            throw new ReglaNegocioExcepcion("La solicitud no puede ser nula");
        }

        if (solicitud.getObjUsuario() == null || solicitud.getObjUsuario().getIdUsuario() == null) {
            throw new ReglaNegocioExcepcion("El usuario no puede ser nulo o sin ID");
        }

        Integer idUsuario = solicitud.getObjUsuario().getIdUsuario();
        Usuario usuario = usuarioGateway.obtenerUsuarioPorId(idUsuario);
        if (usuario == null) {
            throw new EntidadNoExisteException("Usuario no encontrado con ID: " + idUsuario);
        }
        // Si la solicitud solo trae el id del usuario (sin nombre), se completa con el de la base de datos;
        // el gateway arma el nombre de la solicitud con él
        String nombre = solicitud.getObjUsuario().getNombreCompleto();
        if (nombre == null || nombre.trim().isEmpty()) {
            solicitud.setObjUsuario(usuario);
        }

        // Un estudiante solo puede tener una solicitud de Paz y Salvo en trámite a la vez
        boolean tieneSolicitudActiva = listarSolicitudesPorRol("ESTUDIANTE", usuario.getIdUsuario()).stream()
                .anyMatch(s -> !TransicionEstadoPazYSalvo.esFinal(ultimoEstado(s)));
        if (tieneSolicitudActiva) {
            throw new ReglaNegocioExcepcion(
                    "Ya tiene una solicitud de Paz y Salvo en trámite. Debe esperar a que sea aprobada o rechazada.");
        }

        // Sin período académico: el actual. Uno con texto se guarda tal cual llegó.
        if (solicitud.getPeriodoAcademico() == null || solicitud.getPeriodoAcademico().trim().isEmpty()) {
            String periodoActual = PeriodoAcademicoEnum.oActual(null);
            if (periodoActual != null) {
                solicitud.setPeriodoAcademico(periodoActual);
            }
        }

        // Guardar solicitud
        SolicitudPazYSalvo solicitudGuardada = solicitudGateway.guardar(solicitud);

        // Mover a la carpeta de la solicitud y asociar los documentos que este estudiante subió antes de enviarla
        asociadorDocumentos.asociar(solicitudGuardada, usuario.getIdUsuario(), "pazysalvo");

        // Estado inicial "Enviada": el gateway ya lo crea al guardar; solo se agrega si falta,
        // para no dejar dos estados iniciales.
        if (!solicitudGuardada.tieneEstadoRegistrado()) {
            estadoSolicitudGateway.guarEstadoSolicitud(solicitudGuardada.registrarEstadoInicial());
        }

        // No se guarda el usuario: la relación usuario-solicitudes es mappedBy (la solicitud ya
        // lleva su usuario) y, por orphanRemoval, guardar una lista incompleta podría borrar solicitudes.

        // Crear notificación automática para el estudiante
        try {
            this.gestionarNotificacionCU.notificarCreacionSolicitud(solicitudGuardada, "PAZ_Y_SALVO");
        } catch (Exception e) {
        }

        return solicitudGuardada;
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudes() {
        return solicitudGateway.listarSolicitudes();
    }

    @Override
    public List<SolicitudPazYSalvo> listar(FiltroSolicitudes filtro) {
        return listador.listar(filtro);
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudesPorRol(String rol, Integer idUsuario) {
        return listador.listarPorRol(rol, idUsuario);
    }

    @Override
    public SolicitudPazYSalvo buscarPorId(Integer idSolicitud) {
        return solicitudGateway.buscarPorId(idSolicitud)
                .orElseThrow(() -> new EntidadNoExisteException("Solicitud de Paz y Salvo no encontrada con ID: " + idSolicitud));
    }

    @Override
    public void cambiarEstadoSolicitud(Integer idSolicitud, String nuevoEstado) {
        cambiarEstadoSolicitud(idSolicitud, nuevoEstado, null);
    }

    @Override
    public void cambiarEstadoSolicitud(Integer idSolicitud, String nuevoEstado, String comentario) {
        cambiarEstadoSolicitud(idSolicitud, nuevoEstado, comentario, rol -> true)
                .ifPresent(rechazo -> { throw new ReglaNegocioExcepcion(rechazo.mensaje()); });
    }

    @Override
    public Optional<RechazoCambioEstado> cambiarEstadoSolicitud(Integer idSolicitud, String nuevoEstado,
            String comentario, Predicate<String> puedeActuarComo) {
        SolicitudPazYSalvo solicitud = buscarPorId(idSolicitud);
        String estadoAnterior = ultimoEstado(solicitud);
        FlujoEstadosSolicitud.Paso paso;
        try {
            paso = solicitud.pasoA(FlujoEstadosSolicitud.TRES_APROBACIONES, nuevoEstado, comentario);
        } catch (IllegalArgumentException e) {
            return Optional.of(new RechazoCambioEstado(Motivo.TRANSICION_NO_PERMITIDA, e.getMessage(),
                    TransicionEstadoPazYSalvo.normalizar(estadoAnterior)));
        }

        if (!puedeActuarComo.test(paso.rolResponsable())) {
            return Optional.of(new RechazoCambioEstado(Motivo.ROL_NO_AUTORIZADO,
                    "Este paso le corresponde al rol " + paso.rolResponsable(),
                    TransicionEstadoPazYSalvo.normalizar(estadoAnterior)));
        }

        solicitudGateway.cambiarEstadoSolicitud(idSolicitud, paso.registro());
        
        // Crear notificación automática del cambio de estado
        try {
            solicitud = buscarPorId(idSolicitud); // Recargar con el nuevo estado
            this.gestionarNotificacionCU.notificarCambioEstadoSolicitud(solicitud, estadoAnterior, nuevoEstado, "PAZ_Y_SALVO");
        } catch (RuntimeException e) {
            log.warn("No se pudo notificar el cambio de estado de Paz y Salvo para la solicitud {}", idSolicitud, e);
        }
        return Optional.empty();
    }

    @Override
    public List<DocumentoConTipo> documentosConTipo(Integer idSolicitud) {
        List<Documento> documentos = buscarPorId(idSolicitud).getDocumentos();
        if (documentos == null) {
            return List.of();
        }
        return documentos.stream()
                .filter(d -> d.getNombre() != null)
                .map(d -> new DocumentoConTipo(d, DocumentosPazYSalvo.tipoDeDocumento(d.getNombre())))
                .toList();
    }

    @Override
    public List<Documento> oficios(Integer idSolicitud) {
        return DocumentosPazYSalvo.oficiosPdf(buscarPorId(idSolicitud).getDocumentos());
    }

    @Override
    public ComentariosSolicitud comentarios(Integer idSolicitud) {
        SolicitudPazYSalvo solicitud = buscarPorId(idSolicitud);
        List<ComentarioDocumento> documentos = solicitud.getDocumentos() == null ? List.of()
                : solicitud.getDocumentos().stream()
                        .filter(d -> d.getComentario() != null && !d.getComentario().trim().isEmpty())
                        .map(d -> new ComentarioDocumento(d.getIdDocumento(), d.getNombre(), d.getComentario().trim()))
                        .toList();
        return new ComentariosSolicitud(solicitud.ultimoComentarioDeRechazo().orElse(null), documentos);
    }

    @Override
    public Optional<DatosDocumento> prepararDocumento(Integer id, String cedula, String titulo, String director) {
        SolicitudPazYSalvo solicitud = buscarPorId(id);
        if (solicitud.getObjUsuario() == null || solicitud.getObjUsuario().getIdUsuario() == null) {
            return Optional.empty();
        }
        Usuario usuario = usuarioGateway.obtenerUsuarioPorId(solicitud.getObjUsuario().getIdUsuario());
        if (usuario == null) {
            return Optional.empty();
        }
        Date fecha = solicitud.getFechaRegistroSolicitud();
        return Optional.of(new DatosDocumento(solicitud.getObjUsuario().getNombreCompleto(),
                usuario.getNombreCompleto(), usuario.getCodigo(),
                usuario.getObjPrograma() == null ? "Ingeniería Electrónica y Telecomunicaciones"
                        : usuario.getObjPrograma().getNombrePrograma(),
                fecha, primeroConTexto(cedula, usuario.getCedula(), "No especificada", false),
                primeroConTexto(titulo, solicitud.getTituloTrabajoGrado(), "Trabajo de grado", true),
                primeroConTexto(director, solicitud.getDirectorTrabajoGrado(), "Director asignado", true)));
    }

    private static String primeroConTexto(String enviado, String guardado, String porDefecto, boolean recortar) {
        for (String valor : new String[] { enviado, guardado }) {
            if (valor != null && !valor.trim().isEmpty()) {
                return recortar ? valor.trim() : valor;
            }
        }
        return porDefecto;
    }

    /** Último estado con texto de la solicitud (ignora el registro inicial vacío). Null si no hay. */
    public static String ultimoEstado(SolicitudPazYSalvo solicitud) {
        return FlujoEstadosSolicitud
                .ultimoEstado(solicitud.getEstadosSolicitud());
    }

    
}
