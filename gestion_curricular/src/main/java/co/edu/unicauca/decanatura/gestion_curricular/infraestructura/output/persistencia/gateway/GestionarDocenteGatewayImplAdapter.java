package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocenteGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.DocenteEntity;
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

    // Método original (compatibilidad)
    @Override
    @Transactional(readOnly = true)
    public Docente buscarDocentePorId(Integer idDocente) {
        return docenteRepository.findById(idDocente)
                .map(entity -> docenteMapper.map(entity, Docente.class))
                .orElse(null);
    }
    
    // Nuevos métodos CRUD
    @Override
    @Transactional
    public Docente guardarDocente(Docente docente) {
        DocenteEntity docenteEntity = docenteMapper.map(docente, DocenteEntity.class);
        DocenteEntity savedEntity = docenteRepository.save(docenteEntity);
        return docenteMapper.map(savedEntity, Docente.class);
    }
    
    @Override
    @Transactional
    public Docente actualizarDocente(Docente docente) {
        docenteRepository.findById(docente.getId_docente())
            .orElseThrow(() -> new RuntimeException("Docente no encontrado con ID: " + docente.getId_docente()));

        DocenteEntity docenteEntity = docenteMapper.map(docente, DocenteEntity.class);
        DocenteEntity savedEntity = docenteRepository.save(docenteEntity);
        return docenteMapper.map(savedEntity, Docente.class);
    }

    @Override
    @Transactional
    public boolean eliminarDocente(Integer idDocente) {
        boolean existe = false;
        Optional<DocenteEntity> docenteEntity = docenteRepository.findById(idDocente);
        if (docenteEntity.isPresent()) {
            docenteRepository.eliminarPorId(idDocente);
            existe = true;
        }
        return existe;
    }

    @Override
    @Transactional(readOnly = true)
    public Docente obtenerDocentePorId(Integer idDocente) {
        Optional<DocenteEntity> docenteEntity = docenteRepository.findById(idDocente);
        if (docenteEntity.isPresent()) {
            return docenteMapper.map(docenteEntity.get(), Docente.class);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Docente obtenerDocentePorCodigo(String codigo) {
        Docente docenteRetornar = null;
        Optional<DocenteEntity> docenteEntity = docenteRepository.buscarPorCodigo(codigo);
        if (docenteEntity.isPresent()) {
            docenteRetornar = docenteMapper.map(docenteEntity.get(), Docente.class);
        }
        return docenteRetornar;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeDocentePorCodigo(String codigo) {
        boolean existe = false;
        Optional<DocenteEntity> docenteEntity = docenteRepository.buscarPorCodigo(codigo);
        if (docenteEntity.isPresent()) {
            existe = true;
        }
        return existe;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Docente> buscarPorNombreParcial(String nombreParcial) {
        List<Docente> docentesRetornar = null;
        List<DocenteEntity> docentesEntity = docenteRepository.buscarPorNombreParcial(nombreParcial);
        if (docentesEntity != null) {
            docentesRetornar = docentesEntity.stream().map(docenteEntity -> {
                return this.docenteMapper.map(docenteEntity, Docente.class);
            }).toList();
        }
        return docentesRetornar;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Docente> listarDocentes() {
        List<Docente> docentesRetornar = null;
        List<DocenteEntity> docentesEntity = docenteRepository.findAll();
        if (docentesEntity != null) {
            docentesRetornar = docentesEntity.stream().map(docenteEntity -> {
                return this.docenteMapper.map(docenteEntity, Docente.class);
            }).toList();
        }
        return docentesRetornar;
    }
}
