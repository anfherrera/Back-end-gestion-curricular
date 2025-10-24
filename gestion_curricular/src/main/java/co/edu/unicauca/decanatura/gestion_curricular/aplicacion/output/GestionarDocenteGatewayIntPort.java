package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.List;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;

public interface GestionarDocenteGatewayIntPort {
    
    // Método original (mantener compatibilidad)
    Docente buscarDocentePorId(Integer idDocente);
    
    // Nuevos métodos CRUD
    Docente guardarDocente(Docente docente);

    Docente actualizarDocente(Docente docente);

    boolean eliminarDocente(Integer idDocente);

    Docente obtenerDocentePorId(Integer idDocente);

    Docente obtenerDocentePorCodigo(String codigo);

    boolean existeDocentePorCodigo(String codigo);

    List<Docente> buscarPorNombreParcial(String nombreParcial);

    List<Docente> listarDocentes();
    
    boolean tieneCursosAsignados(Integer idDocente);
}
