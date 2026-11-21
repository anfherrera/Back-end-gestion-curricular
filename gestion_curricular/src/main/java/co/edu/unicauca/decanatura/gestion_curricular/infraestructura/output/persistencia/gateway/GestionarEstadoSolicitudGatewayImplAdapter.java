package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoSolicitudGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoSolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers.SolicitudMapperPersistencia;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.EstadoSolicitudRepositoryInt;

@Service
@Transactional
public class GestionarEstadoSolicitudGatewayImplAdapter implements GestionarEstadoSolicitudGatewayIntPort {

    private final EstadoSolicitudRepositoryInt estadoSolicitudRepository;

    public GestionarEstadoSolicitudGatewayImplAdapter(EstadoSolicitudRepositoryInt estadoSolicitudRepository) {
        this.estadoSolicitudRepository = estadoSolicitudRepository;
    }
    @Override
    public EstadoSolicitud guarEstadoSolicitud(EstadoSolicitud estadoSolicitud) {
        // Convertir dominio → entidad
        EstadoSolicitudEntity entity = SolicitudMapperPersistencia.aEntidad(estadoSolicitud);
        // Guardar en base de datos
        EstadoSolicitudEntity saved = estadoSolicitudRepository.save(entity);
        // Convertir entidad → dominio
        return SolicitudMapperPersistencia.aDominio(saved);
    }
    
}
