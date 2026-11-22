package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstudianteCurso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.ListadoEstudiantes;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.EstudiantesCursoVeranoDTORespuesta.*;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.EstudiantesCursoVeranoDTORespuesta;

/** Mapeo compartido por la respuesta HTTP y el formateador PDF. */
public final class EstudiantesCursoVeranoMapper {
    private EstudiantesCursoVeranoMapper() {}
    public static EstudiantesCursoVeranoDTORespuesta mapear(CursoOfertadoVerano curso, ListadoEstudiantes listado) {
        var materia = curso.getObjMateria();
        return new EstudiantesCursoVeranoDTORespuesta(curso.getIdCurso(), materia == null ? "N/A" : materia.getNombre(),
                materia == null ? "N/A" : materia.getCodigo(), curso.getCupoEstimado(), listado.estudiantes().size(),
                listado.totalPreinscritos(), listado.totalInscritos(),
                listado.estudiantes().stream().map(EstudiantesCursoVeranoMapper::mapearEstudiante).toList());
    }
    private static Estudiante mapearEstudiante(EstudianteCurso estudiante) {
        var usuario = estudiante.usuario();
        var preinscripcion = estudiante.preinscripcion();
        var inscripcion = estudiante.inscripcion();
        var programa = usuario.getObjPrograma();
        var condicion = preinscripcion != null ? preinscripcion.getCondicionSolicitud() : inscripcion.getCondicionSolicitud();

        return new Estudiante(usuario.getIdUsuario(), usuario.getNombreCompleto(), usuario.getCodigo(), usuario.getCorreo(),
                estudiante.estadoPreinscripcion(), estudiante.estadoInscripcion(),
                inscripcion == null ? null : inscripcion.getIdSolicitud(), estudiante.inscrito() ? "Inscrito" : "Preinscrito",
                condicion == null ? null : condicion.toString(),
                programa == null ? null : new Programa(programa.getIdPrograma(), programa.getNombrePrograma()),
                preinscripcion == null ? null : new Preinscripcion(preinscripcion.getFechaRegistroSolicitud(),
                        preinscripcion.getIdSolicitud(), inscripcion != null),
                preinscripcion == null ? new InscripcionSinPreinscripcion(inscripcion.getFechaRegistroSolicitud(), false) : null);
    }
}
