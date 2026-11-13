package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudReingresoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoSolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudReingresoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers.Mapper;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudReingresoRepositoryInt;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class GestionarSolicitudReingresoImplAdapter implements GestionarSolicitudReingresoGatewayIntPort {
    private final Mapper mapper;
    private final SolicitudReingresoRepositoryInt solicitudReingresoRepository;

    public GestionarSolicitudReingresoImplAdapter(Mapper mapper, SolicitudReingresoRepositoryInt solicitudReingresoRepository) {
        this.mapper = mapper;
        this.solicitudReingresoRepository = solicitudReingresoRepository;
    }

    @Override
    public SolicitudReingreso crearSolicitudReingreso(SolicitudReingreso solicitud) {
        SolicitudReingresoEntity entity = mapper.map(solicitud, SolicitudReingresoEntity.class);
        SolicitudReingresoEntity savedEntity = solicitudReingresoRepository.save(entity);
        return mapper.map(savedEntity, SolicitudReingreso.class);
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesReingreso() {
        return aDominio(solicitudReingresoRepository.findAll());
    }

    @Override
    public List<SolicitudReingreso> listarPorUsuario(Integer idUsuario, String periodo) {
        return aDominio(solicitudReingresoRepository.findByUsuarioYPeriodo(idUsuario, periodo));
    }

    @Override
    public List<SolicitudReingreso> listarPorUltimoEstado(String estado, Integer idPrograma, String periodo) {
        return aDominio(idPrograma == null
                ? solicitudReingresoRepository.findByUltimoEstadoAndPeriodoAcademico(estado, periodo)
                : solicitudReingresoRepository.findByUltimoEstadoAndProgramaAndPeriodoAcademico(estado, idPrograma, periodo));
    }

    @Override
    public List<SolicitudReingreso> listarPorEstadoEnHistorial(String estado, Integer idPrograma, String periodo) {
        return aDominio(idPrograma == null
                ? solicitudReingresoRepository.findByEstadoEnHistorialAndPeriodoAcademico(estado, periodo)
                : solicitudReingresoRepository.findByEstadoEnHistorialAndProgramaAndPeriodoAcademico(estado, idPrograma, periodo));
    }

    private List<SolicitudReingreso> aDominio(List<SolicitudReingresoEntity> entidades) {
        return entidades.stream()
                .map(entity -> mapper.map(entity, SolicitudReingreso.class))
                .toList();
    }

    @Override
    public Optional<SolicitudReingreso> buscarPorId(Integer id) {
        return solicitudReingresoRepository.findById(id)
                .map(entity -> Optional.of(mapper.map(entity, SolicitudReingreso.class)))
                .orElse(Optional.empty());
    }

    @Override
    public void cambiarEstadoSolicitudReingreso(Integer idSolicitud, EstadoSolicitud nuevoEstado) {
      SolicitudReingresoEntity solicitudEntity = (SolicitudReingresoEntity) solicitudReingresoRepository.findById(idSolicitud)
              .orElseThrow(() -> new EntidadNoExisteException("Solicitud de Reingreso no encontrada con ID: " + idSolicitud));
      EstadoSolicitudEntity nuevo = mapper.map(nuevoEstado, EstadoSolicitudEntity.class);
      nuevo.setObjSolicitud(solicitudEntity);

      solicitudEntity.getEstadosSolicitud().add(nuevo);
      solicitudReingresoRepository.save(solicitudEntity);
    }
}
