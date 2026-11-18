package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadoCursoOfertadoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.EstadoCursoOfertadoRepositoryInt;

@Service
@Transactional
public class GestionarEstadoCursoOfertadoGatewayImplAdapter implements GestionarEstadoCursoOfertadoGatewayIntPort {

    private final EstadoCursoOfertadoRepositoryInt estadoCursoOfertadoRepository;

    public GestionarEstadoCursoOfertadoGatewayImplAdapter(EstadoCursoOfertadoRepositoryInt estadoCursoOfertadoRepository) {
        this.estadoCursoOfertadoRepository = estadoCursoOfertadoRepository;
    }

}
