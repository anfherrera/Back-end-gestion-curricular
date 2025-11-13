package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarProgramasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarProgramaGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;

public class GestionarProgramasCUAdapter implements GestionarProgramasCUIntPort {

    private final GestionarProgramaGatewayIntPort programasGateway;
    private final FormateadorResultadosIntPort formateadorResultados;

    public GestionarProgramasCUAdapter(GestionarProgramaGatewayIntPort programasGateway,
                                       FormateadorResultadosIntPort formateadorResultados) {
        this.programasGateway = programasGateway;
        this.formateadorResultados = formateadorResultados;
    }

    @Override
    public Programa guardarPrograma(Programa programa) {
        if (programa == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("El programa no puede ser nulo.");
        }

        programa.setId_programa(null);

        if (programasGateway.existeProgramaPorCodigo(programa.getCodigo())) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Ya existe un programa con este código.");
        }

        return programasGateway.guardarPrograma(programa);
    }

    @Override
    public Programa actualizarPrograma(Programa programa) {
        if (programa == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("El programa no puede ser nulo.");
        }
        if (programa.getId_programa() == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un ID válido para actualizar.");
        }
        Programa existente = programasGateway.obtenerProgramaPorId(programa.getId_programa());
        if (existente == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró el programa a actualizar.");
        }
        return programasGateway.actualizarPrograma(programa);
    }

    @Override
    public boolean eliminarPrograma(Integer idPrograma) {
        if (idPrograma == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un ID válido.");
        }

        Programa existente = programasGateway.obtenerProgramaPorId(idPrograma);
        if (existente == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró el programa a eliminar.");
        }

        // Verificar si el programa tiene usuarios asociados antes de eliminarlo
        if (programasGateway.tieneUsuariosAsociados(idPrograma)) {
            formateadorResultados.retornarRespuestaErrorReglaDeNegocio(
                "No se puede eliminar el programa porque tiene usuarios asociados. " +
                "Por favor, reasigne los usuarios a otro programa antes de eliminar."
            );
        }

        return programasGateway.eliminarPrograma(idPrograma);
    }

    @Override
    public Programa obtenerProgramaPorId(Integer idPrograma) {
        if (idPrograma == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un ID válido.");
        }

        Programa programa = programasGateway.obtenerProgramaPorId(idPrograma);
        if (programa == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Programa no encontrado.");
        }

        return programa;
    }

    @Override
    public Programa obtenerProgramaPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un código válido.");
        }

        Programa programa = programasGateway.obtenerProgramaPorCodigo(codigo);
        if (programa == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Programa no encontrado con ese código.");
        }

        return programa;
    }

    @Override
    public boolean existeProgramaPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un código válido.");
        }

        return programasGateway.existeProgramaPorCodigo(codigo);
    }

    @Override
    public List<Programa> buscarPorNombreParcial(String nombreParcial) {
        if (nombreParcial == null || nombreParcial.isBlank()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un nombre parcial.");
        }

        return programasGateway.buscarPorNombreParcial(nombreParcial);
    }

    @Override
    public List<Programa> listarProgramas() {
        return programasGateway.listarProgramas();
    }
}

