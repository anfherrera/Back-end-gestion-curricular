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


@Service
@Transactional
public class GestionarMateriasGatewayImplAdapter implements GestionarMateriasIntPort {

    private final MateriaRepositoryInt materiaRepository;
    private final ModelMapper materiasMapper;

    public GestionarMateriasGatewayImplAdapter(MateriaRepositoryInt materiaRepository, ModelMapper materiasMapper) {
        this.materiaRepository = materiaRepository;
        this.materiasMapper = materiasMapper;
    }
    
    @Override
    @Transactional
    public Materia guardarMateria(Materia materia) {
        MateriaEntity materiaEntity = materiasMapper.map(materia, MateriaEntity.class);
        MateriaEntity savedEntity = materiaRepository.save(materiaEntity);
        return materiasMapper.map(savedEntity, Materia.class);
    }
    
    @Override
    @Transactional
    public Materia actualizarMateria(Materia materia) {
        // Validar si existe la materia antes de actualizar
        materiaRepository.findById(materia.getId_materia())
            .orElseThrow(() -> new RuntimeException("Materia no encontrada con ID: " + materia.getId_materia()));

        // Mapear el DTO a la entidad
        MateriaEntity materiaEntity = materiasMapper.map(materia, MateriaEntity.class);

        // Al tener un ID existente, save() hará UPDATE en lugar de INSERT
        MateriaEntity savedEntity = materiaRepository.save(materiaEntity);

        // Retornar la entidad guardada como dominio
        return materiasMapper.map(savedEntity, Materia.class);
    }


    @Override
    @Transactional
    public boolean eliminarMateria(Integer idMateria) {
        boolean existe = false;
        Optional<MateriaEntity> materiaEntity = materiaRepository.findById(idMateria);
        if (materiaEntity != null) {
            materiaRepository.deleteById(idMateria);
            existe = true;
        }
        return existe;
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
    public Materia obtenerMateriaPorCodigo(String codigo) {
        Materia materiaRetornar = null;
        Optional<MateriaEntity> materiaEntity = materiaRepository.buscarPorCodigo(codigo);
        if (materiaEntity != null) {
            materiaRetornar = materiasMapper.map(materiaEntity.get(), Materia.class);
        }
        return materiaRetornar;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeMateriaPorCodigo(String codigo) {
        boolean existe = false;
        Optional<MateriaEntity> materiaEntity = materiaRepository.buscarPorCodigo(codigo);
        if (materiaEntity != null) {
            if( materiaEntity.isPresent() ) {
                existe = true;
            }
                // Si existe, se retorna true
        }
        return existe;

    }

    @Override
    @Transactional(readOnly = true)
    public List<Materia> buscarPorNombreParcial(String nombreParcial) {
        List<Materia> materiasRetornar = null;
        List<MateriaEntity> materiasEntity = materiaRepository.buscarPorNombreParcial(nombreParcial);
        if (materiasEntity != null) {
            materiasRetornar = materiasEntity.stream().map(materiaEntity ->{
                return this.materiasMapper.map(materiaEntity, Materia.class);
            }).toList();
        }
        return materiasRetornar;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Materia> buscarPorCreditos(Integer creditos) {
        List<Materia> materiasRetornar = null;
        List<MateriaEntity> materiasEntity = materiaRepository.buscarPorCreditos(creditos);
        if (materiasEntity != null) {
            materiasRetornar = materiasEntity.stream().map(materiaEntity ->{
                return this.materiasMapper.map(materiaEntity, Materia.class);
            }).toList();
        }
        return materiasRetornar;
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
