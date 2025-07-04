package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;
import java.util.List;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;

public interface GestionarMateriasCUIntPort {

    Materia guardarMateria(Materia materia);

    Materia actualizarMateria(Materia materia);

    boolean eliminarMateria(Integer idMateria);

    Materia obtenerMateriaPorId(Integer idMateria);

    Materia obtenerMateriaPorCodigo(String codigo);

    boolean existeMateriaPorCodigo(String codigo);

    List<Materia> buscarPorNombreParcial(String nombreParcial);

    List<Materia> buscarPorCreditos(Integer creditos);

    List<Materia> listarMaterias();
}
