package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.GrupoCursoVerano;

import java.util.List;

public interface GestionarCursoOfertadoVeranoGatewayIntPort {

    CursoOfertadoVerano crearCurso(CursoOfertadoVerano curso);
    
    CursoOfertadoVerano actualizarCurso(CursoOfertadoVerano curso, EstadoCursoOfertado estadoCurso);
    
    boolean eliminarCurso(Integer idCurso);
    
    CursoOfertadoVerano obtenerCursoPorId(Integer idCurso);
    
    List<CursoOfertadoVerano> listarTodos();

    List<CursoOfertadoVerano> listarPorPeriodo(String periodo);

    Boolean asociarUsuarioCurso(Integer idUsuario, Integer idCurso);

    Boolean desasociarUsuarioCurso(Integer idUsuario, Integer idCurso);

    /**
     * Guarda cupo y salón del curso y, si {@code nuevoEstado} no es null, le agrega ese estado.
     * Los parámetros null no se modifican.
     */
    CursoOfertadoVerano guardarCambiosCurso(Integer idCurso, Integer cupoEstimado, String salon, String nuevoEstado);

    /** true si ya existe un curso con la misma materia, docente, período y grupo. */
    boolean existeCurso(Integer idMateria, Integer idDocente, String periodoAcademico, GrupoCursoVerano grupo);
}
