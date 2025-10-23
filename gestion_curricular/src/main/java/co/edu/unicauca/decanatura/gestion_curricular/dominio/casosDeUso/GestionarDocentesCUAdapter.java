package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocentesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocenteGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;

public class GestionarDocentesCUAdapter implements GestionarDocentesCUIntPort {

    private final GestionarDocenteGatewayIntPort docentesGateway;
    private final FormateadorResultadosIntPort formateadorResultados;

    public GestionarDocentesCUAdapter(GestionarDocenteGatewayIntPort docentesGateway,
                                      FormateadorResultadosIntPort formateadorResultados) {
        this.docentesGateway = docentesGateway;
        this.formateadorResultados = formateadorResultados;
    }

    @Override
    public Docente guardarDocente(Docente docente) {
        if (docente == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("El docente no puede ser nulo.");
        }

        docente.setId_docente(null);

        if (docentesGateway.existeDocentePorCodigo(docente.getCodigo_docente())) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Ya existe un docente con este código.");
        }

        return docentesGateway.guardarDocente(docente);
    }

    @Override
    public Docente actualizarDocente(Docente docente) {
        if (docente == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("El docente no puede ser nulo.");
        }
        if (docente.getId_docente() == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un ID válido para actualizar.");
        }
        Docente existente = docentesGateway.obtenerDocentePorId(docente.getId_docente());
        if (existente == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró el docente a actualizar.");
        }
        return docentesGateway.actualizarDocente(docente);
    }

    @Override
    public boolean eliminarDocente(Integer idDocente) {
        if (idDocente == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un ID válido.");
        }

        Docente existente = docentesGateway.obtenerDocentePorId(idDocente);
        if (existente == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró el docente a eliminar.");
        }

        return docentesGateway.eliminarDocente(idDocente);
    }

    @Override
    public Docente obtenerDocentePorId(Integer idDocente) {
        if (idDocente == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un ID válido.");
        }

        Docente docente = docentesGateway.obtenerDocentePorId(idDocente);
        if (docente == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Docente no encontrado.");
        }

        return docente;
    }

    @Override
    public Docente obtenerDocentePorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un código válido.");
        }

        Docente docente = docentesGateway.obtenerDocentePorCodigo(codigo);
        if (docente == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Docente no encontrado con ese código.");
        }

        return docente;
    }

    @Override
    public boolean existeDocentePorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un código válido.");
        }

        return docentesGateway.existeDocentePorCodigo(codigo);
    }

    @Override
    public List<Docente> buscarPorNombreParcial(String nombreParcial) {
        if (nombreParcial == null || nombreParcial.isBlank()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un nombre parcial.");
        }

        return docentesGateway.buscarPorNombreParcial(nombreParcial);
    }

    @Override
    public List<Docente> listarDocentes() {
        return docentesGateway.listarDocentes();
    }
}

