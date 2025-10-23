package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarProgramaGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.ProgramaRepositoryInt;

@Service
@Transactional
public class GestionarProgramaGatewayImplAdapter implements GestionarProgramaGatewayIntPort {

    private final ProgramaRepositoryInt programaRepository;
    private final ModelMapper programaMapper;

    public GestionarProgramaGatewayImplAdapter(ProgramaRepositoryInt programaRepository, ModelMapper programaMapper) {
        this.programaRepository = programaRepository;
        this.programaMapper = programaMapper;
    }

    // Método original (compatibilidad)
    @Override
    @Transactional(readOnly = true)
    public Programa buscarPorIdPrograma(Integer idPrograma) {
        return programaRepository.findById(idPrograma)
                .map(programaEntity -> programaMapper.map(programaEntity, Programa.class))
                .orElse(null);
    }
    
    // Nuevos métodos CRUD
    @Override
    @Transactional
    public Programa guardarPrograma(Programa programa) {
        ProgramaEntity programaEntity = programaMapper.map(programa, ProgramaEntity.class);
        ProgramaEntity savedEntity = programaRepository.save(programaEntity);
        return programaMapper.map(savedEntity, Programa.class);
    }
    
    @Override
    @Transactional
    public Programa actualizarPrograma(Programa programa) {
        programaRepository.findById(programa.getId_programa())
            .orElseThrow(() -> new RuntimeException("Programa no encontrado con ID: " + programa.getId_programa()));

        ProgramaEntity programaEntity = programaMapper.map(programa, ProgramaEntity.class);
        ProgramaEntity savedEntity = programaRepository.save(programaEntity);
        return programaMapper.map(savedEntity, Programa.class);
    }

    @Override
    @Transactional
    public boolean eliminarPrograma(Integer idPrograma) {
        boolean existe = false;
        Optional<ProgramaEntity> programaEntity = programaRepository.findById(idPrograma);
        if (programaEntity.isPresent()) {
            programaRepository.eliminarPorId(idPrograma);
            existe = true;
        }
        return existe;
    }

    @Override
    @Transactional(readOnly = true)
    public Programa obtenerProgramaPorId(Integer idPrograma) {
        Optional<ProgramaEntity> programaEntity = programaRepository.findById(idPrograma);
        if (programaEntity.isPresent()) {
            return programaMapper.map(programaEntity.get(), Programa.class);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Programa obtenerProgramaPorCodigo(String codigo) {
        Programa programaRetornar = null;
        Optional<ProgramaEntity> programaEntity = programaRepository.buscarPorCodigo(codigo);
        if (programaEntity.isPresent()) {
            programaRetornar = programaMapper.map(programaEntity.get(), Programa.class);
        }
        return programaRetornar;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeProgramaPorCodigo(String codigo) {
        boolean existe = false;
        Optional<ProgramaEntity> programaEntity = programaRepository.buscarPorCodigo(codigo);
        if (programaEntity.isPresent()) {
            existe = true;
        }
        return existe;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Programa> buscarPorNombreParcial(String nombreParcial) {
        List<Programa> programasRetornar = null;
        List<ProgramaEntity> programasEntity = programaRepository.buscarPorNombreParcial(nombreParcial);
        if (programasEntity != null) {
            programasRetornar = programasEntity.stream().map(programaEntity -> {
                return this.programaMapper.map(programaEntity, Programa.class);
            }).toList();
        }
        return programasRetornar;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Programa> listarProgramas() {
        List<Programa> programasRetornar = null;
        List<ProgramaEntity> programasEntity = programaRepository.findAll();
        if (programasEntity != null) {
            programasRetornar = programasEntity.stream().map(programaEntity -> {
                return this.programaMapper.map(programaEntity, Programa.class);
            }).toList();
        }
        return programasRetornar;
    }
}
