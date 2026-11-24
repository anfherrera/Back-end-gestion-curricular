package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.util.Date;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GenerarReportesEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.AnalisisCursosVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.DashboardEjecutivo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EntradaHistorial;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstudiantesPorPrograma;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.Globales;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.ResumenCompleto;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.Rendimiento;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.TendenciasCursosVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.TotalEstudiantes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaPorEstado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaPorMes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaPorProceso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaPorPrograma;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaTendencias;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.RespuestasArchivo;

/**
 * Consultas y reportes del módulo estadístico (EST-7, paso 9). Todas son {@code GET}: {@code SeguridadConfig} las
 * reserva a los roles administrativos. Los reportes se piden con {@code ?formato=pdf|excel} en
 * {@code GET /reportes} (el completo) y {@code GET /cursos-verano/reportes}.
 */
@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
public class EstadisticasRestController {

    private final GestionarEstadisticasCUIntPort estadisticaCU;
    private final GenerarReportesEstadisticasCUIntPort generadorReportes;

    /**
     * Obtiene estadísticas globales del sistema combinando todos los procesos.
     * Acepta MÚLTIPLES FILTROS SIMULTÁNEOS (con AND).
     * 
     * @param proceso Tipo de proceso (opcional) - ej: "Reingreso", "Paz y Salvo", "Homologación"
     * @param idPrograma ID del programa (opcional)
     * @param fechaInicio Fecha de inicio (opcional) - formato: yyyy-MM-dd
     * @param fechaFin Fecha de fin (opcional) - formato: yyyy-MM-dd
     * @return ResponseEntity con estadísticas globales filtradas
     * 
     * Ejemplos de uso:
     * - Sin filtros: GET /api/estadisticas/globales
     * - Por proceso: GET /api/estadisticas/globales?proceso=Reingreso
     * - Por programa: GET /api/estadisticas/globales?idPrograma=1
     * - Por fechas: GET /api/estadisticas/globales?fechaInicio=2025-07-01&fechaFin=2025-09-30
     * - Combinados: GET /api/estadisticas/globales?proceso=Paz%20y%20Salvo&idPrograma=1&fechaInicio=2025-07-01&fechaFin=2025-09-30
     */
    @GetMapping("/globales")
    public ResponseEntity<Globales> obtenerEstadisticasGlobales(
            @RequestParam(required = false) String proceso,
            @RequestParam(required = false) Integer idPrograma,
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin) {
        return ResponseEntity.ok(
                estadisticaCU.obtenerEstadisticasGlobales(proceso, idPrograma, periodoAcademico, fechaInicio, fechaFin));
    }

    /**
     * Obtiene un resumen completo de todas las estadísticas del sistema.
     * Utiliza múltiples repositorios para generar un dashboard completo.
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return ResponseEntity con resumen completo de estadísticas
     */
    @GetMapping("/resumen-completo")
    public ResponseEntity<ResumenCompleto> obtenerResumenCompleto(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma,
            @RequestParam(required = false) String proceso) {
        return ResponseEntity.ok(estadisticaCU.obtenerResumenCompleto(periodoAcademico, idPrograma, proceso));
    }

    /**
     * Obtiene un dashboard ejecutivo con las métricas más importantes.
     * Endpoint simplificado para interfaces de alto nivel.
     * 
     * @return ResponseEntity con dashboard ejecutivo
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardEjecutivo> obtenerDashboardEjecutivo(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma,
            @RequestParam(required = false) String proceso) {
        return ResponseEntity.ok(estadisticaCU.obtenerDashboardEjecutivo(periodoAcademico, idPrograma, proceso));
    }

    /**
     * Obtiene estadísticas de rendimiento del sistema.
     * Métricas específicas de rendimiento y eficiencia.
     * 
     * @return ResponseEntity con estadísticas de rendimiento
     */
    @GetMapping("/rendimiento")
    public ResponseEntity<Rendimiento> obtenerEstadisticasRendimiento() {
        return ResponseEntity.ok(estadisticaCU.obtenerEstadisticasRendimiento());
    }

