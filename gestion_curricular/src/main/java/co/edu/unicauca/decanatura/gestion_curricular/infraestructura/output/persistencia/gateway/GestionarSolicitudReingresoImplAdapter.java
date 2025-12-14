package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudReingresoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.controladorExcepciones.excepcionesPropias.EntidadNoExisteException;
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
       return solicitudReingresoRepository.findByUltimoEstado("Enviada")
                .stream()
                .map(entity -> mapper.map(entity, SolicitudReingreso.class))
                .toList();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesReingresoToSecretaria() {
        return solicitudReingresoRepository.findByUltimoEstado("APROBADA_COORDINADOR")
                .stream()
                .map(entity -> mapper.map(entity, SolicitudReingreso.class))
                .toList();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesReingresoToCoordinador() {
        return solicitudReingresoRepository.findByUltimoEstado("APROBADA_FUNCIONARIO")
                .stream()
                .map(entity -> mapper.map(entity, SolicitudReingreso.class))
                .toList();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesReingresoToCoordinadorPorPrograma(Integer idPrograma) {
        if (idPrograma == null) {
            return listarSolicitudesReingresoToCoordinador();
        }
        return solicitudReingresoRepository.findByUltimoEstadoAndPrograma("APROBADA_FUNCIONARIO", idPrograma)
                .stream()
                .map(entity -> mapper.map(entity, SolicitudReingreso.class))
                .toList();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesAprobadasToSecretaria() {
        return solicitudReingresoRepository.findByUltimoEstado("APROBADA")
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
        return solicitudReingresoRepository.findByUltimoEstadoAndPeriodoAcademico("Enviada", periodoAcademico).stream()
                .map(entity -> mapper.map(entity, SolicitudReingreso.class))
                .toList();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesReingresoToCoordinadorPorProgramaYPeriodo(Integer idPrograma, String periodoAcademico) {
        if (idPrograma == null) {
            return listarSolicitudesReingresoToCoordinador();
        }
        return solicitudReingresoRepository.findByUltimoEstadoAndProgramaAndPeriodoAcademico("APROBADA_FUNCIONARIO", idPrograma, periodoAcademico).stream()
                .map(entity -> mapper.map(entity, SolicitudReingreso.class))
                .toList();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesReingresoToSecretariaPorPeriodo(String periodoAcademico) {
        return solicitudReingresoRepository.findByUltimoEstadoAndPeriodoAcademico("APROBADA_COORDINADOR", periodoAcademico).stream()
                .map(entity -> mapper.map(entity, SolicitudReingreso.class))
                .toList();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesAprobadasToSecretariaPorPeriodo(String periodoAcademico) {
        return solicitudReingresoRepository.findByUltimoEstadoAndPeriodoAcademico("APROBADA", periodoAcademico).stream()
                .map(entity -> mapper.map(entity, SolicitudReingreso.class))
                .toList();
    }

    @Override
    public List<SolicitudReingreso> listarSolicitudesReingresoPorUsuarioYPeriodo(Integer idUsuario, String periodoAcademico) {
        // Para estudiantes, buscamos todas las solicitudes del usuario (sin filtro de estado)
        // pero filtramos por período académico
        return solicitudReingresoRepository.findAll().stream()
                .filter(entity -> entity.getObjUsuario() != null && 
                                 entity.getObjUsuario().getId_usuario().equals(idUsuario) &&
                                 (periodoAcademico == null || periodoAcademico.equals(entity.getPeriodo_academico())))
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
