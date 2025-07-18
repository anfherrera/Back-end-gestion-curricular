package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarCursoOfertadoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.GrupoCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.CursoOfertadoVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoCursoOfertadoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.Enums.GrupoCursoVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.CursoOfertadoVeranoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.EstadoCursoOfertadoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;
import jakarta.persistence.EntityNotFoundException;


@Service
@Transactional
public class GestionarCursoOfertadoVeranoGatewayImplAdapter implements GestionarCursoOfertadoVeranoGatewayIntPort {

    private final CursoOfertadoVeranoRepositoryInt cursoRepository;
    private final UsuarioRepositoryInt usuarioRepository;
    private final EstadoCursoOfertadoRepositoryInt objEstadoCursoOfertadoRepository;
    private final ModelMapper cursoMapper;

    public GestionarCursoOfertadoVeranoGatewayImplAdapter(CursoOfertadoVeranoRepositoryInt cursoRepository, UsuarioRepositoryInt usuarioRepository,EstadoCursoOfertadoRepositoryInt objEstadoCursoOfertadoRepository,@Qualifier("mapperCurso") ModelMapper cursoMapper) {
        this.cursoRepository = cursoRepository;
        this.usuarioRepository = usuarioRepository;
        this.objEstadoCursoOfertadoRepository = objEstadoCursoOfertadoRepository;
        this.cursoMapper = cursoMapper;
    }

    @Override
    @Transactional
    public CursoOfertadoVerano crearCurso(CursoOfertadoVerano curso) {
        CursoOfertadoVeranoEntity cursoEntity = cursoMapper.map(curso, CursoOfertadoVeranoEntity.class);
        EstadoCursoOfertadoEntity estadoCurso = new EstadoCursoOfertadoEntity();
        estadoCurso.setFecha_registro_estado(new Date());
        estadoCurso.setObjCursoOfertadoVerano(cursoEntity);
        //cursoEntity.getEstadosCursoOfertados().add(estadoCurso);
        List<EstadoCursoOfertadoEntity> estadosCurso = cursoEntity.getEstadosCursoOfertados();
        estadosCurso.add(estadoCurso);
        cursoEntity.setEstadosCursoOfertados(estadosCurso); 


        CursoOfertadoVeranoEntity saved = cursoRepository.save(cursoEntity);

        return cursoMapper.map(saved, CursoOfertadoVerano.class);
    }

    @Override
    @Transactional
    public CursoOfertadoVerano actualizarCurso(CursoOfertadoVerano curso, EstadoCursoOfertado estadoCurso) {
        cursoRepository.findById(curso.getId_curso())
            .orElseThrow(() -> new RuntimeException("Curso no encontrado con ID: " + curso.getId_curso()));

        CursoOfertadoVeranoEntity cursoEntity = cursoMapper.map(curso, CursoOfertadoVeranoEntity.class);
        EstadoCursoOfertadoEntity estadoCursoEntity = null;
        EstadoCursoOfertadoEntity estadoCursoGuardado = null;

        if(estadoCurso != null) {
            estadoCursoEntity = cursoMapper.map(estadoCurso, EstadoCursoOfertadoEntity.class);

        } else {
            estadoCursoEntity = new EstadoCursoOfertadoEntity();
            estadoCursoEntity.setEstado_actual("Publicado");
            estadoCursoEntity.setFecha_registro_estado(new Date());

        }
            
            estadoCursoEntity.setObjCursoOfertadoVerano(cursoEntity);

            estadoCursoGuardado = this.objEstadoCursoOfertadoRepository.save(estadoCursoEntity);

            cursoEntity.getEstadosCursoOfertados().add(estadoCursoGuardado);

        CursoOfertadoVeranoEntity cursoGuardado = cursoRepository.save(cursoEntity);

        return cursoMapper.map(cursoGuardado, CursoOfertadoVerano.class);
    }

