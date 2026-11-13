package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadosSolicitud;
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
        return solicitudReingresoRepository.findAll()
                .stream()
                .map(entity -> mapper.map(entity, SolicitudReingreso.class))
                .toList();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesReingresoToFuncionario() {
       return solicitudReingresoRepository.findByUltimoEstado(EstadosSolicitud.ENVIADA.textoGuardado())
                .stream()
                .map(entity -> mapper.map(entity, SolicitudReingreso.class))
                .toList();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesReingresoToSecretaria() {
        return solicitudReingresoRepository.findByUltimoEstado(EstadosSolicitud.APROBADA_COORDINADOR.textoGuardado())
                .stream()
                .map(entity -> mapper.map(entity, SolicitudReingreso.class))
                .toList();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesReingresoToCoordinador() {
        return solicitudReingresoRepository.findByUltimoEstado(EstadosSolicitud.APROBADA_FUNCIONARIO.textoGuardado())
                .stream()
                .map(entity -> mapper.map(entity, SolicitudReingreso.class))
                .toList();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesReingresoToCoordinadorPorPrograma(Integer idPrograma) {
        if (idPrograma == null) {
            return listarSolicitudesReingresoToCoordinador();
        }
        return solicitudReingresoRepository.findByUltimoEstadoAndPrograma(EstadosSolicitud.APROBADA_FUNCIONARIO.textoGuardado(), idPrograma)
                .stream()
                .map(entity -> mapper.map(entity, SolicitudReingreso.class))
                .toList();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesAprobadasToSecretaria() {
        // Buscar en historial completo, no solo último estado
        return solicitudReingresoRepository.findByEstadoEnHistorialAndPeriodoAcademico(EstadosSolicitud.APROBADA.textoGuardado(), null)
                .stream()
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
    public List<SolicitudReingreso> listarSolicitudesReingresoToFuncionarioPorPeriodo(String periodoAcademico) {
        return solicitudReingresoRepository.findByUltimoEstadoAndPeriodoAcademico(EstadosSolicitud.ENVIADA.textoGuardado(), periodoAcademico).stream()
                .map(entity -> mapper.map(entity, SolicitudReingreso.class))
                .toList();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesReingresoToCoordinadorPorProgramaYPeriodo(Integer idPrograma, String periodoAcademico) {
        if (idPrograma == null) {
            return listarSolicitudesReingresoToCoordinador();
        }
        return solicitudReingresoRepository.findByUltimoEstadoAndProgramaAndPeriodoAcademico(EstadosSolicitud.APROBADA_FUNCIONARIO.textoGuardado(), idPrograma, periodoAcademico).stream()
                .map(entity -> mapper.map(entity, SolicitudReingreso.class))
                .toList();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesReingresoToSecretariaPorPeriodo(String periodoAcademico) {
        return solicitudReingresoRepository.findByUltimoEstadoAndPeriodoAcademico(EstadosSolicitud.APROBADA_COORDINADOR.textoGuardado(), periodoAcademico).stream()
                .map(entity -> mapper.map(entity, SolicitudReingreso.class))
                .toList();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesAprobadasToSecretariaPorPeriodo(String periodoAcademico) {
        // Buscar en historial completo, no solo último estado
        return solicitudReingresoRepository.findByEstadoEnHistorialAndPeriodoAcademico(EstadosSolicitud.APROBADA.textoGuardado(), periodoAcademico).stream()
                .map(entity -> mapper.map(entity, SolicitudReingreso.class))
                .toList();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesReingresoPorUsuarioYPeriodo(Integer idUsuario, String periodoAcademico) {
        // Todas las solicitudes del estudiante, sin importar el estado; el filtro lo hace la base de datos
        return solicitudReingresoRepository.findByUsuarioYPeriodo(idUsuario, periodoAcademico).stream()
                .map(entity -> mapper.map(entity, SolicitudReingreso.class))
                .toList();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesAprobadasToFuncionario() {
        // Solicitudes que el funcionario ya procesó (aprobó) - buscar en historial completo
        return solicitudReingresoRepository.findByEstadoEnHistorialAndPeriodoAcademico(EstadosSolicitud.APROBADA_FUNCIONARIO.textoGuardado(), null).stream()
                .map(entity -> mapper.map(entity, SolicitudReingreso.class))
                .toList();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesAprobadasToFuncionarioPorPeriodo(String periodoAcademico) {
        // Buscar en historial completo, no solo último estado
        return solicitudReingresoRepository.findByEstadoEnHistorialAndPeriodoAcademico(EstadosSolicitud.APROBADA_FUNCIONARIO.textoGuardado(), periodoAcademico).stream()
                .map(entity -> mapper.map(entity, SolicitudReingreso.class))
                .toList();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesAprobadasToCoordinador() {
        // Solicitudes que el coordinador ya procesó (aprobó) - buscar en historial completo
        return solicitudReingresoRepository.findByEstadoEnHistorialAndPeriodoAcademico(EstadosSolicitud.APROBADA_COORDINADOR.textoGuardado(), null).stream()
                .map(entity -> mapper.map(entity, SolicitudReingreso.class))
                .toList();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesAprobadasToCoordinadorPorPrograma(Integer idPrograma) {
        if (idPrograma == null) {
            return listarSolicitudesAprobadasToCoordinador();
        }
        // Buscar en historial completo, no solo último estado
        return solicitudReingresoRepository.findByEstadoEnHistorialAndProgramaAndPeriodoAcademico(EstadosSolicitud.APROBADA_COORDINADOR.textoGuardado(), idPrograma, null).stream()
                .map(entity -> mapper.map(entity, SolicitudReingreso.class))
                .toList();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesAprobadasToCoordinadorPorProgramaYPeriodo(Integer idPrograma, String periodoAcademico) {
        if (idPrograma == null) {
            return listarSolicitudesAprobadasToCoordinador();
        }
        // Buscar en historial completo, no solo último estado
        return solicitudReingresoRepository.findByEstadoEnHistorialAndProgramaAndPeriodoAcademico(EstadosSolicitud.APROBADA_COORDINADOR.textoGuardado(), idPrograma, periodoAcademico).stream()
                .map(entity -> mapper.map(entity, SolicitudReingreso.class))
                .toList();
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
