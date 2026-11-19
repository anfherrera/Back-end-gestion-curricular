package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarDocentesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarDocenteGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadYaExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.ReglaNegocioExcepcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;

public class GestionarDocentesCUAdapter implements GestionarDocentesCUIntPort {

    private final GestionarDocenteGatewayIntPort docentesGateway;

    public GestionarDocentesCUAdapter(GestionarDocenteGatewayIntPort docentesGateway) {
        this.docentesGateway = docentesGateway;
    }

    @Override
    public Docente guardarDocente(Docente docente) {
        if (docente == null) {
            throw new ReglaNegocioExcepcion("El docente no puede ser nulo.");
        }

        docente.setId_docente(null);

        if (docentesGateway.existeDocentePorCodigo(docente.getCodigo_docente())) {
            throw new EntidadYaExisteException("Ya existe un docente con este código.");
        }

        return docentesGateway.guardarDocente(docente);
    }

    @Override
    public Docente actualizarDocente(Docente docente) {
        if (docente == null) {
            throw new ReglaNegocioExcepcion("El docente no puede ser nulo.");
        }
        if (docente.getId_docente() == null) {
            throw new ReglaNegocioExcepcion("Debe proporcionar un ID válido para actualizar.");
        }
        Docente existente = docentesGateway.obtenerDocentePorId(docente.getId_docente());
        if (existente == null) {
            throw new EntidadNoExisteException("No se encontró el docente a actualizar.");
        }
        return docentesGateway.actualizarDocente(docente);
    }

    @Override
    public boolean eliminarDocente(Integer idDocente) {
        if (idDocente == null) {
            throw new ReglaNegocioExcepcion("Debe proporcionar un ID válido.");
        }

        Docente existente = docentesGateway.obtenerDocentePorId(idDocente);
        if (existente == null) {
            throw new EntidadNoExisteException("No se encontró el docente a eliminar.");
        }

        // Verificar si el docente tiene cursos asignados antes de eliminarlo
        if (docentesGateway.tieneCursosAsignados(idDocente)) {
            throw new ReglaNegocioExcepcion(
                "No se puede eliminar el docente porque tiene cursos asignados. " +
                "Por favor, reasigne los cursos a otro docente antes de eliminar."
            );
        }

        return docentesGateway.eliminarDocente(idDocente);
    }

    @Override
    public Docente obtenerDocentePorId(Integer idDocente) {
        if (idDocente == null) {
            throw new ReglaNegocioExcepcion("Debe proporcionar un ID válido.");
        }

        Docente docente = docentesGateway.obtenerDocentePorId(idDocente);
        if (docente == null) {
            throw new EntidadNoExisteException("Docente no encontrado.");
        }

        return docente;
    }

    @Override
    public Docente obtenerDocentePorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new ReglaNegocioExcepcion("Debe proporcionar un código válido.");
        }

        Docente docente = docentesGateway.obtenerDocentePorCodigo(codigo);
        if (docente == null) {
            throw new EntidadNoExisteException("Docente no encontrado con ese código.");
        }

        return docente;
    }

    @Override
    public List<Docente> buscarPorNombreParcial(String nombreParcial) {
        if (nombreParcial == null || nombreParcial.isBlank()) {
            throw new ReglaNegocioExcepcion("Debe proporcionar un nombre parcial.");
        }

        return docentesGateway.buscarPorNombreParcial(nombreParcial);
    }

    @Override
    public List<Docente> listarDocentes() {
        return docentesGateway.listarDocentes();
    }
}

