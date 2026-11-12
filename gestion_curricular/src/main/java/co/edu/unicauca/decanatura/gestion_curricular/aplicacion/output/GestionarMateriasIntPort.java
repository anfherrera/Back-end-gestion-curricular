package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;
import java.util.List;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;

public interface GestionarMateriasIntPort {

    Materia obtenerMateriaPorId(Integer idMateria);

    List<Materia> buscarPorNombreParcial(String nombreParcial);

    List<Materia> buscarPorCreditos(Integer creditos);

    List<Materia> listarMaterias();
    
}
