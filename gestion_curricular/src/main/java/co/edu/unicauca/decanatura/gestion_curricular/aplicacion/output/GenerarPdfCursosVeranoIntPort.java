package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;
import java.util.List;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.*;
public interface GenerarPdfCursosVeranoIntPort {
    byte[] generarPDFCursos(List<CursoOfertadoVerano> cursos, String periodo, Integer programa);
    byte[] generarPDFEstudiantesCurso(CursoOfertadoVerano curso, ListadoEstudiantes estudiantes);
}
