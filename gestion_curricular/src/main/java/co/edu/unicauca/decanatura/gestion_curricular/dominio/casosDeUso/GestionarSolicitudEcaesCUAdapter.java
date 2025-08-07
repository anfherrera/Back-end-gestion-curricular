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
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadoSolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.controladorExcepciones.excepcionesPropias.EntidadNoExisteException;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudEcaesRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;


public class GestionarSolicitudEcaesCUAdapter implements GestionarSolicitudEcaesCUIntPort {

    private final SolicitudEcaesRepositoryInt solicitudRepository;
    private final UsuarioRepositoryInt usuarioRepository;
    private final GestionarPreRegistroEcaesGatewayIntPort objGestionarSolicitudEcaesGateway;
    private final FormateadorResultadosIntPort objFormateadorResultados;
    private final GestionarDocumentosGatewayIntPort objDocumentosGateway;
    private final GestionarUsuarioGatewayIntPort objUsuario;
    private final GestionarEstadoSolicitudGatewayIntPort objGestionarEstadoSolicitudGateway;
    


    public GestionarSolicitudEcaesCUAdapter(SolicitudEcaesRepositoryInt solicitudEcaesRepository
            , UsuarioRepositoryInt usuarioRepository,GestionarPreRegistroEcaesGatewayIntPort objGestionarSolicitudEcaesGateway,
            FormateadorResultadosIntPort objFormateadorResultados,
            GestionarDocumentosGatewayIntPort objDocumentosGateway, GestionarUsuarioGatewayIntPort objUsuario,
            GestionarEstadoSolicitudGatewayIntPort objGestionarEstadoSolicitudGateway
            ) {
        this.solicitudRepository= solicitudEcaesRepository;
        this.usuarioRepository = usuarioRepository;
        this.objGestionarSolicitudEcaesGateway = objGestionarSolicitudEcaesGateway;
        this.objFormateadorResultados = objFormateadorResultados;
        this.objDocumentosGateway = objDocumentosGateway;
        this.objUsuario = objUsuario;   
        this.objGestionarEstadoSolicitudGateway = objGestionarEstadoSolicitudGateway;
    
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
            Optional<Usuario> usuarioOpt = objGestionarSolicitudEcaesGateway.buscarUsuarioPorId(idUsuario);
            if (usuarioOpt.isEmpty()) {
                this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("Usuario ID: " + idUsuario + " no encontrado");
            }
            //version original se comento para evitar errores
            // List<Documento> documentos = solicitud.getDocumentos();
            // if(documentos == null || documentos.isEmpty()) {
            //     this.objFormateadorResultados.retornarRespuestaErrorReglaDeNegocio("La solicitud debe tener al menos un documento");
            // }

            SolicitudEcaes solicitudGuardada = this.objGestionarSolicitudEcaesGateway.guardar(solicitud);
            //version original se comento para evitar errores======================
            //Asociar documentos a la solicitud guardada
            // for (Documento doc : solicitudGuardada.getDocumentos()) {
            //     doc.setObjSolicitud(solicitudGuardada);
            //     this.objDocumentosGateway.actualizarDocumento(doc);
            // }
            //==============
            //buscar documentos sin solicitud pero en la solicitud guardada
            
            List<Documento> documentosSinSolicitud = this.objDocumentosGateway.buscarDocumentoSinSolicitud();
            
            for (Documento doc : documentosSinSolicitud) {
                Integer i = 0;
                Integer idsDocumentos = solicitudGuardada.getDocumentos().get(i).getId_documento();
                doc.setObjSolicitud(solicitudGuardada);
                if(solicitud.getDocumentos() != null) {
                    doc.setComentario(solicitud.getDocumentos().get(i).getComentario());
                    System.out.println("Ingreso al if: "+ i);
                    doc.setTipoDocumentoSolicitudPazYSalvo(solicitud.getDocumentos().get(i).getTipoDocumentoSolicitudPazYSalvo()); ;
                }
                
                System.out.println("Documento sin solicitud: " + doc.getNombre());
                System.out.println("iD doc a eliminar: "+ this.objDocumentosGateway.buscarDocumentoId(idsDocumentos).getId_documento());
                System.out.println("Iterador: "+i   );
                this.objDocumentosGateway.actualizarDocumento(doc);
                i++;
                
            }
            //==================


            // for (EstadoSolicitud estado : solicitudGuardada.getEstadosSolicitud()) {
            //     estado.setObjSolicitud(solicitudGuardada);
            //     this.objGestionarEstadoSolicitudGateway.actualizarEstadoSolicitud(estado);
            // }
            //============================================
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
            this.objUsuario.actualizarUsuario(usuarioOpt.get());
        

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
    public void cambiarEstadoSolicitudEcaes(Integer idSolicitud, EstadoSolicitudEcaes nuevoEstado) {
        
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


    


}
