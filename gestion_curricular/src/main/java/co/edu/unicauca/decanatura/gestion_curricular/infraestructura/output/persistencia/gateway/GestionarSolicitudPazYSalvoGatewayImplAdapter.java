package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudPazYSalvoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.controladorExcepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoSolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudPazYSalvoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers.Mapper;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudPazYSalvoRepositoryInt;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class GestionarSolicitudPazYSalvoGatewayImplAdapter implements GestionarSolicitudPazYSalvoGatewayIntPort {

    private final SolicitudPazYSalvoRepositoryInt solicitudRepository;
    private final Mapper mapper;

    public GestionarSolicitudPazYSalvoGatewayImplAdapter(SolicitudPazYSalvoRepositoryInt solicitudRepository, Mapper mapper) {
        this.solicitudRepository = solicitudRepository;
        this.mapper = mapper;
    }

    @Override
    public SolicitudPazYSalvo guardar(SolicitudPazYSalvo solicitudPazYSalvo) {
        SolicitudPazYSalvoEntity entity = mapper.map(solicitudPazYSalvo, SolicitudPazYSalvoEntity.class);
        entity.setNombre_solicitud(SolicitudPazYSalvo.class.getSimpleName());
        entity.setFecha_registro_solicitud(new Date());

        // Crear estado inicial
        EstadoSolicitudEntity estadoSolicitudEntity = new EstadoSolicitudEntity();
        estadoSolicitudEntity.setFecha_registro_estado(new Date());
        estadoSolicitudEntity.setObjSolicitud(entity);

        List<EstadoSolicitudEntity> estadosSolicitud = entity.getEstadosSolicitud();
        estadosSolicitud.add(estadoSolicitudEntity);
        entity.setEstadosSolicitud(estadosSolicitud);

        SolicitudPazYSalvoEntity savedEntity = solicitudRepository.save(entity);
        return mapper.map(savedEntity, SolicitudPazYSalvo.class);
    }

    @Override
    public List<SolicitudPazYSalvo> listarSolicitudes() {
        return solicitudRepository.findAll().stream()
                .map(entity -> mapper.map(entity, SolicitudPazYSalvo.class))
                .toList();
    }

    @Override
    public Optional<SolicitudPazYSalvo> buscarPorId(Integer idSolicitud) {
        return solicitudRepository.findById(idSolicitud)
                .map(entity -> mapper.map(entity, SolicitudPazYSalvo.class));
    }

    @Override
    public void cambiarEstadoSolicitud(Integer idSolicitud, EstadoSolicitud nuevoEstado) {
        SolicitudPazYSalvoEntity solicitudEntity = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new EntidadNoExisteException("Solicitud de Paz y Salvo no encontrada con ID: " + idSolicitud));

        EstadoSolicitudEntity nuevo = mapper.map(nuevoEstado, EstadoSolicitudEntity.class);
        nuevo.setObjSolicitud(solicitudEntity);

        solicitudEntity.getEstadosSolicitud().add(nuevo);
        solicitudRepository.save(solicitudEntity);
    }
}
