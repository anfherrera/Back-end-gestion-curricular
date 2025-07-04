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
    public Materia guardarMateria(Materia materia) {
        if (materia == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("La materia no puede ser nula.");
        }

        if (materia.getCodigo() == null || materia.getCodigo().isBlank()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("El código de la materia es obligatorio.");
        }

        if (materiasGateway.existeMateriaPorCodigo(materia.getCodigo())) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Ya existe una materia con este código.");
        }

        return materiasGateway.guardarMateria(materia);
    }

    @Override
    public Materia actualizarMateria(Materia materia) {
        if (materia == null || materia.getId_materia() == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("La materia o su ID no puede ser nula.");
        }

        Materia existente = materiasGateway.obtenerMateriaPorId(materia.getId_materia());
        if (existente == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró la materia a actualizar.");
        }

        return materiasGateway.actualizarMateria(materia);
    }

    @Override
    public boolean eliminarMateria(Integer idMateria) {
        if (idMateria == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un ID válido.");
        }

        Materia existente = materiasGateway.obtenerMateriaPorId(idMateria);
        if (existente == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró la materia a eliminar.");
        }

        return materiasGateway.eliminarMateria(idMateria);
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
    public Materia obtenerMateriaPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un código válido.");
        }

        Materia materia = materiasGateway.obtenerMateriaPorCodigo(codigo);
        if (materia == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Materia no encontrada con ese código.");
        }

        return materia;
    }

    @Override
    public boolean existeMateriaPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un código válido.");
        }

        return materiasGateway.existeMateriaPorCodigo(codigo);
    }

    @Override
    public List<Materia> buscarPorNombreParcial(String nombreParcial) {
        if (nombreParcial == null || nombreParcial.isBlank()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un nombre parcial.");
        }

        return materiasGateway.buscarPorNombreParcial(nombreParcial);
    }

    @Override
    public List<Materia> buscarPorCreditos(Integer creditos) {
        if (creditos == null || creditos <= 0) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Los créditos deben ser mayores a cero.");
        }

        return materiasGateway.buscarPorCreditos(creditos);
    }

    @Override
    public List<Materia> listarMaterias() {
        return materiasGateway.listarMaterias();
    }
}
