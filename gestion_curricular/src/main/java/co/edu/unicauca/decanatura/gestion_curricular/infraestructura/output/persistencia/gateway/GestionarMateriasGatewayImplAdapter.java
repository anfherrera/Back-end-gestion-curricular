package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarMateriasIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.MateriaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers.CatalogosMapperPersistencia;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.CursoOfertadoVeranoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.MateriaRepositoryInt;


@Service
@Transactional
public class GestionarMateriasGatewayImplAdapter implements GestionarMateriasIntPort {

    private final MateriaRepositoryInt materiaRepository;
    private final CursoOfertadoVeranoRepositoryInt cursoOfertadoRepository;

    public GestionarMateriasGatewayImplAdapter(MateriaRepositoryInt materiaRepository, 
                                                CursoOfertadoVeranoRepositoryInt cursoOfertadoRepository) {
        this.materiaRepository = materiaRepository;
        this.cursoOfertadoRepository = cursoOfertadoRepository;
    }
    

    @Override
    @Transactional(readOnly = true)
    public Materia obtenerMateriaPorId(Integer idMateria) {
        Optional<MateriaEntity> materiaEntity = materiaRepository.findById(idMateria);
        if (materiaEntity.isPresent()) {
            return CatalogosMapperPersistencia.aDominio(materiaEntity.get());
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
                return CatalogosMapperPersistencia.aDominio(materiaEntity);
            }).toList();
        }
        return materiasRetornar;
    }
    
}
