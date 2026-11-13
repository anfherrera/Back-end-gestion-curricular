package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;


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
    public EstadoSolicitud guarEstadoSolicitud(EstadoSolicitud estadoSolicitud) {
        // Convertir dominio → entidad
        EstadoSolicitudEntity entity = estadoSolicitudMapper.map(estadoSolicitud, EstadoSolicitudEntity.class);
        // Guardar en base de datos
        EstadoSolicitudEntity saved = estadoSolicitudRepository.save(entity);
        // Convertir entidad → dominio
        return estadoSolicitudMapper.map(saved, EstadoSolicitud.class);
    }
    
}
