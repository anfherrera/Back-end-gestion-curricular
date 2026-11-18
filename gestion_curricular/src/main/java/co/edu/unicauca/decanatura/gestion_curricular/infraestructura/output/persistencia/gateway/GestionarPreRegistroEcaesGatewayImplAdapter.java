package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarPreRegistroEcaesGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FechaEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoSolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.FechaEcaesEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEcaesEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers.CatalogosMapperPersistencia;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers.SolicitudMapperPersistencia;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.FechaEcaesRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudEcaesRepositoryInt;

@Service
@Transactional
public class GestionarPreRegistroEcaesGatewayImplAdapter implements GestionarPreRegistroEcaesGatewayIntPort{


    private final SolicitudEcaesRepositoryInt solicitudEcaesRepository;
    private final FechaEcaesRepositoryInt fechaEcaesRepository;
    
    public GestionarPreRegistroEcaesGatewayImplAdapter(SolicitudEcaesRepositoryInt solicitudEcaesRepository,
    FechaEcaesRepositoryInt fechaEcaesRepository) {
        this.solicitudEcaesRepository = solicitudEcaesRepository;
        this.fechaEcaesRepository = fechaEcaesRepository;
    }
    @Override
    public SolicitudEcaes guardar(SolicitudEcaes solicitud) {
        // Convertir dominio → entidad
        SolicitudEcaesEntity entity = SolicitudMapperPersistencia.aEntidadEcaes(solicitud);
        // Guardar en base de datos
        SolicitudEcaesEntity saved = solicitudEcaesRepository.save(entity);
        // Convertir entidad → dominio
        return SolicitudMapperPersistencia.aDominioEcaes(saved);

    }

    @Override
    public List<SolicitudEcaes> listar() {
        return solicitudEcaesRepository.listarSolicitudesConUsuarios().stream()
            .map(entity -> SolicitudMapperPersistencia.aDominioEcaes(entity))
            .collect(Collectors.toList());
        
    }

    @Override
    public List<SolicitudEcaes> listarPorUsuario(Integer idUsuario, String periodo) {
        return aDominio(solicitudEcaesRepository.findByUsuarioYPeriodo(idUsuario, periodo));
    }

    @Override
    public List<SolicitudEcaes> listarPorUltimoEstado(String estado, Integer idPrograma, String periodo) {
        return aDominio(idPrograma == null
                ? solicitudEcaesRepository.findByUltimoEstadoAndPeriodoAcademico(estado, periodo)
                : solicitudEcaesRepository.findByUltimoEstadoAndProgramaAndPeriodoAcademico(estado, idPrograma, periodo));
    }

    private List<SolicitudEcaes> aDominio(List<SolicitudEcaesEntity> entidades) {
        return entidades.stream()
                .map(entity -> SolicitudMapperPersistencia.aDominioEcaes(entity))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SolicitudEcaes> buscarPorId(Integer id) {
        return solicitudEcaesRepository.findById(id)
            .map(entity -> SolicitudMapperPersistencia.aDominioEcaes(entity));
    }
    @Override
    public Optional<SolicitudEcaes> buscarOpcionalPorId(Integer id) {
        return solicitudEcaesRepository.findById(id)
            .map(entity -> Optional.of(SolicitudMapperPersistencia.aDominioEcaes(entity)))
            .orElse(Optional.empty());
    }
    
    @Override
    public void cambiarEstadoSolicitudEcaes(Integer idSolicitud, EstadoSolicitud nuevoEstado) {
        SolicitudEcaesEntity solicitudEntity = solicitudEcaesRepository.findById(idSolicitud)
            .orElseThrow(() -> new EntidadNoExisteException("Solicitud ECAES no encontrada con ID: " + idSolicitud));

        EstadoSolicitudEntity nuevo= SolicitudMapperPersistencia.aEntidadEcaes(nuevoEstado);
        nuevo.setObjSolicitud(solicitudEntity); 

        solicitudEntity.getEstadosSolicitud().add(nuevo);
        solicitudEcaesRepository.save(solicitudEntity);

    }
    @Override
    public FechaEcaes publicarFechasEcaes(FechaEcaes fechasEcaes) {
        FechaEcaesEntity entity = CatalogosMapperPersistencia.aEntidad(fechasEcaes);
        FechaEcaesEntity saved = fechaEcaesRepository.save(entity);
        return CatalogosMapperPersistencia.aDominio(saved);
    }
    
    @Override
    public List<FechaEcaes> listarFechasEcaes() {
        return fechaEcaesRepository.findAll().stream()
            .map(entity -> CatalogosMapperPersistencia.aDominio(entity))
            .collect(Collectors.toList());
    }

    @Override
    public Optional<FechaEcaes> buscarFechasPorPeriodo(String periodoAcademico) {
        return fechaEcaesRepository.findByPeriodoAcademico(periodoAcademico)
            .map(entity -> CatalogosMapperPersistencia.aDominio(entity));
    }

    @Override
    public FechaEcaes actualizarFechasEcaes(FechaEcaes fechasEcaes) {
        // Buscar la entidad existente por período académico
        Optional<FechaEcaesEntity> entidadExistente = fechaEcaesRepository.findByPeriodoAcademico(fechasEcaes.getPeriodoAcademico());
        
        if (entidadExistente.isPresent()) {
            // Actualizar la entidad existente
            FechaEcaesEntity entity = entidadExistente.get();
            entity.setInscripcion_est_by_facultad(fechasEcaes.getInscripcion_est_by_facultad());
            entity.setRegistro_recaudo_ordinario(fechasEcaes.getRegistro_recaudo_ordinario());
            entity.setRegistro_recaudo_extraordinario(fechasEcaes.getRegistro_recaudo_extraordinario());
            entity.setCitacion(fechasEcaes.getCitacion());
            entity.setAplicacion(fechasEcaes.getAplicacion());
            entity.setResultados_individuales(fechasEcaes.getResultados_individuales());
            
            FechaEcaesEntity saved = fechaEcaesRepository.save(entity);
            return CatalogosMapperPersistencia.aDominio(saved);
        } else {
            // Si no existe, crear nueva
            FechaEcaesEntity entity = CatalogosMapperPersistencia.aEntidad(fechasEcaes);
            FechaEcaesEntity saved = fechaEcaesRepository.save(entity);
            return CatalogosMapperPersistencia.aDominio(saved);
        }
    }
    

}
