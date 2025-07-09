package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarCursoOfertadoVeranoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarPreRegistroEcaesGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.controladorExcepciones.excepcionesPropias.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEcaesEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers.Mapper;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudEcaesRepositoryInt;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class GestionarPreRegistroEcaesGatewayImplAdapter implements GestionarPreRegistroEcaesGatewayIntPort{

    private final SolicitudEcaesRepositoryInt solicitudEcaesRepository;
    private final ModelMapper mapper;
    @Autowired
    public GestionarPreRegistroEcaesGatewayImplAdapter(SolicitudEcaesRepositoryInt solicitudEcaesRepository) {
        this.solicitudEcaesRepository = solicitudEcaesRepository;
        this.mapper = new ModelMapper();
    }
    @Override
    public SolicitudEcaes guardar(SolicitudEcaesEntity solicitud) {
        //return solicitudEcaesRepository.save(solicitud);
        // Convertir dominio → entidad
        SolicitudEcaesEntity entity = mapper.map(solicitud, SolicitudEcaesEntity.class);
        // Guardar en base de datos
        SolicitudEcaesEntity saved = solicitudEcaesRepository.save(entity);
        // Convertir entidad → dominio
        return mapper.map(saved, SolicitudEcaes.class);

    }

    @Override
    public List<SolicitudEcaes> listar() {
        //return solicitudEcaesRepository.findAll();
        return solicitudEcaesRepository.findAll().stream()
            .map(entity -> mapper.map(entity, SolicitudEcaes.class))
            .collect(Collectors.toList());
        
    }

    @Override
    public SolicitudEcaes buscarPorId(Integer id) {
        SolicitudEcaesEntity entity = solicitudEcaesRepository.findById(id)
            .orElseThrow(() -> new EntidadNoExisteException("No se encontró la solicitud con ID: " + id));
        
        return mapper.map(entity, SolicitudEcaes.class);
    }
    

}
