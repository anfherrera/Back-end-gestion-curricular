package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocenteGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.DocenteRepositoryInt;

@Service
@Transactional
public class GestionarDocenteGatewayImplAdapter implements GestionarDocenteGatewayIntPort {

    private final DocenteRepositoryInt docenteRepository;
    private final ModelMapper docenteMapper;

    public GestionarDocenteGatewayImplAdapter(DocenteRepositoryInt docenteRepository, ModelMapper docenteMapper) {
        this.docenteRepository = docenteRepository;
        this.docenteMapper = docenteMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Docente buscarDocentePorId(Integer idDocente) {
        return docenteRepository.findById(idDocente)
                .map(entity -> docenteMapper.map(entity, Docente.class))
                .orElse(null);
    }
    
}
