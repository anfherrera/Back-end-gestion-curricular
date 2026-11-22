package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarProgramaGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers.CatalogosMapperPersistencia;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.ProgramaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;

@Service
@Transactional
public class GestionarProgramaGatewayImplAdapter implements GestionarProgramaGatewayIntPort {

    private final ProgramaRepositoryInt programaRepository;
    private final UsuarioRepositoryInt usuarioRepository;

    public GestionarProgramaGatewayImplAdapter(ProgramaRepositoryInt programaRepository, 
                                                UsuarioRepositoryInt usuarioRepository) {
        this.programaRepository = programaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Método original (compatibilidad)
    @Override
    @Transactional(readOnly = true)
    public Programa buscarPorIdPrograma(Integer idPrograma) {
        return programaRepository.findById(idPrograma)
                .map(programaEntity -> CatalogosMapperPersistencia.aDominio(programaEntity))
                .orElse(null);
    }
    
    // Nuevos métodos CRUD
    @Override
    @Transactional
    public Programa guardarPrograma(Programa programa) {
        ProgramaEntity programaEntity = CatalogosMapperPersistencia.aEntidad(programa);
        ProgramaEntity savedEntity = programaRepository.save(programaEntity);
        return CatalogosMapperPersistencia.aDominio(savedEntity);
    }
    
    @Override
    @Transactional
    public Programa actualizarPrograma(Programa programa) {
        programaRepository.findById(programa.getIdPrograma())
            .orElseThrow(() -> new RuntimeException("Programa no encontrado con ID: " + programa.getIdPrograma()));

        ProgramaEntity programaEntity = CatalogosMapperPersistencia.aEntidad(programa);
        ProgramaEntity savedEntity = programaRepository.save(programaEntity);
        return CatalogosMapperPersistencia.aDominio(savedEntity);
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
            return CatalogosMapperPersistencia.aDominio(programaEntity.get());
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Programa obtenerProgramaPorCodigo(String codigo) {
        Programa programaRetornar = null;
        Optional<ProgramaEntity> programaEntity = programaRepository.buscarPorCodigo(codigo);
        if (programaEntity.isPresent()) {
            programaRetornar = CatalogosMapperPersistencia.aDominio(programaEntity.get());
        }
        return programaRetornar;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Programa> buscarPorNombreParcial(String nombreParcial) {
        List<Programa> programasRetornar = null;
        List<ProgramaEntity> programasEntity = programaRepository.buscarPorNombreParcial(nombreParcial);
        if (programasEntity != null) {
            programasRetornar = programasEntity.stream().map(programaEntity -> {
                return CatalogosMapperPersistencia.aDominio(programaEntity);
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
                return CatalogosMapperPersistencia.aDominio(programaEntity);
            }).toList();
        }
        return programasRetornar;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean tieneUsuariosAsociados(Integer idPrograma) {
        return !usuarioRepository.buscarPorPrograma(idPrograma).isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeProgramaPorCodigo(String codigo) {
        return programaRepository.buscarPorCodigo(codigo).isPresent();
    }
}
