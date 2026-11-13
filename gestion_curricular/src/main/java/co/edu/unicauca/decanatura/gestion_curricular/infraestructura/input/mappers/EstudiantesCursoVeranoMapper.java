package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.ListadoEstudiantes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstudianteCurso;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.EstudiantesCursoVeranoDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.EstudiantesCursoVeranoDTORespuesta.*;

/** Mapeo compartido por la respuesta HTTP y el formateador PDF. */
public final class EstudiantesCursoVeranoMapper {
    private EstudiantesCursoVeranoMapper() {}
    public static EstudiantesCursoVeranoDTORespuesta mapear(CursoOfertadoVerano curso, ListadoEstudiantes listado) {
        var materia = curso.getObjMateria();
        return new EstudiantesCursoVeranoDTORespuesta(curso.getId_curso(), materia == null ? "N/A" : materia.getNombre(),
                materia == null ? "N/A" : materia.getCodigo(), curso.getCupo_estimado(), listado.estudiantes().size(),
                listado.totalPreinscritos(), listado.totalInscritos(),
                listado.estudiantes().stream().map(EstudiantesCursoVeranoMapper::mapearEstudiante).toList());
    }
    private static Estudiante mapearEstudiante(EstudianteCurso estudiante) {
        var usuario = estudiante.usuario();
        var preinscripcion = estudiante.preinscripcion();
        var inscripcion = estudiante.inscripcion();
        var programa = usuario.getObjPrograma();
        var condicion = preinscripcion != null ? preinscripcion.getCodicion_solicitud() : inscripcion.getCodicion_solicitud();

        return new Estudiante(usuario.getId_usuario(), usuario.getNombre_completo(), usuario.getCodigo(), usuario.getCorreo(),
                estudiante.estadoPreinscripcion(), estudiante.estadoInscripcion(),
                inscripcion == null ? null : inscripcion.getId_solicitud(), estudiante.inscrito() ? "Inscrito" : "Preinscrito",
                condicion == null ? null : condicion.toString(),
                programa == null ? null : new Programa(programa.getId_programa(), programa.getNombre_programa()),
                preinscripcion == null ? null : new Preinscripcion(preinscripcion.getFecha_registro_solicitud(),
                        preinscripcion.getId_solicitud(), inscripcion != null),
                preinscripcion == null ? new InscripcionSinPreinscripcion(inscripcion.getFecha_registro_solicitud(), false) : null);
    }
}
