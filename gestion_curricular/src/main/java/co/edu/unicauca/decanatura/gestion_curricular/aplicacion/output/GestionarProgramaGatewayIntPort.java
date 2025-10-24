package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.List;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;

public interface GestionarProgramaGatewayIntPort {
    
    // Método original (mantener compatibilidad)
    Programa buscarPorIdPrograma(Integer idPrograma);
    
    // Nuevos métodos CRUD
    Programa guardarPrograma(Programa programa);

    Programa actualizarPrograma(Programa programa);

    boolean eliminarPrograma(Integer idPrograma);

    Programa obtenerProgramaPorId(Integer idPrograma);

    Programa obtenerProgramaPorCodigo(String codigo);

    boolean existeProgramaPorCodigo(String codigo);

    List<Programa> buscarPorNombreParcial(String nombreParcial);

    List<Programa> listarProgramas();
    
    boolean tieneUsuariosAsociados(Integer idPrograma);
}
