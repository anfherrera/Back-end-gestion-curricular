package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;
import java.util.Date;
public interface GenerarReportesCursosVeranoCUIntPort {
    byte[] generarPdfCursos(String periodo, Integer programa);
    record DocumentoEstudiantes(String codigoCurso, byte[] contenido) {}
    DocumentoEstudiantes generarPdfEstudiantes(Integer curso);
    byte[] generarExcelSolicitudes(Integer curso, String estado, String periodo, Date inicio, Date fin);
}
