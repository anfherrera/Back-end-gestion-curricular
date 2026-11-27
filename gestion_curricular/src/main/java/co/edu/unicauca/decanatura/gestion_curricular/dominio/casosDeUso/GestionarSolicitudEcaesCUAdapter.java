package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarArchivosCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarNotificacionCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudEcaesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarPreRegistroEcaesGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadYaExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.ReglaNegocioExcepcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FechaEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FiltroSolicitudes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FlujoEstadosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.AsociadorDocumentosPendientes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.ListadorSolicitudes;


public class GestionarSolicitudEcaesCUAdapter implements GestionarSolicitudEcaesCUIntPort {

    private final GestionarPreRegistroEcaesGatewayIntPort gestionarSolicitudEcaesGateway;
    private final GestionarEstadoSolicitudGatewayIntPort gestionarEstadoSolicitudGateway;
    private final GestionarUsuarioGatewayIntPort gestionarUsuarioGateway;
    private final GestionarNotificacionCUIntPort gestionarNotificacionCU;
    private final AsociadorDocumentosPendientes asociadorDocumentos;
    private final ListadorSolicitudes<SolicitudEcaes> listador;


    public GestionarSolicitudEcaesCUAdapter(GestionarPreRegistroEcaesGatewayIntPort gestionarSolicitudEcaesGateway,
            GestionarDocumentosGatewayIntPort documentosGateway,
            GestionarEstadoSolicitudGatewayIntPort gestionarEstadoSolicitudGateway,
            GestionarUsuarioGatewayIntPort gestionarUsuarioGateway,
            GestionarNotificacionCUIntPort gestionarNotificacionCU,
            GestionarArchivosCUIntPort gestionarArchivos
            ) {
        this.gestionarSolicitudEcaesGateway = gestionarSolicitudEcaesGateway;
        this.gestionarEstadoSolicitudGateway = gestionarEstadoSolicitudGateway;
        this.gestionarUsuarioGateway = gestionarUsuarioGateway;
        this.gestionarNotificacionCU = gestionarNotificacionCU;
        this.asociadorDocumentos = new AsociadorDocumentosPendientes(documentosGateway, gestionarArchivos);
        this.listador = new ListadorSolicitudes<>(FlujoEstadosSolicitud.ECAES, gestionarSolicitudEcaesGateway);

    }

   
        @Override
        public SolicitudEcaes guardar(SolicitudEcaes solicitud) {
            if(solicitud == null) {
                throw new ReglaNegocioExcepcion("La solicitud no puede ser nula");
            }

            if(solicitud.getIdSolicitud()!=null){
                Optional<SolicitudEcaes> solicitudExistente = gestionarSolicitudEcaesGateway.buscarOpcionalPorId(solicitud.getIdSolicitud());
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


            SolicitudEcaes solicitudGuardada = this.gestionarSolicitudEcaesGateway.guardar(solicitud);

            this.gestionarEstadoSolicitudGateway.guarEstadoSolicitud(solicitudGuardada.registrarEstadoInicial());
            // No se guarda el usuario: la relación usuario-solicitudes es mappedBy (la solicitud ya
            // lleva su usuario) y, por orphanRemoval, guardar una lista incompleta podría borrar solicitudes.

            // Crear notificación automática para el estudiante
            try {
                this.gestionarNotificacionCU.notificarCreacionSolicitud(solicitudGuardada, "ECAES");
            } catch (Exception e) {
            }

            // Asociar y mover documentos DESPUÉS de estado/usuario/notificación: si algo falla antes,
            // Mover a la carpeta de la solicitud y asociar los documentos que este estudiante subió antes de enviarla
            asociadorDocumentos.asociar(solicitudGuardada, idUsuario, "ecaes");

            return solicitudGuardada;
    
        }

    @Override
    public List<SolicitudEcaes> listarSolicitudes() {
        return gestionarSolicitudEcaesGateway.listar();
    }
    @Override
    public SolicitudEcaes buscarPorId(Integer idSolicitud) {
        return gestionarSolicitudEcaesGateway.buscarPorId(idSolicitud)
            .orElseThrow(() -> new EntidadNoExisteException("Solicitud no encontrada con ID: " + idSolicitud));    
    }
    @Override
    public void cambiarEstadoSolicitud(Integer idSolicitud, String nuevoEstado) {
        // Obtener el estado anterior
        SolicitudEcaes solicitud = buscarPorId(idSolicitud);

        // Solo se permiten los pasos del flujo (ver FlujoEstadosSolicitud.ECAES)
        FlujoEstadosSolicitud.Paso paso;
        try {
            paso = solicitud.pasoA(FlujoEstadosSolicitud.ECAES, nuevoEstado, null);
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioExcepcion(e.getMessage());
        }

        gestionarSolicitudEcaesGateway.cambiarEstadoSolicitudEcaes(idSolicitud, paso.registro());

        // Crear notificación automática del cambio de estado
        try {
            solicitud = buscarPorId(idSolicitud); // Recargar con el nuevo estado
            this.gestionarNotificacionCU.notificarCambioEstadoSolicitud(solicitud, paso.estadoAnterior(), nuevoEstado, "ECAES");
        } catch (Exception e) {
        }
    }

    @Override
    public FechaEcaes publicarFechasEcaes(FechaEcaes fechasEcaes) {
        return gestionarSolicitudEcaesGateway.publicarFechasEcaes(fechasEcaes);
    }

    @Override
    public List<FechaEcaes> listarFechasEcaes() {
        return gestionarSolicitudEcaesGateway.listarFechasEcaes();
    }

    /** En el flujo ECAES solo decide el funcionario: el coordinador y la secretaría reciben una lista vacía. */
    @Override
    public List<SolicitudEcaes> listar(FiltroSolicitudes filtro) {
        return listador.listar(filtro);
    }

    @Override
    public List<SolicitudEcaes> listarSolicitudesPorRol(String rol, Integer idUsuario) {
        return listador.listarPorRol(rol, idUsuario);
    }

    @Override
    public Optional<FechaEcaes> buscarFechasPorPeriodo(String periodoAcademico) {
        return gestionarSolicitudEcaesGateway.buscarFechasPorPeriodo(periodoAcademico);
    }

    @Override
    public FechaEcaes actualizarFechasEcaes(FechaEcaes fechasEcaes) {
        return gestionarSolicitudEcaesGateway.actualizarFechasEcaes(fechasEcaes);
    }

}
