package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudHomologacionGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
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
       return aDominio(solicitudHomologacionRepository.findAll());
    }

    @Override
    public List<SolicitudHomologacion> listarPorUsuario(Integer idUsuario, String periodo) {
        return aDominio(solicitudHomologacionRepository.findByUsuarioYPeriodo(idUsuario, periodo));
    }

    @Override
    public List<SolicitudHomologacion> listarPorUltimoEstado(String estado, Integer idPrograma, String periodo) {
        return aDominio(idPrograma == null
                ? solicitudHomologacionRepository.findByUltimoEstadoAndPeriodoAcademico(estado, periodo)
                : solicitudHomologacionRepository.findByUltimoEstadoAndProgramaAndPeriodoAcademico(estado, idPrograma, periodo));
    }

    @Override
    public List<SolicitudHomologacion> listarPorEstadoEnHistorial(String estado, Integer idPrograma, String periodo) {
        return aDominio(idPrograma == null
                ? solicitudHomologacionRepository.findByEstadoEnHistorialAndPeriodoAcademico(estado, periodo)
                : solicitudHomologacionRepository.findByEstadoEnHistorialAndProgramaAndPeriodoAcademico(estado, idPrograma, periodo));
    }

    private List<SolicitudHomologacion> aDominio(List<SolicitudHomologacionEntity> entidades) {
        return entidades.stream()
                .map(entity -> mapper.map(entity, SolicitudHomologacion.class))
                .toList();
    }

    @Override
    public Optional<SolicitudHomologacion> buscarPorId(Integer idSolicitud) {
       return solicitudHomologacionRepository.findByIdWithDocumentos(idSolicitud)
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
