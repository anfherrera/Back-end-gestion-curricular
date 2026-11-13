package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarMateriasIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.MateriaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.MateriaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.CursoOfertadoVeranoRepositoryInt;


@Service
@Transactional
public class GestionarMateriasGatewayImplAdapter implements GestionarMateriasIntPort {

    private final MateriaRepositoryInt materiaRepository;
    private final CursoOfertadoVeranoRepositoryInt cursoOfertadoRepository;
    private final ModelMapper materiasMapper;

    public GestionarMateriasGatewayImplAdapter(MateriaRepositoryInt materiaRepository, 
                                                CursoOfertadoVeranoRepositoryInt cursoOfertadoRepository,
                                                ModelMapper materiasMapper) {
        this.materiaRepository = materiaRepository;
        this.cursoOfertadoRepository = cursoOfertadoRepository;
        this.materiasMapper = materiasMapper;
    }
    

    @Override
    @Transactional(readOnly = true)
    public Materia obtenerMateriaPorId(Integer idMateria) {
        Optional<MateriaEntity> materiaEntity = materiaRepository.findById(idMateria);
        if (materiaEntity.isPresent()) {
            return materiasMapper.map(materiaEntity.get(), Materia.class);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Materia> listarMaterias() {
        List<Materia> materiasRetornar = null;
        List<MateriaEntity> materiasEntity = materiaRepository.findAll();
        if (materiasEntity != null) {
            materiasRetornar = materiasEntity.stream().map(materiaEntity ->{
                return this.materiasMapper.map(materiaEntity, Materia.class);
            }).toList();
        }
        return materiasRetornar;
    }
    
}
