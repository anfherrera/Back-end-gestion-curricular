package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.Date;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarSolicitudPazYSalvoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoSolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudPazYSalvoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class GestionarSolicitudPazYSalvoGatewayImplAdapter implements GestionarSolicitudPazYSalvoGatewayIntPort {
    
    private final SolicitudRepositoryInt solicitudRepository;

    private final ModelMapper solicitudMapper;

    public GestionarSolicitudPazYSalvoGatewayImplAdapter(SolicitudRepositoryInt solicitudRepository,
                                                ModelMapper solicitudMapper) {
        this.solicitudRepository = solicitudRepository;
        this.solicitudMapper = solicitudMapper;
    }

    @Override
    @Transactional
    public SolicitudPazYSalvo crearSolicitudPazYSalvo(SolicitudPazYSalvo solicitudPazYSalvo) {
        SolicitudPazYSalvoEntity solicitudPazYSalvoEntity = solicitudMapper.map(solicitudPazYSalvo, SolicitudPazYSalvoEntity.class);
        solicitudPazYSalvoEntity.setNombre_solicitud(SolicitudPazYSalvo.class.getSimpleName());
        solicitudPazYSalvoEntity.setFecha_registro_solicitud(new Date());
        EstadoSolicitudEntity estadoSolicitudEntity = null;
        estadoSolicitudEntity = new EstadoSolicitudEntity();
        estadoSolicitudEntity.setFecha_registro_estado(new Date());
        estadoSolicitudEntity.setObjSolicitud(solicitudPazYSalvoEntity);
        
        List<EstadoSolicitudEntity> estadosSolcitud = solicitudPazYSalvoEntity.getEstadosSolicitud();
        estadosSolcitud.add(estadoSolicitudEntity);
        solicitudPazYSalvoEntity.setEstadosSolicitud(estadosSolcitud);
        SolicitudPazYSalvoEntity solicitudPazYSalvoGuardado = solicitudRepository.save(solicitudPazYSalvoEntity);
        
        return solicitudMapper.map(solicitudPazYSalvoGuardado, SolicitudPazYSalvo.class); // Implementación pendiente

    }

}
