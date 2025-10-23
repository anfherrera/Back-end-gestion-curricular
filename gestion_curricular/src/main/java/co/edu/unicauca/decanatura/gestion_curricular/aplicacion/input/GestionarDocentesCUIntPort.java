package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.List;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;

public interface GestionarDocentesCUIntPort {

    Docente guardarDocente(Docente docente);

    Docente actualizarDocente(Docente docente);

    boolean eliminarDocente(Integer idDocente);

    Docente obtenerDocentePorId(Integer idDocente);

    Docente obtenerDocentePorCodigo(String codigo);

    boolean existeDocentePorCodigo(String codigo);

    List<Docente> buscarPorNombreParcial(String nombreParcial);

    List<Docente> listarDocentes();
}