    @Override
    @Transactional
    public boolean eliminarCurso(Integer idCurso) {
        Optional<CursoOfertadoVeranoEntity> entityOpt = cursoRepository.findById(idCurso);
        if (entityOpt.isPresent()) {
            cursoRepository.deleteById(idCurso);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public CursoOfertadoVerano obtenerCursoPorId(Integer idCurso) {
        return cursoRepository.findById(idCurso)
            .map(entity -> cursoMapper.map(entity, CursoOfertadoVerano.class))
            .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CursoOfertadoVerano> buscarPorGrupo(GrupoCursoVerano grupo) {
        GrupoCursoVeranoEntity grupoEntity = cursoMapper.map(grupo, GrupoCursoVeranoEntity.class);
        return cursoRepository.buscarPorGrupo(grupoEntity).stream()
            .map(entity -> cursoMapper.map(entity, CursoOfertadoVerano.class))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CursoOfertadoVerano> buscarPorMateria(Integer idMateria) {
        return cursoRepository.buscarPorMateria(idMateria).stream()
            .map(entity -> cursoMapper.map(entity, CursoOfertadoVerano.class))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CursoOfertadoVerano> buscarPorDocente(Integer idDocente) {
        return cursoRepository.buscarPorDocente(idDocente).stream()
            .map(entity -> cursoMapper.map(entity, CursoOfertadoVerano.class))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Integer contarPorSalon(String salon) {
        return cursoRepository.contarPorSalon(salon);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CursoOfertadoVerano> listarTodos() {
        return cursoRepository.findAll().stream()
            .map(entity -> cursoMapper.map(entity, CursoOfertadoVerano.class))
            .toList();
    }

    @Override
    @Transactional
    public CursoOfertadoVerano asociarUsuarioCurso(Integer idUsuario, Integer idCurso) {
        Set<UsuarioEntity> usuariosEntitySet = null;
        CursoOfertadoVeranoEntity cursoOfertadoVeranoEntityGuardado = null;
        Optional<UsuarioEntity> usuarioEntityOptional = usuarioRepository.findById(idUsuario);
        Optional<CursoOfertadoVeranoEntity> cursoEntityOptional = cursoRepository.findById(idCurso);
            if(usuarioEntityOptional.isPresent() && cursoEntityOptional.isPresent()) {
                UsuarioEntity usuarioEntity = usuarioEntityOptional.get();
                CursoOfertadoVeranoEntity cursoEntity = cursoEntityOptional.get();

                usuariosEntitySet = cursoEntity.getEstudiantesInscritos();
                usuariosEntitySet.add(usuarioEntity);
                cursoEntity.setEstudiantesInscritos(usuariosEntitySet);
                
                cursoOfertadoVeranoEntityGuardado = cursoRepository.save(cursoEntity);
            }
        
        

        return cursoMapper.map(cursoOfertadoVeranoEntityGuardado, CursoOfertadoVerano.class);
    }

    @Override
    @Transactional
    public CursoOfertadoVerano desasociarUsuarioCurso(Integer idUsuario, Integer idCurso) {
        Set<UsuarioEntity> usuariosEntitySet = null;
        CursoOfertadoVeranoEntity cursoOfertadoVeranoEntityGuardado = null;
        Optional<UsuarioEntity> usuarioEntityOptional = usuarioRepository.findById(idUsuario);
        Optional<CursoOfertadoVeranoEntity> cursoEntityOptional = cursoRepository.findById(idCurso);
            if(usuarioEntityOptional.isPresent() && cursoEntityOptional.isPresent()) {
            UsuarioEntity usuarioEntity = usuarioEntityOptional.get();
            CursoOfertadoVeranoEntity cursoEntity = cursoEntityOptional.get();

            usuariosEntitySet = cursoEntity.getEstudiantesInscritos();
            usuariosEntitySet.remove(usuarioEntity);
            cursoEntity.setEstudiantesInscritos(usuariosEntitySet);
            
            cursoOfertadoVeranoEntityGuardado = cursoRepository.save(cursoEntity);
        }
        


        return cursoMapper.map(cursoOfertadoVeranoEntityGuardado, CursoOfertadoVerano.class);
    }
}