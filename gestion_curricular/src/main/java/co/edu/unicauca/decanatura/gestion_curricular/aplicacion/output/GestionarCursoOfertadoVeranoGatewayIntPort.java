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
    
    List<CursoOfertadoVerano> buscarPorGrupo(GrupoCursoVerano grupo);
    
    List<CursoOfertadoVerano> buscarPorMateria(Integer idMateria);
    
    List<CursoOfertadoVerano> buscarPorDocente(Integer idDocente);
    
    Integer contarPorSalon(String salon);
    
    List<CursoOfertadoVerano> listarTodos();

    Boolean asociarUsuarioCurso(Integer idUsuario, Integer idCurso);

    Boolean desasociarUsuarioCurso(Integer idUsuario, Integer idCurso);
}
