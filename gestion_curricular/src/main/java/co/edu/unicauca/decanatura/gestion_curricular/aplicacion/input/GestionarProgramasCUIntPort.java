package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.List;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;

public interface GestionarProgramasCUIntPort {

    Programa guardarPrograma(Programa programa);

    Programa actualizarPrograma(Programa programa);

    boolean eliminarPrograma(Integer idPrograma);

    Programa obtenerProgramaPorId(Integer idPrograma);

    Programa obtenerProgramaPorCodigo(String codigo);

    boolean existeProgramaPorCodigo(String codigo);

    List<Programa> buscarPorNombreParcial(String nombreParcial);

    List<Programa> listarProgramas();
}