    // ===== Reportes en PDF y Excel (los documentos los arma GeneradorReportesEstadisticas) =====

    private static final MediaType EXCEL =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    /**
     * GET /api/estadisticas/reportes?formato=pdf|excel (antes /export/pdf y /export/excel): el reporte completo, con
     * filtros opcionales. Sin {@code formato} responde 400 (GC-0400); con otro valor, también 400.
     */
    @GetMapping("/reportes")
    public ResponseEntity<byte[]> obtenerReporte(
            @RequestParam String formato,
            @RequestParam(required = false) String proceso,
            @RequestParam(required = false) Integer idPrograma,
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin) {
        if (esPdf(formato)) {
            byte[] pdf = generadorReportes.pdfCompleto(proceso, idPrograma, periodoAcademico, fechaInicio, fechaFin);
            return archivo(pdf, MediaType.APPLICATION_PDF, "estadisticas_generales.pdf");
        }
        byte[] excel = generadorReportes.excelCompleto(proceso, idPrograma, periodoAcademico, fechaInicio, fechaFin);
        return archivo(excel, EXCEL, "estadisticas_generales.xlsx");
    }

    /**
     * GET /api/estadisticas/cursos-verano/reportes?formato=pdf|excel (antes /export/pdf/cursos-verano y
     * /export/excel/cursos-verano): el dashboard de cursos de verano, con periodo "YYYY-P" y programa opcionales.
     */
    @GetMapping("/cursos-verano/reportes")
    public ResponseEntity<byte[]> obtenerReporteCursosVerano(
            @RequestParam String formato,
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        String nombre = nombreArchivoCursosVerano(periodoAcademico, idPrograma);
        if (esPdf(formato)) {
            byte[] pdf = generadorReportes.pdfCursosVerano(periodoAcademico, idPrograma);
            return archivo(pdf, MediaType.APPLICATION_PDF, nombre + ".pdf");
        }
        byte[] excel = generadorReportes.excelCursosVerano(periodoAcademico, idPrograma);
        return archivo(excel, EXCEL, nombre + ".xlsx");
    }

    /** true para {@code pdf}, false para {@code excel}; cualquier otro valor es un 400. */
    private static boolean esPdf(String formato) {
        return switch (formato) {
            case "pdf" -> true;
            case "excel" -> false;
            default -> throw new IllegalArgumentException("Formato no válido: use pdf o excel");
        };
    }

    private static String nombreArchivoCursosVerano(String periodoAcademico, Integer idPrograma) {
        String nombre = "estadisticas_cursos_verano";
        if (periodoAcademico != null) {
            nombre += "_" + periodoAcademico.replace("-", "_");
        }
        if (idPrograma != null) {
            nombre += "_programa_" + idPrograma;
        }
        return nombre;
    }

