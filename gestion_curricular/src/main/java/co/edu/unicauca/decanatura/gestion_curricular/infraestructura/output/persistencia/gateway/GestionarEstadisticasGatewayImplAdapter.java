package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.gateway;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarEstadisticasGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoInscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudReingreso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
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

    private final SolicitudRepositoryInt solicitudRepository;
    private final ProgramaRepositoryInt programaRepository;
    private final UsuarioRepositoryInt usuarioRepository;

    public GestionarEstadisticasGatewayImplAdapter(ProgramaRepositoryInt programaRepository, SolicitudRepositoryInt solicitudRepository, UsuarioRepositoryInt usuarioRepository) {
        this.programaRepository = programaRepository;
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
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
            estado.setEstadoActual(fila.estado());
            estado.setFechaRegistroEstado(fila.fecha());
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
            SolicitudCursoVeranoInscripcionEntity.class, SolicitudCursoVeranoInscripcion::new,
            SolicitudEcaesEntity.class, SolicitudEcaes::new);

    private static Solicitud aDominio(FilaSolicitudEstadistica fila, List<EstadoSolicitud> estados) {
        Solicitud solicitud = SUBTIPOS.getOrDefault(fila.tipo(), Solicitud::new).get();
        solicitud.setIdSolicitud(fila.idSolicitud());
        solicitud.setNombreSolicitud(fila.nombre());
        solicitud.setPeriodoAcademico(fila.periodoAcademico());
        solicitud.setFechaRegistroSolicitud(fila.fechaRegistro());
        solicitud.setEstadosSolicitud(estados);

        if (fila.idUsuario() != null) {
            Usuario usuario = new Usuario();
            usuario.setIdUsuario(fila.idUsuario());
            usuario.setNombreCompleto(fila.nombreUsuario());
            if (fila.idPrograma() != null) {
                usuario.setObjPrograma(new Programa(fila.idPrograma(), fila.codigoPrograma(), fila.nombrePrograma()));
            }
            solicitud.setObjUsuario(usuario);
        }
        if (fila.idCurso() != null) {
            CursoOfertadoVerano curso = new CursoOfertadoVerano();
            curso.setIdCurso(fila.idCurso());
            curso.setPeriodoAcademico(fila.periodoCurso());
            if (fila.idMateria() != null) {
                curso.setObjMateria(new Materia(fila.idMateria(), fila.codigoMateria(), fila.nombreMateria(), null));
            }
            solicitud.setObjCursoOfertadoVerano(curso);
        }
        return solicitud;
    }

    private static Usuario aDominio(UsuarioEntity entidad) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(entidad.getId_usuario());
        usuario.setNombreCompleto(entidad.getNombre_completo());
        if (entidad.getObjPrograma() != null) {
            usuario.setObjPrograma(aDominio(entidad.getObjPrograma()));
        }
        return usuario;
    }

    private static Programa aDominio(ProgramaEntity entidad) {
        return new Programa(entidad.getId_programa(), entidad.getCodigo(), entidad.getNombre_programa());
    }
}
