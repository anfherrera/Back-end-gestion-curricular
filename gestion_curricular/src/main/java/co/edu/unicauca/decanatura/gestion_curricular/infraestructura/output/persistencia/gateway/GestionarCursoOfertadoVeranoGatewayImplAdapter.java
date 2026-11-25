package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarCursoOfertadoVeranoGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.GrupoCursoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.CursoOfertadoVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.DocenteEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoCursoOfertadoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.MateriaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.enums.GrupoCursoVeranoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers.SolicitudMapperPersistencia;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.CursoOfertadoVeranoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.DocenteRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.EstadoCursoOfertadoRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.MateriaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;


@Service
@Transactional
public class GestionarCursoOfertadoVeranoGatewayImplAdapter implements GestionarCursoOfertadoVeranoGatewayIntPort {

    private final CursoOfertadoVeranoRepositoryInt cursoRepository;
    private final UsuarioRepositoryInt usuarioRepository;
    private final EstadoCursoOfertadoRepositoryInt estadoCursoOfertadoRepository;
    private final DocenteRepositoryInt docenteRepository;
    private final MateriaRepositoryInt materiaRepository;

    public GestionarCursoOfertadoVeranoGatewayImplAdapter(CursoOfertadoVeranoRepositoryInt cursoRepository, UsuarioRepositoryInt usuarioRepository,EstadoCursoOfertadoRepositoryInt estadoCursoOfertadoRepository, DocenteRepositoryInt docenteRepository, MateriaRepositoryInt materiaRepository) {
        this.cursoRepository = cursoRepository;
        this.usuarioRepository = usuarioRepository;
        this.estadoCursoOfertadoRepository = estadoCursoOfertadoRepository;
        this.docenteRepository = docenteRepository;
        this.materiaRepository = materiaRepository;
    }

    @Override
    @Transactional
    public CursoOfertadoVerano crearCurso(CursoOfertadoVerano curso) {
        // Crear entidad del curso (nueva instancia para evitar reutilización)
        CursoOfertadoVeranoEntity cursoEntity = new CursoOfertadoVeranoEntity();
        cursoEntity.setCupo_estimado(curso.getCupoEstimado());
        cursoEntity.setSalon(curso.getSalon());
        cursoEntity.setFecha_inicio(curso.getFecha_inicio()); // Guardar fecha de inicio directamente en el curso
        cursoEntity.setFecha_fin(curso.getFecha_fin()); // Guardar fecha de fin directamente en el curso
        cursoEntity.setPeriodo_academico(curso.getPeriodoAcademico()); // Guardar período académico directamente en el curso
        
        // Mapear grupo
        GrupoCursoVeranoEntity grupoEntity = SolicitudMapperPersistencia.aEntidad(curso.getGrupo());
        cursoEntity.setGrupo(grupoEntity);
        
        // Obtener y asignar materia existente de la base de datos
        if (curso.getObjMateria() != null && curso.getObjMateria().getIdMateria() != null) {
            // Obtener la materia desde la base de datos (forzar nueva consulta)
            MateriaEntity materiaEntity = materiaRepository.findById(curso.getObjMateria().getIdMateria())
                .orElseThrow(() -> new RuntimeException("Materia no encontrada con ID: " + curso.getObjMateria().getIdMateria()));
            
            // Asignar la materia al curso
            cursoEntity.setObjMateria(materiaEntity);
        } else {
            throw new RuntimeException("La materia es requerida para crear el curso");
        }
        
        // Obtener y asignar docente existente de la base de datos
        if (curso.getObjDocente() != null && curso.getObjDocente().getIdDocente() != null) {
            // Obtener el docente desde la base de datos (forzar nueva consulta)
            DocenteEntity docenteEntity = docenteRepository.findById(curso.getObjDocente().getIdDocente())
                .orElseThrow(() -> new RuntimeException("Docente no encontrado con ID: " + curso.getObjDocente().getIdDocente()));
            
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
                estadoCursoEntity.setEstado_actual(estadoCurso.getEstadoActual());
                estadoCursoEntity.setFecha_registro_estado(estadoCurso.getFechaRegistroEstado() != null ? 
                    estadoCurso.getFechaRegistroEstado() : new Date());
                // fecha_fin y periodo_academico ahora están solo en el curso, no en estados
                estadoCursoEntity.setObjCursoOfertadoVerano(cursoEntity);
                estadosCursoEntity.add(estadoCursoEntity);
            }
        } else {
            // Si no hay estado, usar "Abierto" por defecto
            EstadoCursoOfertadoEntity estadoCurso = new EstadoCursoOfertadoEntity();
            estadoCurso.setFecha_registro_estado(new Date());
            estadoCurso.setEstado_actual("Abierto");
            // fecha_fin y periodo_academico ahora están solo en el curso, no en estados
            estadoCurso.setObjCursoOfertadoVerano(cursoEntity);
            estadosCursoEntity.add(estadoCurso);
        }
        cursoEntity.setEstadosCursoOfertados(estadosCursoEntity); 

