package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarMateriasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarMateriasIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.ReglaNegocioExcepcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;

public class GestionarMateriasCUAdapter implements GestionarMateriasCUIntPort {

    private final GestionarMateriasIntPort materiasGateway;

    public GestionarMateriasCUAdapter(GestionarMateriasIntPort materiasGateway) {
        this.materiasGateway = materiasGateway;
    }

    @Override
    public Materia obtenerMateriaPorId(Integer idMateria) {
        if (idMateria == null) {
            throw new ReglaNegocioExcepcion("Debe proporcionar un ID válido.");
        }

        Materia materia = materiasGateway.obtenerMateriaPorId(idMateria);
        if (materia == null) {
            throw new EntidadNoExisteException("Materia no encontrada.");
        }

        return materia;
    }

    @Override
    public List<Materia> listarMaterias() {
        return materiasGateway.listarMaterias();
    }
}