    private static ResponseEntity<byte[]> archivo(byte[] contenido, MediaType tipo, String nombreArchivo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(tipo);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, RespuestasArchivo.adjuntoEscapado(nombreArchivo));
        return new ResponseEntity<>(contenido, headers, HttpStatus.OK);
    }

    /**
     * Obtiene el número total de estudiantes registrados en el sistema.
     * 
     * @return ResponseEntity con el conteo total de estudiantes
     */
    @GetMapping("/total-estudiantes")
    public ResponseEntity<TotalEstudiantes> obtenerNumeroTotalEstudiantes() {
        return ResponseEntity.ok(estadisticaCU.obtenerNumeroTotalEstudiantes());
    }

    /**
     * Obtiene la distribución de estudiantes por programa académico.
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return ResponseEntity con la distribución de estudiantes por programa
     */
    @GetMapping("/estudiantes-por-programa")
    public ResponseEntity<EstudiantesPorPrograma> obtenerEstudiantesPorPrograma(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        return ResponseEntity.ok(estadisticaCU.obtenerEstudiantesPorPrograma(periodoAcademico, idPrograma));
    }

    /**
     * Obtiene estadísticas detalladas por tipo de proceso.
     * Incluye conteos, porcentajes y análisis por proceso.
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return ResponseEntity con estadísticas detalladas por proceso
     */
    @GetMapping("/estadisticas-por-proceso")
    public ResponseEntity<VistaPorProceso> obtenerEstadisticasDetalladasPorProceso(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        return ResponseEntity.ok(estadisticaCU.obtenerEstadisticasDetalladasPorProceso(periodoAcademico, idPrograma));
    }

    /**
     * Obtiene estadísticas por estado de solicitudes.
     * Incluye conteos, porcentajes y análisis por estado.
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return ResponseEntity con estadísticas por estado
     */
    @GetMapping("/estado-solicitudes")
    public ResponseEntity<VistaPorEstado> obtenerEstadisticasPorEstado(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        return ResponseEntity.ok(estadisticaCU.obtenerEstadisticasPorEstado(periodoAcademico, idPrograma));
    }

    /**
     * Obtiene estadísticas por período/mes.
     * Incluye tendencias, picos de actividad y análisis temporal.
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return ResponseEntity con estadísticas por período
     */
    @GetMapping("/por-periodo")
    public ResponseEntity<VistaPorMes> obtenerEstadisticasPorPeriodo(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        return ResponseEntity.ok(estadisticaCU.obtenerEstadisticasPorPeriodo(periodoAcademico, idPrograma));
    }

    /**
     * Obtiene estadísticas por programa académico.
     * Incluye distribución de solicitudes, estudiantes y análisis por programa.
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return ResponseEntity con estadísticas por programa
     */
    @GetMapping("/por-programa")
    public ResponseEntity<VistaPorPrograma> obtenerEstadisticasPorPrograma(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        return ResponseEntity.ok(estadisticaCU.obtenerEstadisticasPorPrograma(periodoAcademico, idPrograma));
    }

    /**
     * Obtiene tendencias y comparativas del sistema.
     * Incluye análisis de crecimiento, comparaciones entre períodos y tendencias estratégicas.
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return ResponseEntity con tendencias y comparativas
     */
    @GetMapping("/tendencias-comparativas")
    public ResponseEntity<VistaTendencias> obtenerTendenciasYComparativas(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        return ResponseEntity.ok(estadisticaCU.obtenerTendenciasYComparativas(periodoAcademico, idPrograma));
    }

    /**
     * Obtiene estadísticas específicas para cursos de verano.
     * Incluye análisis de demanda por materia, tendencias temporales y recomendaciones.
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return ResponseEntity con estadísticas detalladas de cursos de verano
     */
    @GetMapping("/cursos-verano")
    public ResponseEntity<AnalisisCursosVerano> obtenerEstadisticasCursosVerano(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        
        AnalisisCursosVerano resultado = estadisticaCU.obtenerEstadisticasCursosVerano(periodoAcademico, idPrograma);
        
        return ResponseEntity.ok(resultado);
    }

    /**
     * Obtiene las tendencias temporales de cursos de verano.
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return ResponseEntity con tendencias temporales
     */
    @GetMapping("/cursos-verano/tendencias-temporales")
    public ResponseEntity<TendenciasCursosVerano> obtenerTendenciasTemporalesCursosVerano(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        
        // Obtener solo las tendencias temporales de manera optimizada
        TendenciasCursosVerano tendencias = estadisticaCU.obtenerTendenciasTemporalesCursosVerano(periodoAcademico, idPrograma);
        
        return ResponseEntity.ok(tendencias);
    }

    /**
     * Obtiene historial de estadísticas por períodos académicos.
     * 
     * @param anioInicio Año de inicio (opcional, por defecto últimos 5 años)
     * @param anioFin Año de fin (opcional, por defecto año actual)
     * @return ResponseEntity con lista de estadísticas por período
     */
    @GetMapping("/historial")
    public ResponseEntity<List<EntradaHistorial>> obtenerHistorialEstadisticas(
            @RequestParam(name = "anioInicio", required = false) Integer anioInicio,
            @RequestParam(name = "anioFin", required = false) Integer anioFin) {
        return ResponseEntity.ok(estadisticaCU.obtenerHistorialEstadisticas(anioInicio, anioFin));
    }

}

