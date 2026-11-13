package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;


import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoCursoOfertadoGatewayIntPort;
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

}
