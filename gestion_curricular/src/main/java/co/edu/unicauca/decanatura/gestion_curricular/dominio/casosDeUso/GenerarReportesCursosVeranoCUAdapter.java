package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.Date;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.*;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.*;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.PeriodoAcademicoEnum;

/** Consulta los datos; los adaptadores de salida solo los mapean y formatean. */
public class GenerarReportesCursosVeranoCUAdapter implements GenerarReportesCursosVeranoCUIntPort {
    private final GestionarCursoOfertadoVeranoCUIntPort cursos;
    private final GestionarSolicitudCursoVeranoCUIntPort solicitudes;
    private final GenerarPdfCursosVeranoIntPort pdf;
    private final GenerarExcelSolicitudesVeranoIntPort excel;
    public GenerarReportesCursosVeranoCUAdapter(GestionarCursoOfertadoVeranoCUIntPort cursos,
            GestionarSolicitudCursoVeranoCUIntPort solicitudes, GenerarPdfCursosVeranoIntPort pdf,
            GenerarExcelSolicitudesVeranoIntPort excel) {
        this.cursos = cursos; this.solicitudes = solicitudes; this.pdf = pdf; this.excel = excel;
    }
    @Override public byte[] generarPdfCursos(String periodo, Integer programa) {
        return pdf.generarPDFCursos(cursos.listarPorFiltros(periodoFiltro(periodo), programa, false), periodo, programa);
    }
    @Override public DocumentoEstudiantes generarPdfEstudiantes(Integer idCurso) {
        var curso = cursos.obtenerCursoPorId(idCurso);
        if (curso == null) return null;
        var listado = solicitudes.listarEstudiantesPorCurso(idCurso);
        String codigo = curso.getObjMateria() == null ? "N/A" : curso.getObjMateria().getCodigo();
        return new DocumentoEstudiantes(codigo, pdf.generarPDFEstudiantesCurso(curso, listado));
    }
    @Override public byte[] generarExcelSolicitudes(Integer curso, String estado, String periodo, Date inicio, Date fin) {
        var datos = solicitudes.listarSolicitudesParaReporte(curso, estado, periodoFiltro(periodo), inicio, fin);
        return excel.generarExcelSolicitudes(datos, curso, estado, periodo, inicio, fin);
    }
    private static String periodoFiltro(String periodoAcademico) {
        String periodo = periodoAcademico == null ? "" : periodoAcademico.trim();
        return periodo.isEmpty() || "todos".equalsIgnoreCase(periodo) ? null : PeriodoAcademicoEnum.normalizar(periodo);
    }
}
