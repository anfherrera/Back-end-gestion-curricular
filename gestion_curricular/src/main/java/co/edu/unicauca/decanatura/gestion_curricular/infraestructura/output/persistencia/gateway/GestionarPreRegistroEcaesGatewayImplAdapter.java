package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarPreRegistroEcaesGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.FechaEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.controladorExcepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoSolicitudEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.FechaEcaesEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEcaesEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.FechaEcaesRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudEcaesRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class GestionarPreRegistroEcaesGatewayImplAdapter implements GestionarPreRegistroEcaesGatewayIntPort{

    private final UsuarioRepositoryInt usuarioRepository;

    private final SolicitudEcaesRepositoryInt solicitudEcaesRepository;
    private final FechaEcaesRepositoryInt fechaEcaesRepository;
    private final ModelMapper mapper;
    @Autowired
    public GestionarPreRegistroEcaesGatewayImplAdapter(SolicitudEcaesRepositoryInt solicitudEcaesRepository, UsuarioRepositoryInt usuarioRepositoryInt,
    FechaEcaesRepositoryInt fechaEcaesRepository) {
        this.solicitudEcaesRepository = solicitudEcaesRepository;
        this.mapper = new ModelMapper();
        this.usuarioRepository = usuarioRepositoryInt;
        this.fechaEcaesRepository = fechaEcaesRepository;
    }
    @Override
    public SolicitudEcaes guardar(SolicitudEcaes solicitud) {
        // Convertir dominio → entidad
        SolicitudEcaesEntity entity = mapper.map(solicitud, SolicitudEcaesEntity.class);
        // Guardar en base de datos
        SolicitudEcaesEntity saved = solicitudEcaesRepository.save(entity);
        // Convertir entidad → dominio
        return mapper.map(saved, SolicitudEcaes.class);

    }

    @Override
    public List<SolicitudEcaes> listar() {
        return solicitudEcaesRepository.listarSolicitudesConUsuarios().stream()
            .map(entity -> mapper.map(entity, SolicitudEcaes.class))
            .collect(Collectors.toList());
        
    }

    @Override
    public Optional<SolicitudEcaes> buscarPorId(Integer id) {
        return solicitudEcaesRepository.findById(id)
            .map(entity -> mapper.map(entity, SolicitudEcaes.class));
    }
    @Override
    public Optional<SolicitudEcaes> buscarOpcionalPorId(Integer id) {
        return solicitudEcaesRepository.findById(id)
            .map(entity -> Optional.of(mapper.map(entity, SolicitudEcaes.class)))
            .orElse(Optional.empty());
    }
    
    @Override
    public void cambiarEstadoSolicitudEcaes(Integer idSolicitud, EstadoSolicitud nuevoEstado) {
        SolicitudEcaesEntity solicitudEntity = solicitudEcaesRepository.findById(idSolicitud)
            .orElseThrow(() -> new EntidadNoExisteException("Solicitud ECAES no encontrada con ID: " + idSolicitud));

        EstadoSolicitudEntity nuevo= mapper.map(nuevoEstado, EstadoSolicitudEntity.class);
        nuevo.setObjSolicitud(solicitudEntity); 

        solicitudEntity.getEstadosSolicitud().add(nuevo);
        solicitudEcaesRepository.save(solicitudEntity);

    }
    @Override
    public FechaEcaes publicarFechasEcaes(FechaEcaes fechasEcaes) {
        FechaEcaesEntity entity = mapper.map(fechasEcaes, FechaEcaesEntity.class);
        FechaEcaesEntity saved = fechaEcaesRepository.save(entity);
        return mapper.map(saved, FechaEcaes.class);
    }
    
    @Override
    public List<FechaEcaes> listarFechasEcaes() {
        return fechaEcaesRepository.findAll().stream()
            .map(entity -> mapper.map(entity, FechaEcaes.class))
            .collect(Collectors.toList());
    }
    
    

}
