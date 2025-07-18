package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoCursoOfertadoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoCursoOfertadoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.EstadoCursoOfertadoRepositoryInt;

@Service
@Transactional
public class GestionarEstadoCursoOfertadoGatewayImplAdapter implements GestionarEstadoCursoOfertadoGatewayIntPort {

    private final EstadoCursoOfertadoRepositoryInt estadoCursoOfertadoRepository;
    private final ModelMapper estadoMapper;

    public GestionarEstadoCursoOfertadoGatewayImplAdapter(EstadoCursoOfertadoRepositoryInt estadoCursoOfertadoRepository, ModelMapper estadoMapper) {
        this.estadoCursoOfertadoRepository = estadoCursoOfertadoRepository;
        this.estadoMapper = estadoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public EstadoCursoOfertado buscarPorIEstadoCursoOfertado(Integer idEstadoCursoOfertado) {
        EstadoCursoOfertado estadoCurso = null;
        Optional<EstadoCursoOfertadoEntity> estadoCursoEntity = estadoCursoOfertadoRepository.findById(idEstadoCursoOfertado);
        if(estadoCursoEntity.isPresent()){
            estadoCurso = estadoMapper.map(estadoCursoEntity.get(), EstadoCursoOfertado.class);
        }
        return estadoCurso;
    }
    
}
