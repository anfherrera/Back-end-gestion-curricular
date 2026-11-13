package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadoSolicitudCursoVerano;
import java.util.Date;
import java.util.List;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

public interface GestionarSolicitudCursoVeranoCUIntPort {
    SolicitudCursoVeranoPreinscripcion crearSolicitudCursoVeranoPreinscripcion(SolicitudCursoVeranoPreinscripcion solicitudCursoVerano);
    SolicitudCursoVeranoIncripcion crearSolicitudCursoVeranoInscripcion(SolicitudCursoVeranoIncripcion solicitudCursoVerano);
    
    // Métodos para seguimiento de solicitudes
    List<SolicitudCursoVeranoPreinscripcion> buscarSolicitudesPorUsuario(Integer idUsuario);
    SolicitudCursoVeranoPreinscripcion buscarSolicitudPorId(Integer idSolicitud);
    SolicitudCursoVeranoIncripcion buscarPorIdInscripcion(Integer idSolicitud);
    
    // Métodos para gestión de funcionarios
    List<SolicitudCursoVeranoPreinscripcion> buscarPreinscripcionesPorCurso(Integer idCurso);
    List<SolicitudCursoVeranoIncripcion> buscarInscripcionesPorCurso(Integer idCurso);
    
    List<Solicitud> listarSolicitudesParaReporte(Integer idCurso, String estado, String periodo, Date inicio, Date fin);

    ListadoEstudiantes listarEstudiantesPorCurso(Integer idCurso);

    record ListadoEstudiantes(List<EstudianteCurso> estudiantes, int totalPreinscritos, int totalInscritos) {
    }

    record EstudianteCurso(Usuario usuario, SolicitudCursoVeranoPreinscripcion preinscripcion,
                           SolicitudCursoVeranoIncripcion inscripcion, String estadoPreinscripcion,
                           String estadoInscripcion) {
        public boolean inscrito() {
            return EstadoSolicitudCursoVerano.PAGO_VALIDADO.texto().equals(estadoInscripcion);
        }
    }

    // Métodos para gestión de funcionarios - Aprobar/Rechazar
    SolicitudCursoVeranoPreinscripcion aprobarPreinscripcion(Integer idSolicitud, String comentarios);
    SolicitudCursoVeranoPreinscripcion rechazarPreinscripcion(Integer idSolicitud, String motivo);
    SolicitudCursoVeranoIncripcion validarPago(Integer idSolicitud, boolean esValido, String observaciones);
    SolicitudCursoVeranoIncripcion completarInscripcion(Integer idSolicitud);

    // Consultas que usan los controladores de cursos de verano
    /** Preinscripción del usuario al curso, o null si no tiene. */
    Solicitud buscarPreinscripcionPorUsuarioYCurso(Integer idUsuario, Integer idCurso);
    SolicitudCursoVeranoPreinscripcion buscarPreinscripcionAprobada(Integer idUsuario, Integer idCurso);
    boolean tieneInscripcionActiva(Integer idUsuario, Integer idCurso);
    List<SolicitudCursoVeranoIncripcion> buscarInscripcionesPorUsuarioYCurso(Integer idUsuario, Integer idCurso);
    List<SolicitudCursoVeranoIncripcion> buscarInscripcionesPorUsuario(Integer idUsuario);
    /** Inscripciones del curso cuyo último estado es {@code estado}. */
    Integer contarInscripcionesPorEstado(Integer idCurso, String estado);
    /** Inscripciones que ocupan cupo (pago validado o completadas). */
    Integer contarInscripcionesAceptadasPorCurso(Integer idCurso);

    /**
     * Exige que el curso tenga un cupo libre (ver {@code ReglasCupo}). Un curso null o sin id no se limita.
     *
     * @throws co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.CupoAgotadoException si está lleno
     */
    void verificarCupoDisponible(CursoOfertadoVerano curso);

    /**
     * Igual que {@link #verificarCupoDisponible}, salvo que la inscripción ya ocupe un cupo:
     * aceptarla otra vez no suma.
     */
    void verificarCupoParaAceptar(SolicitudCursoVeranoIncripcion inscripcion);
    /** Actualiza las observaciones de una preinscripción. */
    SolicitudCursoVeranoPreinscripcion actualizarObservacionesPreinscripcion(SolicitudCursoVeranoPreinscripcion preinscripcion);
}
