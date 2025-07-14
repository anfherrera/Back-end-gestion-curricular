package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarSolicitudEcaesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarPreRegistroEcaesGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadoSolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.controladorExcepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.formateador.FormateadorResultadosImplAdapter;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoSolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEcaesEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudEcaesRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;


public class GestionarSolicitudEcaesCUAdapter implements GestionarSolicitudEcaesCUIntPort {

    private final FormateadorResultadosImplAdapter formateadorResultadosImplAdapter;

    private final SolicitudEcaesRepositoryInt solicitudRepository;
    private final UsuarioRepositoryInt usuarioRepository;
    private final GestionarPreRegistroEcaesGatewayIntPort objGestionarSolicitudEcaesGateway;
    private final FormateadorResultadosIntPort objFormateadorResultados;
    private final ModelMapper mapper;


    public GestionarSolicitudEcaesCUAdapter(SolicitudEcaesRepositoryInt solicitudEcaesRepository
            , UsuarioRepositoryInt usuarioRepository,GestionarPreRegistroEcaesGatewayIntPort objGestionarSolicitudEcaesGateway,FormateadorResultadosIntPort objFormateadorResultados
            ,ModelMapper modelMapper, FormateadorResultadosImplAdapter formateadorResultadosImplAdapter) {
        this.solicitudRepository= solicitudEcaesRepository;
        this.usuarioRepository = usuarioRepository;
        this.objGestionarSolicitudEcaesGateway = objGestionarSolicitudEcaesGateway;
        this.objFormateadorResultados = objFormateadorResultados;
        this.mapper = modelMapper;
        this.formateadorResultadosImplAdapter = formateadorResultadosImplAdapter;

    }

    @Override
    public SolicitudEcaes guardar(SolicitudEcaes solicitud) {

        SolicitudEcaes solicitudExistePorId = objGestionarSolicitudEcaesGateway.buscarPorId(solicitud.getId_solicitud());
        if (solicitudExistePorId != null) {
            this.formateadorResultadosImplAdapter.retornarRespuestaErrorEntidadExiste("Ya existe una solicitud con el ID: " + solicitud.getId_solicitud());
        }
        //============debe ir las reglas de negocio? no lo de abajo========================
        // Buscar usuario
        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.findById(solicitud.getObjUsuario().getId_usuario());
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + solicitud.getObjUsuario().getId_usuario());
        }

        UsuarioEntity usuarioEntity = usuarioOpt.get();

        // Mapear a entidad
        SolicitudEcaesEntity solicitudEntity = mapper.map(solicitud, SolicitudEcaesEntity.class);
        // Asociar usuario 
        solicitudEntity.setObjUsuario(usuarioEntity);
        
        EstadoSolicitudEntity estado = new EstadoSolicitudEntity();
        estado.setEstado_actual("Enviado");
        estado.setFecha_registro_estado(new Date(System.currentTimeMillis()));
        estado.setObjSolicitud(solicitudEntity); // <- esto establece el vínculo
        solicitudEntity.getEstadosSolicitud().add(estado); // <- y este el otro lado


        // Guardar solicitud
        SolicitudEcaesEntity saved = solicitudRepository.save(solicitudEntity);

        // Mapear de vuelta a dominio
        return mapper.map(saved, SolicitudEcaes.class);
    }

    @Override
    public List<SolicitudEcaes> listarSolicitudes() {
        return solicitudRepository.listarSolicitudesConUsuarios() 
                .stream()
                .map(entity -> mapper.map(entity, SolicitudEcaes.class))
                .collect(Collectors.toList());
    }
    @Override
    public SolicitudEcaes buscarPorId(Integer idSolicitud) {
        SolicitudEcaesEntity entity = solicitudRepository.findById(idSolicitud)
            .orElseThrow(() -> new EntidadNoExisteException("Solicitud no encontrada con ID: " + idSolicitud));

        return mapper.map(entity, SolicitudEcaes.class);
    }
    // @Override
    // public Optional<SolicitudEcaes> buscarPorId(Integer idSolicitud) {
    //     return solicitudRepository.findById(idSolicitud)
    //             .map(entity -> mapper.map(entity, SolicitudEcaes.class  ));
    // }
    @Override
    public void cambiarEstadoSolicitudEcaes(Integer idSolicitud, EstadoSolicitudEcaes nuevoEstado) {
        SolicitudEcaesEntity solicitud = solicitudRepository.findById(idSolicitud)
            .orElseThrow(() -> new EntidadNoExisteException("Solicitud no encontrada con ID: " + idSolicitud));

        // Crear nuevo estado
        EstadoSolicitudEntity nuevo = new EstadoSolicitudEntity();
        nuevo.setEstado_actual(nuevoEstado.name());
        nuevo.setFecha_registro_estado(new Date());
        nuevo.setObjSolicitud(solicitud);

        // Agregar al historial de estados
        solicitud.getEstadosSolicitud().add(nuevo);

        // Guardar cambios
        solicitudRepository.save(solicitud);
    }
    //metodo adicional si deseo obtener el ultimo estado de la solicitud
    // public String obtenerEstadoActual() {
    //     if (objEstadosSolicitud == null || objEstadosSolicitud.isEmpty()) {
    //         return "Sin estado";
    //     }
    //     return objEstadosSolicitud.get(objEstadosSolicitud.size() - 1).getEstado_actual();
    // }


}
