package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GenerarReportesEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Estadistica;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.AnalisisCursosVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EntradaHistorial;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPorEstado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPorProceso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstadisticasPorPrograma;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.EstudiantesPorPrograma;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.Globales;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.ProcesosPorProgramaYPeriodo;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.ProgramasPorProcesoYEstado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.ResumenCompleto;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.TendenciasCursosVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.TotalEstudiantes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaPorEstado;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaPorMes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaPorProceso;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaPorPrograma;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.estadisticas.VistaTendencias;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.RespuestasArchivo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.EstadisticaDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.EstadisticaDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.EstadisticasFiltradasDTORespuesta.ErrorPeriodo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.EstadisticasFiltradasDTORespuesta.Filtradas;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.EstadisticaMapperDominio;

@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
@Validated
public class EstadisticasRestController {

    private final GestionarEstadisticasCUIntPort estadisticaCU;
    private final EstadisticaMapperDominio mapper;
    private final GenerarReportesEstadisticasCUIntPort generadorReportes;

    @PostMapping("/crear")
    public ResponseEntity<EstadisticaDTORespuesta> crearEstadistica(@RequestBody @Valid EstadisticaDTOPeticion estadistica) {
        Estadistica creada = estadisticaCU.crearEstadistica(mapper.mapearDeEstadisticaDTOPeticionAEstadistica(estadistica));
        return new ResponseEntity<>(
                mapper.mapearDeEstadisticaAEstadisticaDTORespuesta(creada),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/actualizar")
    public ResponseEntity<EstadisticaDTORespuesta> actualizarEstadistica(@RequestBody @Valid EstadisticaDTOPeticion estadistica) {
        Estadistica actualizada = estadisticaCU.actualizarEstadistica(mapper.mapearDeEstadisticaDTOPeticionAEstadistica(estadistica));
        return new ResponseEntity<>(
                mapper.mapearDeEstadisticaAEstadisticaDTORespuesta(actualizada),
                HttpStatus.ACCEPTED
        );
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Boolean> eliminarEstadistica(@Min(value =  1) @PathVariable Integer id) {
        boolean eliminada = estadisticaCU.eliminarEstadistica(id);
        return new ResponseEntity<>(eliminada ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @GetMapping("/buscarPorId/{id}")
    public ResponseEntity<EstadisticaDTORespuesta> obtenerPorId(@Min(value = 1) @PathVariable Integer id) {
        Estadistica estadistica = estadisticaCU.obtenerEstadisticaPorId(id);
        if (estadistica == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(
                mapper.mapearDeEstadisticaAEstadisticaDTORespuesta(estadistica),
                HttpStatus.OK
        );
    }

    @GetMapping("/porSolicitudPeriodoEstadoPrograma")
    public ResponseEntity<EstadisticaDTORespuesta> obtenerPorSolicitudPeriodoEstadoYPrograma(
            @RequestParam(name = "idEstadistica", required = true) @Min(value =  1) Integer idEstadistica,
            @RequestParam(name = "proceso", required = true) String proceso,
            @RequestParam(name = "fechaInicio", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(name = "fechaFin", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin,
            @RequestParam(name = "estado", required = true) String estado,
            @RequestParam(name = "idPrograma", required = true) @Min(value =  1) Integer idPrograma) {

        Estadistica resultado = estadisticaCU.obtenerEstadisticasSolicitudPeriodoEstadoYPrograma(
                idEstadistica, proceso, fechaInicio, fechaFin, estado, idPrograma
        );

        return new ResponseEntity<>(
                mapper.mapearDeEstadisticaAEstadisticaDTORespuesta(resultado),
                HttpStatus.OK
        );
    }

    @GetMapping("/porPeriodoEstadoPrograma")
    public ResponseEntity<List<EstadisticaDTORespuesta>> obtenerPorPeriodoEstadoYPrograma(
            @RequestParam(name = "fechaInicio", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(name = "fechaFin", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin,
            @RequestParam(name = "idPrograma", required = true) @Min(value =  1) Integer idPrograma) {

        List<Estadistica> lista = estadisticaCU.obtenerEstadisticasPeriodoEstadoYPrograma(
                fechaInicio, fechaFin, idPrograma
        );

        List<EstadisticaDTORespuesta> respuesta = lista.stream()
                .map(mapper::mapearDeEstadisticaAEstadisticaDTORespuesta)
                .collect(Collectors.toList());

        return ResponseEntity.ok(respuesta);
    }

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
     * Obtiene estadísticas con filtros dinámicos.
     * 
     * @param nombreProceso Tipo de proceso (opcional)
     * @param idPrograma ID del programa (opcional)
     * @param estado Estado de la solicitud (opcional)
     * @param fechaInicio Fecha de inicio (opcional)
     * @param fechaFin Fecha de fin (opcional)
     * @return ResponseEntity con estadísticas filtradas
     */
    @GetMapping("/filtradas")
    public ResponseEntity<Filtradas> obtenerEstadisticasFiltradas(
            @RequestParam(name = "nombreProceso", required = false) String nombreProceso,
            @RequestParam(name = "idPrograma", required = false) Integer idPrograma,
            @RequestParam(name = "estado", required = false) String estado,
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin) {
        
        
        // Un solo filtro, en este orden de prioridad: proceso, programa, estado, periodo; sin ninguno, las globales
        Filtradas filtradas;
        if (nombreProceso != null && !nombreProceso.trim().isEmpty()) {
            filtradas = Filtradas.de(estadisticaCU.obtenerEstadisticasPorProceso(nombreProceso));
        } else if (idPrograma != null && idPrograma > 0) {
            filtradas = Filtradas.de(estadisticaCU.obtenerEstadisticasPorPrograma(idPrograma));
        } else if (estado != null && !estado.trim().isEmpty()) {
            filtradas = Filtradas.de(estadisticaCU.obtenerEstadisticasPorEstado(estado));
        } else if (fechaInicio != null && fechaFin != null) {
            filtradas = Filtradas.de(estadisticaCU.obtenerEstadisticasPorPeriodo(fechaInicio, fechaFin));
        } else {
            filtradas = Filtradas.de(estadisticaCU.obtenerEstadisticasGlobales());
        }
        return ResponseEntity.ok(filtradas);
    }

    /**
     * Obtiene estadísticas específicas por tipo de proceso.
     * 
     * @param tipoProceso Tipo de proceso (REINGRESO, HOMOLOGACION, ECAES, CURSO_VERANO, PAZ_SALVO)
     * @return ResponseEntity con estadísticas del proceso específico
     */
    @GetMapping("/proceso/{tipoProceso}")
    public ResponseEntity<EstadisticasPorProceso> obtenerEstadisticasPorProceso(
            @PathVariable String tipoProceso) {
        return ResponseEntity.ok(estadisticaCU.obtenerEstadisticasPorProceso(tipoProceso));
    }

    /**
     * Obtiene estadísticas por estado de solicitud en todos los procesos.
     * 
     * @param estado Estado de la solicitud (EN_PROCESO, APROBADA, RECHAZADA, etc.)
     * @return ResponseEntity con estadísticas por estado
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<EstadisticasPorEstado> obtenerEstadisticasPorEstado(
            @PathVariable String estado) {
        return ResponseEntity.ok(estadisticaCU.obtenerEstadisticasPorEstado(estado));
    }

    /**
     * Obtiene estadísticas por programa académico.
     * 
     * @param idPrograma ID del programa académico
     * @return ResponseEntity con estadísticas del programa
     */
    @GetMapping("/programa/{idPrograma}")
    public ResponseEntity<EstadisticasPorPrograma> obtenerEstadisticasPorPrograma(
            @Min(value = 1) @PathVariable Integer idPrograma) {
        return ResponseEntity.ok(estadisticaCU.obtenerEstadisticasPorPrograma(idPrograma));
    }

    /**
     * Obtiene estadísticas por período académico.
     * Ahora acepta tanto fechas como formato de período académico (YYYY-P).
     * 
     * @param periodoAcademico Período en formato "YYYY-P" (opcional, ej: "2024-2")
     * @param fechaInicio Fecha de inicio del período (opcional si se proporciona periodoAcademico)
     * @param fechaFin Fecha de fin del período (opcional si se proporciona periodoAcademico)
     * @return ResponseEntity con estadísticas del período
     */
    @GetMapping("/periodo")
    public ResponseEntity<?> obtenerEstadisticasPorPeriodo(
            @RequestParam(name = "periodoAcademico", required = false) String periodoAcademico,
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin) {
        // Primero el período académico; si no viene, el par de fechas
        if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
            return ResponseEntity.ok(estadisticaCU.obtenerEstadisticasPorPeriodoAcademico(periodoAcademico.trim()));
        }
        if (fechaInicio != null && fechaFin != null) {
            return ResponseEntity.ok(estadisticaCU.obtenerEstadisticasPorPeriodo(fechaInicio, fechaFin));
        }
        return ResponseEntity.badRequest().body(new ErrorPeriodo(
                "Debe proporcionar 'periodoAcademico' (ej: '2024-2') o 'fechaInicio' y 'fechaFin'"));
    }

    /**
     * Obtiene estadísticas combinadas por programa y período académico.
     * 
     * @param idPrograma ID del programa académico
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin Fecha de fin del período
     * @return ResponseEntity con estadísticas combinadas
     */
    @GetMapping("/programa/{idPrograma}/periodo")
    public ResponseEntity<ProcesosPorProgramaYPeriodo> obtenerEstadisticasPorProgramaYPeriodo(
            @Min(value = 1) @PathVariable Integer idPrograma,
            @RequestParam(name = "fechaInicio", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(name = "fechaFin", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin) {
        return ResponseEntity.ok(estadisticaCU.obtenerEstadisticasPorProgramaYPeriodo(idPrograma, fechaInicio, fechaFin));
    }

    /**
     * Obtiene estadísticas detalladas por proceso y estado.
     * 
     * @param tipoProceso Tipo de proceso
     * @param estado Estado de la solicitud
     * @return ResponseEntity con estadísticas detalladas
     */
    @GetMapping("/proceso/{tipoProceso}/estado/{estado}")
    public ResponseEntity<ProgramasPorProcesoYEstado> obtenerEstadisticasPorProcesoYEstado(
            @PathVariable String tipoProceso,
            @PathVariable String estado) {
        return ResponseEntity.ok(estadisticaCU.obtenerEstadisticasPorProcesoYEstado(tipoProceso, estado));
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
     * Obtiene estadísticas de tendencias por período (comparación entre períodos).
     * 
     * @param fechaInicio1 Fecha de inicio del primer período
     * @param fechaFin1 Fecha de fin del primer período
     * @param fechaInicio2 Fecha de inicio del segundo período
     * @param fechaFin2 Fecha de fin del segundo período
     * @return ResponseEntity con comparación de tendencias
     */
    @GetMapping("/tendencias")
    public ResponseEntity<?> obtenerTendenciasPorPeriodo(
            @RequestParam(name = "fechaInicio1", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio1,
            @RequestParam(name = "fechaFin1", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin1,
            @RequestParam(name = "fechaInicio2", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio2,
            @RequestParam(name = "fechaFin2", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin2) {
        // Si no se proporcionan parámetros, devolver estadísticas globales como tendencias
        if (fechaInicio1 == null || fechaFin1 == null || fechaInicio2 == null || fechaFin2 == null) {
            return ResponseEntity.ok(estadisticaCU.obtenerEstadisticasGlobales());
        }
        return ResponseEntity.ok(
                estadisticaCU.obtenerTendenciasPorPeriodo(fechaInicio1, fechaFin1, fechaInicio2, fechaFin2));
    }

    /**
     * Obtiene un dashboard ejecutivo con las métricas más importantes.
     * Endpoint simplificado para interfaces de alto nivel.
     * 
     * @return ResponseEntity con dashboard ejecutivo
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> obtenerDashboardEjecutivo(
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
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasRendimiento() {
        return ResponseEntity.ok(estadisticaCU.obtenerEstadisticasRendimiento());
    }

    // ===== Exportación a PDF y Excel (los documentos los arma GeneradorReportesEstadisticas) =====

    private static final MediaType EXCEL =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    /** PDF del dashboard general. */
    @GetMapping("/export/pdf/general")
    public ResponseEntity<byte[]> exportarEstadisticasGeneralesPDF() {
        byte[] pdf = generadorReportes.pdfGeneral();
        return archivo(pdf, MediaType.APPLICATION_PDF, "estadisticas_generales_dashboard.pdf");
    }

    /** PDF del dashboard de cursos de verano (periodo "YYYY-P" y programa opcionales). */
    @GetMapping("/export/pdf/cursos-verano")
    public ResponseEntity<byte[]> exportarEstadisticasCursosVeranoPDF(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        byte[] pdf = generadorReportes.pdfCursosVerano(periodoAcademico, idPrograma);
        return archivo(pdf, MediaType.APPLICATION_PDF, nombreArchivoCursosVerano(periodoAcademico, idPrograma) + ".pdf");
    }

    /** PDF completo con filtros opcionales. */
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportarEstadisticasPDF(
            @RequestParam(required = false) String proceso,
            @RequestParam(required = false) Integer idPrograma,
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin) {
        byte[] pdf = generadorReportes.pdfCompleto(proceso, idPrograma, periodoAcademico, fechaInicio, fechaFin);
        return archivo(pdf, MediaType.APPLICATION_PDF, "estadisticas_generales.pdf");
    }

    /** Excel del dashboard general. */
    @GetMapping("/export/excel/general")
    public ResponseEntity<byte[]> exportarEstadisticasGeneralesExcel() {
        byte[] excel = generadorReportes.excelGeneral();
        return archivo(excel, EXCEL, "estadisticas_generales_dashboard.xlsx");
    }

    /** Excel del dashboard de cursos de verano (periodo "YYYY-P" y programa opcionales). */
    @GetMapping("/export/excel/cursos-verano")
    public ResponseEntity<byte[]> exportarEstadisticasCursosVeranoExcel(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        byte[] excel = generadorReportes.excelCursosVerano(periodoAcademico, idPrograma);
        return archivo(excel, EXCEL, nombreArchivoCursosVerano(periodoAcademico, idPrograma) + ".xlsx");
    }

    /** Excel completo con filtros opcionales. */
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportarEstadisticasExcel(
            @RequestParam(required = false) String proceso,
            @RequestParam(required = false) Integer idPrograma,
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin) {
        byte[] excel = generadorReportes.excelCompleto(proceso, idPrograma, periodoAcademico, fechaInicio, fechaFin);
        return archivo(excel, EXCEL, "estadisticas_generales.xlsx");
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

