package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.ArrayList;
import java.util.Date;
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
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FechaEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FiltroSolicitudes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FlujoEstadosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.EstadosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.AsociadorDocumentosPendientes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.ListadorSolicitudes;


public class GestionarSolicitudEcaesCUAdapter implements GestionarSolicitudEcaesCUIntPort {

    private final GestionarPreRegistroEcaesGatewayIntPort gestionarSolicitudEcaesGateway;
    private final GestionarDocumentosGatewayIntPort documentosGateway;
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
        this.documentosGateway = documentosGateway;
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

            if(solicitud.getId_solicitud()!=null){
                Optional<SolicitudEcaes> solicitudExistente = gestionarSolicitudEcaesGateway.buscarOpcionalPorId(solicitud.getId_solicitud());
                if (solicitudExistente.isPresent()) {
                    throw new EntidadYaExisteException("Ya existe una solicitud con el ID: "
                    + solicitud.getId_solicitud());
                }
            }

            if(solicitud.getObjUsuario() == null || solicitud.getObjUsuario().getId_usuario() == null){
                throw new ReglaNegocioExcepcion("El usuario no puede ser nulo o no tener ID");
            }
            Integer idUsuario = solicitud.getObjUsuario().getId_usuario();
            Optional<Usuario> usuarioOpt = gestionarUsuarioGateway.buscarUsuarioPorId(idUsuario);
            if (usuarioOpt.isEmpty()) {
                throw new EntidadNoExisteException("Usuario ID: " + idUsuario + " no encontrado");
            }


            SolicitudEcaes solicitudGuardada = this.gestionarSolicitudEcaesGateway.guardar(solicitud);

            EstadoSolicitud estadoInicial = new EstadoSolicitud();
            estadoInicial.setEstado_actual(EstadosSolicitud.ENVIADA.textoGuardado());//Se pone por defecto el estado de Enviada
            estadoInicial.setFecha_registro_estado(new Date());
            estadoInicial.setObjSolicitud(solicitudGuardada); // establecer vínculo
            
            if(solicitudGuardada.getEstadosSolicitud() == null) {
                solicitudGuardada.setEstadosSolicitud(new ArrayList<>());
            }
            solicitudGuardada.getEstadosSolicitud().add(estadoInicial);
            this.gestionarEstadoSolicitudGateway.guarEstadoSolicitud(estadoInicial);
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
        String estadoAnterior = FlujoEstadosSolicitud.ultimoEstado(solicitud.getEstadosSolicitud());

        // Solo se permiten los pasos del flujo (ver FlujoEstadosSolicitud.ECAES)
        try {
            FlujoEstadosSolicitud.ECAES.validarYObtenerRolResponsable(estadoAnterior, nuevoEstado);
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioExcepcion(e.getMessage());
        }

        EstadoSolicitud nuevo = new EstadoSolicitud();
        nuevo.setEstado_actual(FlujoEstadosSolicitud.ECAES.normalizar(nuevoEstado));
        nuevo.setFecha_registro_estado(new Date());

        gestionarSolicitudEcaesGateway.cambiarEstadoSolicitudEcaes(idSolicitud, nuevo);
        
        // Crear notificación automática del cambio de estado
        try {
            solicitud = buscarPorId(idSolicitud); // Recargar con el nuevo estado
            this.gestionarNotificacionCU.notificarCambioEstadoSolicitud(solicitud, estadoAnterior, nuevoEstado, "ECAES");
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
