package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.DocenteRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.MateriaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.DocenteEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.MateriaEntity;


@Service
@Transactional
public class GestionarCursoOfertadoVeranoGatewayImplAdapter implements GestionarCursoOfertadoVeranoGatewayIntPort {

    private final CursoOfertadoVeranoRepositoryInt cursoRepository;
    private final UsuarioRepositoryInt usuarioRepository;
    private final EstadoCursoOfertadoRepositoryInt objEstadoCursoOfertadoRepository;
    private final DocenteRepositoryInt docenteRepository;
    private final MateriaRepositoryInt materiaRepository;
    private final ModelMapper cursoMapper;

    public GestionarCursoOfertadoVeranoGatewayImplAdapter(CursoOfertadoVeranoRepositoryInt cursoRepository, UsuarioRepositoryInt usuarioRepository,EstadoCursoOfertadoRepositoryInt objEstadoCursoOfertadoRepository, DocenteRepositoryInt docenteRepository, MateriaRepositoryInt materiaRepository, @Qualifier("mapperCurso") ModelMapper cursoMapper) {
        this.cursoRepository = cursoRepository;
        this.usuarioRepository = usuarioRepository;
        this.objEstadoCursoOfertadoRepository = objEstadoCursoOfertadoRepository;
        this.docenteRepository = docenteRepository;
        this.materiaRepository = materiaRepository;
        this.cursoMapper = cursoMapper;
    }

