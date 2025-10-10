package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudEcaesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocumentosGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarPreRegistroEcaesGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Documento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FechaEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.controladorExcepciones.excepcionesPropias.EntidadNoExisteException;


public class GestionarSolicitudEcaesCUAdapter implements GestionarSolicitudEcaesCUIntPort {

    private final GestionarPreRegistroEcaesGatewayIntPort objGestionarSolicitudEcaesGateway;
    private final FormateadorResultadosIntPort objFormateadorResultados;
    private final GestionarDocumentosGatewayIntPort objDocumentosGateway;
    private final GestionarEstadoSolicitudGatewayIntPort objGestionarEstadoSolicitudGateway;
    private final GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway;


    public GestionarSolicitudEcaesCUAdapter(GestionarPreRegistroEcaesGatewayIntPort objGestionarSolicitudEcaesGateway,
            FormateadorResultadosIntPort objFormateadorResultados,
            GestionarDocumentosGatewayIntPort objDocumentosGateway,
            GestionarEstadoSolicitudGatewayIntPort objGestionarEstadoSolicitudGateway,
            GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway
            ) {
        this.objGestionarSolicitudEcaesGateway = objGestionarSolicitudEcaesGateway;
        this.objFormateadorResultados = objFormateadorResultados;
        this.objDocumentosGateway = objDocumentosGateway;   
        this.objGestionarEstadoSolicitudGateway = objGestionarEstadoSolicitudGateway;
        this.objGestionarUsuarioGateway = objGestionarUsuarioGateway;
    
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
            
            List<Documento> documentosSinSolicitud = this.objDocumentosGateway.buscarDocumentosSinSolicitud();
            
            for (Documento doc : documentosSinSolicitud) {
                
                doc.setObjSolicitud(solicitudGuardada);
                this.objDocumentosGateway.actualizarDocumento(doc);
                
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
    //metodo poco eficiente, de ser posible se elimina
    @Override
    public void cambiarEstadoSolicitudEcaes(Integer idSolicitud, EstadosSolicitud nuevoEstado) {
        
        EstadoSolicitud nuevo = new EstadoSolicitud();
        nuevo.setEstado_actual(nuevoEstado.name());
        nuevo.setFecha_registro_estado(new Date());
        
        objGestionarSolicitudEcaesGateway.cambiarEstadoSolicitudEcaes(idSolicitud, nuevo);
    }
    //Metodo que solo pide Id y el nuevo estado como String
    @Override
    public void cambiarEstadoSolicitud(Integer idSolicitud, String nuevoEstado) {
        EstadoSolicitud nuevo = new EstadoSolicitud();
        nuevo.setEstado_actual(nuevoEstado);
        nuevo.setFecha_registro_estado(new Date());

        objGestionarSolicitudEcaesGateway.cambiarEstadoSolicitudEcaes(idSolicitud, nuevo);
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

}
