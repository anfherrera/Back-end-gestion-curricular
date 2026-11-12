package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.GrupoCursoVerano;

public interface GestionarCursoOfertadoVeranoCUIntPort {
    
    CursoOfertadoVerano crearCurso(CursoOfertadoVerano curso);

    CursoOfertadoVerano actualizarCurso(CursoOfertadoVerano curso, EstadoCursoOfertado estadoCurso);

    boolean eliminarCurso(Integer idCurso);
    
    CursoOfertadoVerano obtenerCursoPorId(Integer idCurso);

    List<CursoOfertadoVerano> listarTodos();

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
