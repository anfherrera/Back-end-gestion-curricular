package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadosSolicitud;
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
       return  solicitudHomologacionRepository.findAll().stream()
               .map(entity -> mapper.map(entity, SolicitudHomologacion.class))
               .toList();
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesToFuncionario() {
        return  solicitudHomologacionRepository.findByUltimoEstado(EstadosSolicitud.ENVIADA.textoGuardado()).stream()
               .map(entity -> mapper.map(entity, SolicitudHomologacion.class))
               .toList();
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesToCoordinador() {
       return  solicitudHomologacionRepository.findByUltimoEstado(EstadosSolicitud.APROBADA_FUNCIONARIO.textoGuardado()).stream()
               .map(entity -> mapper.map(entity, SolicitudHomologacion.class))
               .toList();
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesToCoordinadorPorPrograma(Integer idPrograma) {
        if (idPrograma == null) {
            return listarSolicitudesToCoordinador();
        }
        return solicitudHomologacionRepository.findByUltimoEstadoAndPrograma(EstadosSolicitud.APROBADA_FUNCIONARIO.textoGuardado(), idPrograma)
                .stream()
                .map(entity -> mapper.map(entity, SolicitudHomologacion.class))
                .toList();
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesToSecretaria() {
       return  solicitudHomologacionRepository.findByUltimoEstado(EstadosSolicitud.APROBADA_COORDINADOR.textoGuardado()).stream()
               .map(entity -> mapper.map(entity, SolicitudHomologacion.class))
               .toList();
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesAprobadasToSecretaria() {
        // Buscar en historial completo, no solo último estado
        return solicitudHomologacionRepository.findByEstadoEnHistorialAndPeriodoAcademico(EstadosSolicitud.APROBADA.textoGuardado(), null).stream()
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
    public List<SolicitudHomologacion> listarSolicitudesToFuncionarioPorPeriodo(String periodoAcademico) {
        return solicitudHomologacionRepository.findByUltimoEstadoAndPeriodoAcademico(EstadosSolicitud.ENVIADA.textoGuardado(), periodoAcademico).stream()
                .map(entity -> mapper.map(entity, SolicitudHomologacion.class))
                .toList();
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesToCoordinadorPorProgramaYPeriodo(Integer idPrograma, String periodoAcademico) {
        if (idPrograma == null) {
            return listarSolicitudesToCoordinador();
        }
        return solicitudHomologacionRepository.findByUltimoEstadoAndProgramaAndPeriodoAcademico(EstadosSolicitud.APROBADA_FUNCIONARIO.textoGuardado(), idPrograma, periodoAcademico).stream()
                .map(entity -> mapper.map(entity, SolicitudHomologacion.class))
                .toList();
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesToSecretariaPorPeriodo(String periodoAcademico) {
        return solicitudHomologacionRepository.findByUltimoEstadoAndPeriodoAcademico(EstadosSolicitud.APROBADA_COORDINADOR.textoGuardado(), periodoAcademico).stream()
                .map(entity -> mapper.map(entity, SolicitudHomologacion.class))
                .toList();
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesAprobadasToSecretariaPorPeriodo(String periodoAcademico) {
        // Buscar en historial completo, no solo último estado
        return solicitudHomologacionRepository.findByEstadoEnHistorialAndPeriodoAcademico(EstadosSolicitud.APROBADA.textoGuardado(), periodoAcademico).stream()
                .map(entity -> mapper.map(entity, SolicitudHomologacion.class))
                .toList();
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesPorUsuarioYPeriodo(Integer idUsuario, String periodoAcademico) {
        // Todas las solicitudes del estudiante, sin importar el estado; el filtro lo hace la base de datos
        return solicitudHomologacionRepository.findByUsuarioYPeriodo(idUsuario, periodoAcademico).stream()
                .map(entity -> mapper.map(entity, SolicitudHomologacion.class))
                .toList();
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesAprobadasToFuncionario() {
        // Solicitudes que el funcionario ya procesó (aprobó) - buscar en historial completo
        return solicitudHomologacionRepository.findByEstadoEnHistorialAndPeriodoAcademico(EstadosSolicitud.APROBADA_FUNCIONARIO.textoGuardado(), null).stream()
                .map(entity -> mapper.map(entity, SolicitudHomologacion.class))
                .toList();
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesAprobadasToFuncionarioPorPeriodo(String periodoAcademico) {
        // Buscar en historial completo, no solo último estado
        return solicitudHomologacionRepository.findByEstadoEnHistorialAndPeriodoAcademico(EstadosSolicitud.APROBADA_FUNCIONARIO.textoGuardado(), periodoAcademico).stream()
                .map(entity -> mapper.map(entity, SolicitudHomologacion.class))
                .toList();
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesAprobadasToCoordinador() {
        // Solicitudes que el coordinador ya procesó (aprobó) - buscar en historial completo
        return solicitudHomologacionRepository.findByEstadoEnHistorialAndPeriodoAcademico(EstadosSolicitud.APROBADA_COORDINADOR.textoGuardado(), null).stream()
                .map(entity -> mapper.map(entity, SolicitudHomologacion.class))
                .toList();
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesAprobadasToCoordinadorPorPrograma(Integer idPrograma) {
        if (idPrograma == null) {
            return listarSolicitudesAprobadasToCoordinador();
        }
        // Buscar en historial completo, no solo último estado
        return solicitudHomologacionRepository.findByEstadoEnHistorialAndProgramaAndPeriodoAcademico(EstadosSolicitud.APROBADA_COORDINADOR.textoGuardado(), idPrograma, null).stream()
                .map(entity -> mapper.map(entity, SolicitudHomologacion.class))
                .toList();
    }

    @Override
    public List<SolicitudHomologacion> listarSolicitudesAprobadasToCoordinadorPorProgramaYPeriodo(Integer idPrograma, String periodoAcademico) {
        if (idPrograma == null) {
            return listarSolicitudesAprobadasToCoordinador();
        }
        // Buscar en historial completo, no solo último estado
        return solicitudHomologacionRepository.findByEstadoEnHistorialAndProgramaAndPeriodoAcademico(EstadosSolicitud.APROBADA_COORDINADOR.textoGuardado(), idPrograma, periodoAcademico).stream()
                .map(entity -> mapper.map(entity, SolicitudHomologacion.class))
                .toList();
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