    @Override
    @Transactional
    public CursoOfertadoVerano crearCurso(CursoOfertadoVerano curso) {
        // Log para depuración
        System.out.println("DEBUG Gateway: Creando curso con docente ID: " + 
            (curso.getObjDocente() != null ? curso.getObjDocente().getId_docente() : "null") +
            " y materia ID: " + 
            (curso.getObjMateria() != null ? curso.getObjMateria().getId_materia() : "null"));
        
        // Crear entidad del curso (nueva instancia para evitar reutilización)
        CursoOfertadoVeranoEntity cursoEntity = new CursoOfertadoVeranoEntity();
        cursoEntity.setCupo_estimado(curso.getCupo_estimado());
        cursoEntity.setSalon(curso.getSalon());
        
        // Mapear grupo
        GrupoCursoVeranoEntity grupoEntity = cursoMapper.map(curso.getGrupo(), GrupoCursoVeranoEntity.class);
        cursoEntity.setGrupo(grupoEntity);
        
        // Obtener y asignar materia existente de la base de datos
        if (curso.getObjMateria() != null && curso.getObjMateria().getId_materia() != null) {
            // Obtener la materia desde la base de datos (forzar nueva consulta)
            MateriaEntity materiaEntity = materiaRepository.findById(curso.getObjMateria().getId_materia())
                .orElseThrow(() -> new RuntimeException("Materia no encontrada con ID: " + curso.getObjMateria().getId_materia()));
            
            System.out.println("DEBUG Gateway: Materia obtenida - ID: " + materiaEntity.getId_materia() + 
                             ", Nombre: " + materiaEntity.getNombre());
            
            // Asignar la materia al curso
            cursoEntity.setObjMateria(materiaEntity);
        } else {
            throw new RuntimeException("La materia es requerida para crear el curso");
        }
        
        // Obtener y asignar docente existente de la base de datos
        if (curso.getObjDocente() != null && curso.getObjDocente().getId_docente() != null) {
            // Obtener el docente desde la base de datos (forzar nueva consulta)
            DocenteEntity docenteEntity = docenteRepository.findById(curso.getObjDocente().getId_docente())
                .orElseThrow(() -> new RuntimeException("Docente no encontrado con ID: " + curso.getObjDocente().getId_docente()));
            
            System.out.println("DEBUG Gateway: Docente obtenido - ID: " + docenteEntity.getId_docente() + 
                             ", Nombre: " + docenteEntity.getNombre_docente());
            
            // Asignar el docente al curso
            cursoEntity.setObjDocente(docenteEntity);
        } else {
            throw new RuntimeException("El docente es requerido para crear el curso");
        }
        
        // Crear estado inicial del curso usando el estado del modelo si existe
        List<EstadoCursoOfertadoEntity> estadosCursoEntity = new ArrayList<>();
        if (curso.getEstadosCursoOfertados() != null && !curso.getEstadosCursoOfertados().isEmpty()) {
            // Usar el estado que viene del modelo
            for (EstadoCursoOfertado estadoCurso : curso.getEstadosCursoOfertados()) {
                EstadoCursoOfertadoEntity estadoCursoEntity = new EstadoCursoOfertadoEntity();
                estadoCursoEntity.setEstado_actual(estadoCurso.getEstado_actual());
                estadoCursoEntity.setFecha_registro_estado(estadoCurso.getFecha_registro_estado() != null ? 
                    estadoCurso.getFecha_registro_estado() : new Date());
                estadoCursoEntity.setFecha_fin(estadoCurso.getFecha_fin()); // Guardar fecha de fin
                estadoCursoEntity.setObjCursoOfertadoVerano(cursoEntity);
                estadosCursoEntity.add(estadoCursoEntity);
            }
        } else {
            // Si no hay estado, usar "Abierto" por defecto
            EstadoCursoOfertadoEntity estadoCurso = new EstadoCursoOfertadoEntity();
            estadoCurso.setFecha_registro_estado(new Date());
            estadoCurso.setEstado_actual("Abierto");
            estadoCurso.setFecha_fin(null); // Sin fecha de fin por defecto
            estadoCurso.setObjCursoOfertadoVerano(cursoEntity);
            estadosCursoEntity.add(estadoCurso);
        }
        cursoEntity.setEstadosCursoOfertados(estadosCursoEntity); 

        // Verificar que el docente y la materia están correctamente asignados antes de guardar
        System.out.println("DEBUG Gateway: Antes de guardar - Docente ID: " + 
            (cursoEntity.getObjDocente() != null ? cursoEntity.getObjDocente().getId_docente() : "null") +
            ", Nombre: " + 
            (cursoEntity.getObjDocente() != null ? cursoEntity.getObjDocente().getNombre_docente() : "null"));
        
        // Guardar el curso
        CursoOfertadoVeranoEntity saved = cursoRepository.save(cursoEntity);
        
        // Forzar flush para asegurar que se guarde inmediatamente
        cursoRepository.flush();
        
        // Verificar que el curso guardado tiene el docente correcto
        System.out.println("DEBUG Gateway: Después de guardar - Curso ID: " + saved.getId_curso() +
                         ", Docente ID: " + 
                         (saved.getObjDocente() != null ? saved.getObjDocente().getId_docente() : "null") +
                         ", Nombre: " + 
                         (saved.getObjDocente() != null ? saved.getObjDocente().getNombre_docente() : "null"));
        
        // Obtener el curso guardado desde la base de datos para asegurar que tiene los datos correctos
        // Esto evita problemas de caché y asegura que obtenemos los datos reales desde la BD
        CursoOfertadoVeranoEntity cursoDesdeBD = cursoRepository.findById(saved.getId_curso())
            .orElseThrow(() -> new RuntimeException("Curso no encontrado después de guardar con ID: " + saved.getId_curso()));
        
        System.out.println("DEBUG Gateway: Curso desde BD - Curso ID: " + cursoDesdeBD.getId_curso() +
                         ", Docente ID: " + 
                         (cursoDesdeBD.getObjDocente() != null ? cursoDesdeBD.getObjDocente().getId_docente() : "null") +
                         ", Nombre: " + 
                         (cursoDesdeBD.getObjDocente() != null ? cursoDesdeBD.getObjDocente().getNombre_docente() : "null"));

        return cursoMapper.map(cursoDesdeBD, CursoOfertadoVerano.class);
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

        //return null;
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
    public Boolean asociarUsuarioCurso(Integer idUsuario, Integer idCurso) {
        Optional<UsuarioEntity> usuarioEntityOptional = usuarioRepository.findById(idUsuario);
        Optional<CursoOfertadoVeranoEntity> cursoEntityOptional = cursoRepository.findById(idCurso);
        Boolean usuarioYaInscrito = false;
        Integer result = 0;
            if(usuarioEntityOptional.isPresent() && cursoEntityOptional.isPresent()) {
                UsuarioEntity usuarioEntity = usuarioEntityOptional.get();
                CursoOfertadoVeranoEntity cursoEntity = cursoEntityOptional.get();

                result = cursoRepository.insertarCursoEstudiante(cursoEntity.getId_curso(), usuarioEntity.getId_usuario());
                
                if (result == 1){
                    usuarioYaInscrito = true;
                }
                
                
            }
        
        

        return usuarioYaInscrito;
    }

    @Override
    @Transactional
    public Boolean desasociarUsuarioCurso(Integer idUsuario, Integer idCurso) {
        Optional<UsuarioEntity> usuarioEntityOptional = usuarioRepository.findById(idUsuario);
        Optional<CursoOfertadoVeranoEntity> cursoEntityOptional = cursoRepository.findById(idCurso);
        Boolean usuarioYaEliminado = false;
        Integer result = 0;
            if(usuarioEntityOptional.isPresent() && cursoEntityOptional.isPresent()) {
            UsuarioEntity usuarioEntity = usuarioEntityOptional.get();
            CursoOfertadoVeranoEntity cursoEntity = cursoEntityOptional.get();
            result = cursoRepository.eliminarEstudianteDeCurso(cursoEntity.getId_curso(), usuarioEntity.getId_usuario());
                if (result == 1){
                    usuarioYaEliminado = true;
                }

        }
        


        return usuarioYaEliminado;
    }
}