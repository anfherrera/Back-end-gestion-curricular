package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Estadistica;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadisticaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ProgramaEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudCursoVeranoInscripcionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudCursoVeranoPreinscripcionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudEcaesEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudHomologacionEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudPazYSalvoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudReingresoEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.proyecciones.FilaEstadoEstadistica;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.proyecciones.FilaSolicitudEstadistica;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.EstadisticaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.ProgramaRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.SolicitudRepositoryInt;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios.UsuarioRepositoryInt;

/**
 * Lee de la base de datos lo que necesitan las estadísticas. Los cálculos están en el dominio
 * ({@code dominio.servicios.estadisticas}).
 */
@Service
@Transactional
public class GestionarEstadisticasGatewayImplAdapter implements GestionarEstadisticasGatewayIntPort {

    private static final int ROL_ESTUDIANTE = 2;

    private final EstadisticaRepositoryInt estadisticaRepository;
    private final SolicitudRepositoryInt solicitudRepository;
    private final ProgramaRepositoryInt programaRepository;
    private final UsuarioRepositoryInt usuarioRepository;
    private final ModelMapper estadisticaMapper;

    public GestionarEstadisticasGatewayImplAdapter(EstadisticaRepositoryInt estadisticaRepository, ProgramaRepositoryInt programaRepository, SolicitudRepositoryInt solicitudRepository, UsuarioRepositoryInt usuarioRepository, ModelMapper estadisticaMapper) {
        this.estadisticaRepository = estadisticaRepository;
        this.programaRepository = programaRepository;
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
        this.estadisticaMapper = estadisticaMapper;
    }

    @Override
    @Transactional
    public Estadistica crearEstadistica(Estadistica estadistica) {
        if(solicitudRepository.count() <= 0) {
            throw new RuntimeException("No hay solicitudes disponibles para crear estadisticas.");
        }
        if(programaRepository.count() <= 0) {
            throw new RuntimeException("No hay programas disponibles para crear estadisticas.");
        }
        EstadisticaEntity estadisticaEntity = estadisticaMapper.map(estadistica, EstadisticaEntity.class);
        estadisticaEntity.setTotal_solicitudes(solicitudRepository.totalSolicitudes());
        estadisticaEntity.setTotal_aprobadas(solicitudRepository.contarEstado("APROBADA"));
        estadisticaEntity.setTotal_rechazadas(solicitudRepository.contarEstado("RECHAZADA"));
        estadisticaEntity.setNombres_procesos(new ArrayList<String>(solicitudRepository.buscarNombresSolicitudes()));
        estadisticaEntity.setPeriodos_academico(new ArrayList<Date>());
        estadisticaEntity.setNombres_programas(new ArrayList<String>(programaRepository.buscarNombresProgramas()));
        EstadisticaEntity savedEntity = estadisticaRepository.save(estadisticaEntity);
        return estadisticaMapper.map(savedEntity, Estadistica.class);
    }

    @Override
    @Transactional
    public Estadistica actualizarEstadistica(Estadistica estadistica) {
        estadisticaRepository.findById(estadistica.getId_estadistica())
            .orElseThrow(() -> new RuntimeException("Estadistica no encontrada con ID: " + estadistica.getId_estadistica()));
        EstadisticaEntity estadisticaEntity = estadisticaMapper.map(estadistica, EstadisticaEntity.class);
        estadisticaEntity.setTotal_solicitudes(solicitudRepository.totalSolicitudes());
        estadisticaEntity.setTotal_aprobadas(solicitudRepository.contarEstado("APROBADA"));
        estadisticaEntity.setTotal_rechazadas(solicitudRepository.contarEstado("RECHAZADA"));
        estadisticaEntity.setNombres_procesos(new ArrayList<String>(solicitudRepository.buscarNombresSolicitudes()));
        estadisticaEntity.setPeriodos_academico(new ArrayList<Date>());
        estadisticaEntity.setNombres_programas(new ArrayList<String>(programaRepository.buscarNombresProgramas()));
        EstadisticaEntity updatedEntity = estadisticaRepository.save(estadisticaEntity);
        return estadisticaMapper.map(updatedEntity, Estadistica.class);
    }

