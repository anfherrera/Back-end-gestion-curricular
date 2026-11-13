package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.Date;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.GrupoCursoVerano;

public interface GestionarCursoOfertadoVeranoCUIntPort {
    
    record DatosCreacion(int idMateria, int idDocente, int cupoEstimado, Date fechaInicio, Date fechaFin,
                         String periodo, Integer idSalon, String espacioAsignado, String grupo, String estado) {
    }

    CursoOfertadoVerano crearCurso(CursoOfertadoVerano curso);

    CursoOfertadoVerano crearCurso(DatosCreacion datos);

    String resolverSalon(Integer idSalon, String espacioAsignado);

    CursoOfertadoVerano actualizarCurso(CursoOfertadoVerano curso, EstadoCursoOfertado estadoCurso);

    boolean eliminarCurso(Integer idCurso);
    
    CursoOfertadoVerano obtenerCursoPorId(Integer idCurso);

    List<CursoOfertadoVerano> listarTodos();

    List<CursoOfertadoVerano> listarPorFiltros(String periodo, Integer idPrograma, boolean soloVisibles);

    /** Registra al estudiante en la lista del curso. @return true si quedó asociado */
    boolean asociarEstudiante(Integer idCurso, Integer idUsuario);

    /**
     * Cambia cupo, salón y/o estado de un curso. Los parámetros null no se modifican.
     * El cambio de estado debe respetar {@link co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.TransicionEstadoCurso}.
     *
     * @return el curso actualizado, o null si no existe
     * @throws co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.TransicionEstadoCurso.TransicionInvalidaException
     *         si la transición de estado no está permitida
     */
    CursoOfertadoVerano modificarCurso(Integer idCurso, Integer cupoEstimado, String salon, String nuevoEstado);

    /** true si ya existe un curso con la misma materia, docente, período y grupo. */
    boolean existeCurso(Integer idMateria, Integer idDocente, String periodoAcademico, GrupoCursoVerano grupo);

}
