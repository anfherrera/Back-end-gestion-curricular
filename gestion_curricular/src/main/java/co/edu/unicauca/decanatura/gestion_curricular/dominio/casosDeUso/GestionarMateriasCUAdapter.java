package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarMateriasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarMateriasIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;

public class GestionarMateriasCUAdapter implements GestionarMateriasCUIntPort {

    private final GestionarMateriasIntPort materiasGateway;
    private final FormateadorResultadosIntPort formateadorResultados;

    public GestionarMateriasCUAdapter(GestionarMateriasIntPort materiasGateway,
                                      FormateadorResultadosIntPort formateadorResultados) {
        this.materiasGateway = materiasGateway;
        this.formateadorResultados = formateadorResultados;
    }

    @Override
    public Materia obtenerMateriaPorId(Integer idMateria) {
        if (idMateria == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un ID válido.");
        }

        Materia materia = materiasGateway.obtenerMateriaPorId(idMateria);
        if (materia == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Materia no encontrada.");
        }

        return materia;
    }

    @Override
    public List<Materia> listarMaterias() {
        return materiasGateway.listarMaterias();
    }
}
