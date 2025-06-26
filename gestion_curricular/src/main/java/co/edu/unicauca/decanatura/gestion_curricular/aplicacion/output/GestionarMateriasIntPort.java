package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;
import java.util.List;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;

public interface GestionarMateriasIntPort {

    Materia guardarMateria(Materia materia);

    Materia actualizarMateria(Materia materia);

    boolean eliminarMateria(Integer idMateria);

    Materia obtenerMateriaPorId(Integer idMateria);

    boolean existeMateriaPorCodigo(String codigo);

    List<Materia> buscarPorNombreParcial(String nombreParcial);

    List<Materia> buscarPorCreditos(Integer creditos);

    List<Materia> listarMaterias();
}
