package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoSolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.EstadoSolicitudRepositoryInt;

@Service
@Transactional
public class GestionarEstadoSolicitudGatewayImplAdapter implements GestionarEstadoSolicitudGatewayIntPort {

    private final EstadoSolicitudRepositoryInt estadoSolicitudRepository;
    private final ModelMapper estadoSolicitudMapper;

    public GestionarEstadoSolicitudGatewayImplAdapter(EstadoSolicitudRepositoryInt estadoSolicitudRepository, ModelMapper estadoSolicitudMapper) {
        this.estadoSolicitudRepository = estadoSolicitudRepository;
        this.estadoSolicitudMapper = estadoSolicitudMapper;
    }
    @Override
    @Transactional
    public EstadoSolicitud actualizarEstadoSolicitud(EstadoSolicitud estadoSolicitud) {
        estadoSolicitudRepository.findById(estadoSolicitud.getId_estado())
            .orElseThrow(() -> new RuntimeException("Materia no encontrada con ID: " + estadoSolicitud.getId_estado()));
        EstadoSolicitudEntity estadoSolicitudEntity = estadoSolicitudMapper.map(estadoSolicitud, EstadoSolicitudEntity.class);
        EstadoSolicitudEntity savedEntity = estadoSolicitudRepository.save(estadoSolicitudEntity);
        return estadoSolicitudMapper.map(savedEntity, EstadoSolicitud.class);
    }

    @Override
    @Transactional(readOnly = true)
    public EstadoSolicitud obtenerEstadoSolicitudPorId(Integer id_estado_solicitud) {
        EstadoSolicitud estadoSolicitud = null;
        Optional<EstadoSolicitudEntity> estadoSolicitudEntity = estadoSolicitudRepository.findById(id_estado_solicitud);
        if(estadoSolicitudEntity !=null){
            estadoSolicitud = estadoSolicitudMapper.map(estadoSolicitudEntity, EstadoSolicitud.class);
        }
        return estadoSolicitud;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstadoSolicitud> obtenerEstadoSolicitudPorFecha(Date fecha_registro_solicitud) {
        List<EstadoSolicitud> estadoSolicitudRetornar = null;
        List<EstadoSolicitudEntity> estadoSolicitudEntities = estadoSolicitudRepository.buscarPorFechaRegistro(fecha_registro_solicitud);
        if(estadoSolicitudEntities != null){
            estadoSolicitudRetornar = estadoSolicitudEntities.stream()
                    .map(estadoSolicitudEntity -> estadoSolicitudMapper.map(estadoSolicitudEntity, EstadoSolicitud.class))
                    .toList();
        }
        return estadoSolicitudRetornar;
    }
    @Override
    @Transactional(readOnly = true)
    public EstadoSolicitud obtenerEstadoSolicitudPorSolicitudId(Integer id_estado_solicitud) {
        EstadoSolicitud estadoSolicitud = null;
        Optional<EstadoSolicitudEntity> estadoSolicitudEntity = estadoSolicitudRepository.buscarPorSolicitud(id_estado_solicitud);
        if(estadoSolicitudEntity !=null){
            estadoSolicitud = estadoSolicitudMapper.map(estadoSolicitudEntity, EstadoSolicitud.class);
        }
        return estadoSolicitud;
    }
    @Override
    public EstadoSolicitud guarEstadoSolicitud(EstadoSolicitud estadoSolicitud) {
        // Convertir dominio → entidad
        EstadoSolicitudEntity entity = estadoSolicitudMapper.map(estadoSolicitud, EstadoSolicitudEntity.class);
        // Guardar en base de datos
        EstadoSolicitudEntity saved = estadoSolicitudRepository.save(entity);
        // Convertir entidad → dominio
        return estadoSolicitudMapper.map(saved, EstadoSolicitud.class);
    }
    
}
