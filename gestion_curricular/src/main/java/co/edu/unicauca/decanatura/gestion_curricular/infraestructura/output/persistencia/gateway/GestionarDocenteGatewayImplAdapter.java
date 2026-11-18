package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocenteGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.DocenteEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers.CatalogosMapperPersistencia;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.CursoOfertadoVeranoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.DocenteRepositoryInt;

@Service
@Transactional
public class GestionarDocenteGatewayImplAdapter implements GestionarDocenteGatewayIntPort {

    private final DocenteRepositoryInt docenteRepository;
    private final CursoOfertadoVeranoRepositoryInt cursoOfertadoRepository;

    public GestionarDocenteGatewayImplAdapter(DocenteRepositoryInt docenteRepository,
                                               CursoOfertadoVeranoRepositoryInt cursoOfertadoRepository) {
        this.docenteRepository = docenteRepository;
        this.cursoOfertadoRepository = cursoOfertadoRepository;
    }

    // Método original (compatibilidad)
    @Override
    @Transactional(readOnly = true)
    public Docente buscarDocentePorId(Integer idDocente) {
        return docenteRepository.findById(idDocente)
                .map(entity -> CatalogosMapperPersistencia.aDominio(entity))
                .orElse(null);
    }
    
    // Nuevos métodos CRUD
    @Override
    @Transactional
    public Docente guardarDocente(Docente docente) {
        DocenteEntity docenteEntity = CatalogosMapperPersistencia.aEntidad(docente);
        DocenteEntity savedEntity = docenteRepository.save(docenteEntity);
        return CatalogosMapperPersistencia.aDominio(savedEntity);
    }
    
    @Override
    @Transactional
    public Docente actualizarDocente(Docente docente) {
        docenteRepository.findById(docente.getId_docente())
            .orElseThrow(() -> new RuntimeException("Docente no encontrado con ID: " + docente.getId_docente()));

        DocenteEntity docenteEntity = CatalogosMapperPersistencia.aEntidad(docente);
        DocenteEntity savedEntity = docenteRepository.save(docenteEntity);
        return CatalogosMapperPersistencia.aDominio(savedEntity);
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
            return CatalogosMapperPersistencia.aDominio(docenteEntity.get());
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Docente obtenerDocentePorCodigo(String codigo) {
        Docente docenteRetornar = null;
        Optional<DocenteEntity> docenteEntity = docenteRepository.buscarPorCodigo(codigo);
        if (docenteEntity.isPresent()) {
            docenteRetornar = CatalogosMapperPersistencia.aDominio(docenteEntity.get());
        }
        return docenteRetornar;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Docente> buscarPorNombreParcial(String nombreParcial) {
        List<Docente> docentesRetornar = null;
        List<DocenteEntity> docentesEntity = docenteRepository.buscarPorNombreParcial(nombreParcial);
        if (docentesEntity != null) {
            docentesRetornar = docentesEntity.stream().map(docenteEntity -> {
                return CatalogosMapperPersistencia.aDominio(docenteEntity);
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
                return CatalogosMapperPersistencia.aDominio(docenteEntity);
            }).toList();
        }
        return docentesRetornar;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean tieneCursosAsignados(Integer idDocente) {
        return !cursoOfertadoRepository.buscarPorDocente(idDocente).isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeDocentePorCodigo(String codigo) {
        return docenteRepository.buscarPorCodigo(codigo).isPresent();
    }
}
