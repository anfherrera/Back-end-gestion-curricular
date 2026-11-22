package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarProgramasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarProgramaGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadYaExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.ReglaNegocioExcepcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;

public class GestionarProgramasCUAdapter implements GestionarProgramasCUIntPort {

    private final GestionarProgramaGatewayIntPort programasGateway;

    public GestionarProgramasCUAdapter(GestionarProgramaGatewayIntPort programasGateway) {
        this.programasGateway = programasGateway;
    }

    @Override
    public Programa guardarPrograma(Programa programa) {
        if (programa == null) {
            throw new ReglaNegocioExcepcion("El programa no puede ser nulo.");
        }

        programa.setIdPrograma(null);

        if (programasGateway.existeProgramaPorCodigo(programa.getCodigo())) {
            throw new EntidadYaExisteException("Ya existe un programa con este código.");
        }

        return programasGateway.guardarPrograma(programa);
    }

    @Override
    public Programa actualizarPrograma(Programa programa) {
        if (programa == null) {
            throw new ReglaNegocioExcepcion("El programa no puede ser nulo.");
        }
        if (programa.getIdPrograma() == null) {
            throw new ReglaNegocioExcepcion("Debe proporcionar un ID válido para actualizar.");
        }
        Programa existente = programasGateway.obtenerProgramaPorId(programa.getIdPrograma());
        if (existente == null) {
            throw new EntidadNoExisteException("No se encontró el programa a actualizar.");
        }
        return programasGateway.actualizarPrograma(programa);
    }

    @Override
    public boolean eliminarPrograma(Integer idPrograma) {
        if (idPrograma == null) {
            throw new ReglaNegocioExcepcion("Debe proporcionar un ID válido.");
        }

        Programa existente = programasGateway.obtenerProgramaPorId(idPrograma);
        if (existente == null) {
            throw new EntidadNoExisteException("No se encontró el programa a eliminar.");
        }

        // Verificar si el programa tiene usuarios asociados antes de eliminarlo
        if (programasGateway.tieneUsuariosAsociados(idPrograma)) {
            throw new ReglaNegocioExcepcion(
                "No se puede eliminar el programa porque tiene usuarios asociados. " +
                "Por favor, reasigne los usuarios a otro programa antes de eliminar."
            );
        }

        return programasGateway.eliminarPrograma(idPrograma);
    }

    @Override
    public Programa obtenerProgramaPorId(Integer idPrograma) {
        if (idPrograma == null) {
            throw new ReglaNegocioExcepcion("Debe proporcionar un ID válido.");
        }

        Programa programa = programasGateway.obtenerProgramaPorId(idPrograma);
        if (programa == null) {
            throw new EntidadNoExisteException("Programa no encontrado.");
        }

        return programa;
    }

    @Override
    public Programa obtenerProgramaPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new ReglaNegocioExcepcion("Debe proporcionar un código válido.");
        }

        Programa programa = programasGateway.obtenerProgramaPorCodigo(codigo);
        if (programa == null) {
            throw new EntidadNoExisteException("Programa no encontrado con ese código.");
        }

        return programa;
    }

    @Override
    public List<Programa> buscarPorNombreParcial(String nombreParcial) {
        if (nombreParcial == null || nombreParcial.isBlank()) {
            throw new ReglaNegocioExcepcion("Debe proporcionar un nombre parcial.");
        }

        return programasGateway.buscarPorNombreParcial(nombreParcial);
    }

    @Override
    public List<Programa> listarProgramas() {
        return programasGateway.listarProgramas();
    }
}