    @Override
    @Transactional
    public Boolean eliminarEstadistica(Integer idEstadistica) {
        boolean eliminado = false;
        Optional<EstadisticaEntity> estadisticaEntity = estadisticaRepository.findById(idEstadistica);
        if (estadisticaEntity != null) {
            estadisticaRepository.deleteById(idEstadistica);
            eliminado = true;
        }
        return eliminado;
    }

    @Override
    @Transactional(readOnly = true)
    public Estadistica obtenerEstadisticaPorId(Integer idEstadistica) {
       EstadisticaEntity entity = estadisticaRepository.findById(idEstadistica)
        .orElseThrow(() -> new IllegalArgumentException("Estadistica no encontrada"));
        return estadisticaMapper.map(entity, Estadistica.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Estadistica obtenerEstadisticasSolicitudPeriodoEstadoYPrograma(Integer idEstadistica, String proceso,
            Date fechaInicio, Date fechaFin, String estado, Integer idPrograma) {
        EstadisticaEntity estadisticaEntity = estadisticaRepository.findById(idEstadistica)
            .orElseThrow(() -> new IllegalArgumentException("Estadistica no encontrada con ID: " + idEstadistica));

        if(solicitudRepository.count() == 0) {
            throw new RuntimeException("No hay solicitudes disponibles para crear estadisticas.");
        }

        int totalSolicitudes = solicitudRepository.contarNombreFechaEstadoYPrograma(
            proceso, fechaInicio, fechaFin, estado, idPrograma);
        estadisticaEntity.setTotal_solicitudes(totalSolicitudes);
        return estadisticaMapper.map(estadisticaEntity, Estadistica.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Estadistica> obtenerEstadisticasPeriodoEstadoYPrograma(Date fechaInicio, Date fechaFin,
            Integer idPrograma) {
        List<EstadisticaEntity> estadisticaEntityOp = estadisticaRepository.findAll();
        List<Estadistica> estadisticas = new ArrayList<>();
        if(estadisticaEntityOp!=null){
            List<String> nombres_procesos = new ArrayList<String>(solicitudRepository.buscarNombresSolicitudes());
            for (String proceso : nombres_procesos) {
                Estadistica estadistica = new Estadistica();
                estadistica.setNombre(proceso);
                estadistica.setTotal_solicitudes(solicitudRepository.contarNombreFechaYPrograma(proceso, fechaInicio, fechaFin, idPrograma));
                estadistica.setTotal_aprobadas(solicitudRepository.contarNombreFechaEstadoYPrograma(proceso, fechaInicio, fechaFin, "APROBADA", idPrograma));
                estadistica.setTotal_rechazadas(solicitudRepository.contarNombreFechaEstadoYPrograma(proceso, fechaInicio, fechaFin, "RECHAZADA", idPrograma));
                estadisticas.add(estadistica);
            }
        }
        return estadisticas;
    }

    // ------------------------------------------------------------------------------------------
    // Datos para las estadísticas
    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<Solicitud> listarSolicitudesConEstadosYUsuario() {
        // Dos consultas de columnas sueltas (solicitudes y estados) en lugar de cargar las entidades completas
        Map<Integer, List<EstadoSolicitud>> estadosPorSolicitud = new HashMap<>();
        for (FilaEstadoEstadistica fila : solicitudRepository.buscarEstadosParaEstadisticas()) {
            EstadoSolicitud estado = new EstadoSolicitud();
            estado.setEstado_actual(fila.estado());
            estado.setFecha_registro_estado(fila.fecha());
            estadosPorSolicitud.computeIfAbsent(fila.idSolicitud(), id -> new ArrayList<>()).add(estado);
        }
        return solicitudRepository.buscarSolicitudesParaEstadisticas().stream()
                .map(fila -> aDominio(fila, estadosPorSolicitud.getOrDefault(fila.idSolicitud(), new ArrayList<>())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Programa> listarProgramas() {
        return programaRepository.findAll().stream().map(GestionarEstadisticasGatewayImplAdapter::aDominio).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listarNombresProgramas() {
        return programaRepository.buscarNombresProgramas();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarEstudiantes() {
        return usuarioRepository.buscarPorRol(ROL_ESTUDIANTE).stream()
                .map(GestionarEstadisticasGatewayImplAdapter::aDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public int contarSolicitudesPorEstado(String estado) {
        return cero(solicitudRepository.contarEstado(estado));
    }

    @Override
    @Transactional(readOnly = true)
    public int contarTotalSolicitudes() {
        return cero(solicitudRepository.totalSolicitudes());
    }

    @Override
    @Transactional(readOnly = true)
    public int contarSolicitudesPorEstadoConFiltros(String estado, String proceso, Integer idPrograma, Date fechaInicio, Date fechaFin) {
        return cero(solicitudRepository.contarSolicitudesPorEstadoConFiltros(estado, proceso, idPrograma, fechaInicio, fechaFin));
    }

    @Override
    @Transactional(readOnly = true)
    public int contarSolicitudesConFiltros(String proceso, Integer idPrograma, Date fechaInicio, Date fechaFin) {
        return cero(solicitudRepository.contarSolicitudesConFiltros(proceso, idPrograma, fechaInicio, fechaFin));
    }

    @Override
    @Transactional(readOnly = true)
    public int contarSolicitudesPorProgramaConFiltros(Integer idPrograma, String proceso, Date fechaInicio, Date fechaFin) {
        return cero(solicitudRepository.contarSolicitudesPorProgramaConFiltros(idPrograma, proceso, fechaInicio, fechaFin));
    }

    private static int cero(Integer valor) {
        return valor != null ? valor : 0;
    }

    // ------------------------------------------------------------------------------------------
    // Conversión a modelos de dominio: solo los campos que usan las estadísticas, sin referencias de vuelta
    // ------------------------------------------------------------------------------------------

    /** Subtipo de solicitud de dominio para cada clase de entidad: es lo que define su proceso (ver TipoProcesoSolicitud). */
    private static final Map<Class<?>, Supplier<Solicitud>> SUBTIPOS = Map.of(
            SolicitudHomologacionEntity.class, SolicitudHomologacion::new,
            SolicitudPazYSalvoEntity.class, SolicitudPazYSalvo::new,
            SolicitudReingresoEntity.class, SolicitudReingreso::new,
            SolicitudCursoVeranoPreinscripcionEntity.class, SolicitudCursoVeranoPreinscripcion::new,
            SolicitudCursoVeranoInscripcionEntity.class, SolicitudCursoVeranoIncripcion::new,
            SolicitudEcaesEntity.class, SolicitudEcaes::new);

    private static Solicitud aDominio(FilaSolicitudEstadistica fila, List<EstadoSolicitud> estados) {
        Solicitud solicitud = SUBTIPOS.getOrDefault(fila.tipo(), Solicitud::new).get();
        solicitud.setId_solicitud(fila.idSolicitud());
        solicitud.setNombre_solicitud(fila.nombre());
        solicitud.setPeriodo_academico(fila.periodoAcademico());
        solicitud.setFecha_registro_solicitud(fila.fechaRegistro());
        solicitud.setEstadosSolicitud(estados);

        if (fila.idUsuario() != null) {
            Usuario usuario = new Usuario();
            usuario.setId_usuario(fila.idUsuario());
            usuario.setNombre_completo(fila.nombreUsuario());
            if (fila.idPrograma() != null) {
                usuario.setObjPrograma(new Programa(fila.idPrograma(), fila.codigoPrograma(), fila.nombrePrograma()));
            }
            solicitud.setObjUsuario(usuario);
        }
        if (fila.idCurso() != null) {
            CursoOfertadoVerano curso = new CursoOfertadoVerano();
            curso.setId_curso(fila.idCurso());
            curso.setPeriodo_academico(fila.periodoCurso());
            if (fila.idMateria() != null) {
                curso.setObjMateria(new Materia(fila.idMateria(), fila.codigoMateria(), fila.nombreMateria(), null));
            }
            solicitud.setObjCursoOfertadoVerano(curso);
        }
        return solicitud;
    }

    private static Usuario aDominio(UsuarioEntity entidad) {
        Usuario usuario = new Usuario();
        usuario.setId_usuario(entidad.getId_usuario());
        usuario.setNombre_completo(entidad.getNombre_completo());
        if (entidad.getObjPrograma() != null) {
            usuario.setObjPrograma(aDominio(entidad.getObjPrograma()));
        }
        return usuario;
    }

    private static Programa aDominio(ProgramaEntity entidad) {
        return new Programa(entidad.getId_programa(), entidad.getCodigo(), entidad.getNombre_programa());
    }
}