        // Verificar que el docente y la materia están correctamente asignados antes de guardar
        // Guardar el curso
        CursoOfertadoVeranoEntity saved = cursoRepository.save(cursoEntity);
        
        // Forzar flush para asegurar que se guarde inmediatamente
        cursoRepository.flush();
        
        // Verificar que el curso guardado tiene el docente correcto
        // Obtener el curso guardado desde la base de datos para asegurar que tiene los datos correctos
        // Esto evita problemas de caché y asegura que obtenemos los datos reales desde la BD
        CursoOfertadoVeranoEntity cursoDesdeBD = cursoRepository.findById(saved.getId_curso())
            .orElseThrow(() -> new RuntimeException("Curso no encontrado después de guardar con ID: " + saved.getId_curso()));
        

        return SolicitudMapperPersistencia.aDominioCurso(cursoDesdeBD);
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
            .map(entity -> SolicitudMapperPersistencia.aDominioCurso(entity))
            .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CursoOfertadoVerano> listarTodos() {
        return cursoRepository.findAll().stream()
            .map(entity -> SolicitudMapperPersistencia.aDominioCurso(entity))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CursoOfertadoVerano> listarPorPeriodo(String periodo) {
        return cursoRepository.listarPorPeriodo(periodo).stream()
                .map(entity -> SolicitudMapperPersistencia.aDominioCurso(entity))
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
    public CursoOfertadoVerano guardarCambiosCurso(Integer idCurso, Integer cupoEstimado, String salon, String nuevoEstado) {
        CursoOfertadoVeranoEntity curso = cursoRepository.findByIdConEstados(idCurso);
        if (curso == null) {
            return null;
        }
        if (cupoEstimado != null) {
            curso.setCupo_estimado(cupoEstimado);
        }
        if (salon != null) {
            curso.setSalon(salon);
        }
        CursoOfertadoVeranoEntity guardado = cursoRepository.save(curso);
        if (nuevoEstado != null) {
            EstadoCursoOfertadoEntity estado = new EstadoCursoOfertadoEntity();
            estado.setEstado_actual(nuevoEstado);
            estado.setFecha_registro_estado(new Date());
            estado.setObjCursoOfertadoVerano(guardado);
            estadoCursoOfertadoRepository.save(estado);
        }
        return SolicitudMapperPersistencia.aDominioCurso(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeCurso(Integer idMateria, Integer idDocente, String periodoAcademico, GrupoCursoVerano grupo) {
        GrupoCursoVeranoEntity grupoEntity = grupo != null
                ? GrupoCursoVeranoEntity.valueOf(grupo.name())
                : GrupoCursoVeranoEntity.A;
        return cursoRepository.existePorMateriaDocentePeriodoGrupo(idMateria, idDocente, periodoAcademico, grupoEntity);
    }
}