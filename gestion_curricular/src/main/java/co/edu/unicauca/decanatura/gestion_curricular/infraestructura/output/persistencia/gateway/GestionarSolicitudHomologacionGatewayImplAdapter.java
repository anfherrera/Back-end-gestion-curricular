package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudHomologacionGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.controladorExcepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoSolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudHomologacionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers.Mapper;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudHomologacionRepositoryInt;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class GestionarSolicitudHomologacionGatewayImplAdapter implements GestionarSolicitudHomologacionGatewayIntPort {

    private final Mapper mapper;
    private final SolicitudHomologacionRepositoryInt solicitudHomologacionRepository;

    public GestionarSolicitudHomologacionGatewayImplAdapter(Mapper mapper, SolicitudHomologacionRepositoryInt solicitudHomologacionRepository) {
        this.mapper = mapper;
        this.solicitudHomologacionRepository = solicitudHomologacionRepository;
    }

    @Override
    public SolicitudHomologacion guardar(SolicitudHomologacion solicitud) {
       SolicitudHomologacionEntity entity = mapper.map(solicitud, SolicitudHomologacionEntity.class);
       SolicitudHomologacionEntity savedEntity = solicitudHomologacionRepository.save(entity);
       return mapper.map(savedEntity, SolicitudHomologacion.class);
    
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudes() {
       return  solicitudHomologacionRepository.findAll().stream()
               .map(entity -> mapper.map(entity, SolicitudHomologacion.class))
               .toList();
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesToFuncionario() {
        return  solicitudHomologacionRepository.findByUltimoEstado("Enviada").stream()
               .map(entity -> mapper.map(entity, SolicitudHomologacion.class))
               .toList();
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesToCoordinador() {
       return  solicitudHomologacionRepository.findByUltimoEstado("APROBADA_FUNCIONARIO").stream()
               .map(entity -> mapper.map(entity, SolicitudHomologacion.class))
               .toList();
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesToSecretaria() {
       return  solicitudHomologacionRepository.findByUltimoEstado("APROBADA_COORDINADOR").stream()
               .map(entity -> mapper.map(entity, SolicitudHomologacion.class))
               .toList();
    }

    @Override
    public Optional<SolicitudHomologacion> buscarPorId(Integer idSolicitud) {
       return solicitudHomologacionRepository.findById(idSolicitud)
               .map(entity -> Optional.of(mapper.map(entity, SolicitudHomologacion.class)))
               .orElse(Optional.empty());
    }

    @Override
    public void cambiarEstadoSolicitud(Integer idSolicitud, EstadoSolicitud nuevoEstado) {
       SolicitudHomologacionEntity solicitudEntity = solicitudHomologacionRepository.findById(idSolicitud)
            .orElseThrow(() -> new EntidadNoExisteException("Solicitud de Homologacion no encontrada con ID: " + idSolicitud));

        EstadoSolicitudEntity nuevo= mapper.map(nuevoEstado, EstadoSolicitudEntity.class);
        nuevo.setObjSolicitud(solicitudEntity); 

        solicitudEntity.getEstadosSolicitud().add(nuevo);
        solicitudHomologacionRepository.save(solicitudEntity);

    }


}
