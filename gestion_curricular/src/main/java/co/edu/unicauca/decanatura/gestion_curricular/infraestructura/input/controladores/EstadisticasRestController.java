package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadosSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.servicios.estadisticas.TipoProcesoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Estadistica;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.EstadisticaDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.EstadisticaMapperDominio;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.formateador.GeneradorReportesEstadisticas;
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

import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
@Validated
@Slf4j
public class EstadisticasRestController {

    private final GestionarEstadisticasCUIntPort estadisticaCU;
    private final EstadisticaMapperDominio mapper;
    private final GeneradorReportesEstadisticas generadorReportes;

    @PostMapping("/crear")
    public ResponseEntity<EstadisticaDTORespuesta> crearEstadistica(@RequestBody @Valid Estadistica estadistica) {
        Estadistica creada = estadisticaCU.crearEstadistica(estadistica);
        return new ResponseEntity<>(
                mapper.mappearDeEstadisticaAEstadisticaDTORespuesta(creada),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/actualizar")
    public ResponseEntity<EstadisticaDTORespuesta> actualizarEstadistica(@RequestBody @Valid Estadistica estadistica) {
        Estadistica actualizada = estadisticaCU.actualizarEstadistica(estadistica);
        return new ResponseEntity<>(
                mapper.mappearDeEstadisticaAEstadisticaDTORespuesta(actualizada),
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
                mapper.mappearDeEstadisticaAEstadisticaDTORespuesta(estadistica),
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
                mapper.mappearDeEstadisticaAEstadisticaDTORespuesta(resultado),
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
                .map(mapper::mappearDeEstadisticaAEstadisticaDTORespuesta)
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
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasGlobales(
            @RequestParam(required = false) String proceso,
            @RequestParam(required = false) Integer idPrograma,
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin) {
        // Si hay período académico, usar resumen completo que soporta filtros por período
        if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
            Map<String, Object> resumen = estadisticaCU.obtenerResumenCompleto(periodoAcademico, idPrograma);
            @SuppressWarnings("unchecked")
            Map<String, Object> estadisticasGlobales = (Map<String, Object>) resumen.get("estadisticasGlobales");
            
            if (estadisticasGlobales != null && proceso != null && !proceso.trim().isEmpty()) {
                aplicarKpisDelProceso(estadisticasGlobales, resumen, TipoProcesoSolicitud.delFiltro(proceso));
            }

            return ResponseEntity.ok(estadisticasGlobales != null ? estadisticasGlobales : new HashMap<>());
        }
        
        // Si no hay período académico, usar el método original
        Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasGlobales(proceso, idPrograma, fechaInicio, fechaFin);
        return ResponseEntity.ok(estadisticas);
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
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasFiltradas(
            @RequestParam(name = "nombreProceso", required = false) String nombreProceso,
            @RequestParam(name = "idPrograma", required = false) Integer idPrograma,
            @RequestParam(name = "estado", required = false) String estado,
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin) {
        
        
        Map<String, Object> estadisticas;
        
        // Determinar qué tipo de consulta realizar basado en los parámetros
        if (nombreProceso != null && !nombreProceso.trim().isEmpty()) {
            // Consulta por proceso específico
            estadisticas = estadisticaCU.obtenerEstadisticasPorProceso(nombreProceso);
        } else if (idPrograma != null && idPrograma > 0) {
            // Consulta por programa específico
            estadisticas = estadisticaCU.obtenerEstadisticasPorPrograma(idPrograma);
        } else if (estado != null && !estado.trim().isEmpty()) {
            // Consulta por estado específico
            estadisticas = estadisticaCU.obtenerEstadisticasPorEstado(estado);
        } else if (fechaInicio != null && fechaFin != null) {
            // Consulta por período específico
            estadisticas = estadisticaCU.obtenerEstadisticasPorPeriodo(fechaInicio, fechaFin);
        } else {
            // Consulta global (sin filtros)
            estadisticas = estadisticaCU.obtenerEstadisticasGlobales();
        }
        
        // Asegurar que la estructura JSON tenga el formato solicitado
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("fechaConsulta", estadisticas.get("fechaConsulta"));
        respuesta.put("totalSolicitudes", estadisticas.get("totalSolicitudes"));
        respuesta.put("totalAprobadas", estadisticas.get("totalAprobadas"));
        respuesta.put("totalRechazadas", estadisticas.get("totalRechazadas"));
        respuesta.put("totalEnProceso", estadisticas.get("totalEnProceso"));
        respuesta.put("porcentajeAprobacion", estadisticas.get("porcentajeAprobacion"));
        respuesta.put("porTipoProceso", estadisticas.get("porTipoProceso"));
        respuesta.put("porPrograma", estadisticas.get("porPrograma"));
        respuesta.put("porEstado", estadisticas.get("porEstado"));
        
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtiene estadísticas específicas por tipo de proceso.
     * 
     * @param tipoProceso Tipo de proceso (REINGRESO, HOMOLOGACION, ECAES, CURSO_VERANO, PAZ_SALVO)
     * @return ResponseEntity con estadísticas del proceso específico
     */
    @GetMapping("/proceso/{tipoProceso}")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorProceso(
            @PathVariable String tipoProceso) {
        Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasPorProceso(tipoProceso);
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Obtiene estadísticas por estado de solicitud en todos los procesos.
     * 
     * @param estado Estado de la solicitud (EN_PROCESO, APROBADA, RECHAZADA, etc.)
     * @return ResponseEntity con estadísticas por estado
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorEstado(
            @PathVariable String estado) {
        Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasPorEstado(estado);
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Obtiene estadísticas por programa académico.
     * 
     * @param idPrograma ID del programa académico
     * @return ResponseEntity con estadísticas del programa
     */
    @GetMapping("/programa/{idPrograma}")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorPrograma(
            @Min(value = 1) @PathVariable Integer idPrograma) {
        Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasPorPrograma(idPrograma);
        return ResponseEntity.ok(estadisticas);
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
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorPeriodo(
            @RequestParam(name = "periodoAcademico", required = false) String periodoAcademico,
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin) {
        Map<String, Object> estadisticas;
        
        // Si se proporciona período académico, usarlo
        if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
            estadisticas = estadisticaCU.obtenerEstadisticasPorPeriodoAcademico(periodoAcademico.trim());
        } 
        // Si se proporcionan fechas, usarlas
        else if (fechaInicio != null && fechaFin != null) {
            estadisticas = estadisticaCU.obtenerEstadisticasPorPeriodo(fechaInicio, fechaFin);
        } 
        // Si no se proporciona nada, retornar error
        else {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Debe proporcionar 'periodoAcademico' (ej: '2024-2') o 'fechaInicio' y 'fechaFin'");
            return ResponseEntity.badRequest().body(error);
        }
        
        return ResponseEntity.ok(estadisticas);
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
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorProgramaYPeriodo(
            @Min(value = 1) @PathVariable Integer idPrograma,
            @RequestParam(name = "fechaInicio", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(name = "fechaFin", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin) {
        Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasPorProgramaYPeriodo(idPrograma, fechaInicio, fechaFin);
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Obtiene estadísticas detalladas por proceso y estado.
     * 
     * @param tipoProceso Tipo de proceso
     * @param estado Estado de la solicitud
     * @return ResponseEntity con estadísticas detalladas
     */
    @GetMapping("/proceso/{tipoProceso}/estado/{estado}")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorProcesoYEstado(
            @PathVariable String tipoProceso,
            @PathVariable String estado) {
        Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasPorProcesoYEstado(tipoProceso, estado);
        return ResponseEntity.ok(estadisticas);
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
    public ResponseEntity<Map<String, Object>> obtenerResumenCompleto(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma,
            @RequestParam(required = false) String proceso) {
        Map<String, Object> resumen = estadisticaCU.obtenerResumenCompleto(periodoAcademico, idPrograma);
        
        // Si hay filtro de proceso, actualizar KPIs con datos solo de ese proceso.
        // IMPORTANTE: usar resumen.porTipoProceso (Map con stats completas), NO estadisticasGlobales.porTipoProceso (solo conteos Integer)
        String procesoNormalizado = TipoProcesoSolicitud.delFiltro(proceso);
        if (procesoNormalizado != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> porTipoProcesoResumen = (Map<String, Object>) resumen.get("porTipoProceso");
            if (porTipoProcesoResumen != null && porTipoProcesoResumen.containsKey(procesoNormalizado)) {
                Object datosProceso = porTipoProcesoResumen.get(procesoNormalizado);
                if (datosProceso instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> statsProceso = (Map<String, Object>) datosProceso;
                    @SuppressWarnings("unchecked")
                    Map<String, Object> estadisticasGlobales = (Map<String, Object>) resumen.get("estadisticasGlobales");
                    if (estadisticasGlobales != null) {
                        estadisticasGlobales.put("totalSolicitudes", statsProceso.getOrDefault("totalSolicitudes", 0));
                        estadisticasGlobales.put("totalAprobadas", statsProceso.getOrDefault("totalAprobadas", 0));
                        estadisticasGlobales.put("totalRechazadas", statsProceso.getOrDefault("totalRechazadas", 0));
                        estadisticasGlobales.put("totalEnProceso", statsProceso.getOrDefault("totalEnProceso", 0));
                        Object porEstadoObj = statsProceso.get("porEstado");
                        int totalEnviadas = 0;
                        if (porEstadoObj instanceof Map) {
                            Object env = ((Map<?, ?>) porEstadoObj).get(EstadosSolicitud.ENVIADA.name());
                            totalEnviadas = env instanceof Number ? ((Number) env).intValue() : 0;
                        }
                        estadisticasGlobales.put("totalEnviadas", totalEnviadas);
                        estadisticasGlobales.put("porcentajeAprobacion", statsProceso.getOrDefault("porcentajeAprobacion", 0.0));
                        estadisticasGlobales.put("porEstado", statsProceso.getOrDefault("porEstado", new HashMap<>()));
                        estadisticasGlobales.put("porTipoProceso", Map.of(procesoNormalizado, datosProceso));
                    }
                }
            }
            if (porTipoProcesoResumen != null && porTipoProcesoResumen.containsKey(procesoNormalizado)) {
                resumen.put("porTipoProceso", Map.of(procesoNormalizado, porTipoProcesoResumen.get(procesoNormalizado)));
            }
            if (resumen.get("filtrosAplicados") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> filtrosAplicados = (Map<String, Object>) resumen.get("filtrosAplicados");
                filtrosAplicados.put("proceso", procesoNormalizado);
            } else {
                Map<String, Object> filtros = new HashMap<>();
                filtros.put("proceso", procesoNormalizado);
                resumen.put("filtrosAplicados", filtros);
            }
        }
        
        return ResponseEntity.ok(resumen);
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
    public ResponseEntity<Map<String, Object>> obtenerTendenciasPorPeriodo(
            @RequestParam(name = "fechaInicio1", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio1,
            @RequestParam(name = "fechaFin1", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin1,
            @RequestParam(name = "fechaInicio2", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio2,
            @RequestParam(name = "fechaFin2", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin2) {
        // Si no se proporcionan parámetros, devolver estadísticas globales como tendencias
        if (fechaInicio1 == null || fechaFin1 == null || fechaInicio2 == null || fechaFin2 == null) {
            Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasGlobales();
            return ResponseEntity.ok(estadisticas);
        }
        Map<String, Object> tendencias = estadisticaCU.obtenerTendenciasPorPeriodo(fechaInicio1, fechaFin1, fechaInicio2, fechaFin2);
        return ResponseEntity.ok(tendencias);
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
        String periodoNormalizado = PeriodoAcademicoEnum.normalizar(periodoAcademico);
        boolean hayProceso = proceso != null && !proceso.trim().isEmpty();

        Map<String, Object> estadisticasGlobales;
        Map<String, Object> resumen = null;
        if (periodoNormalizado != null || idPrograma != null) {
            resumen = estadisticaCU.obtenerResumenCompleto(periodoNormalizado, idPrograma);
            @SuppressWarnings("unchecked")
            Map<String, Object> globales = (Map<String, Object>) resumen.get("estadisticasGlobales");
            estadisticasGlobales = globales;
        } else {
            estadisticasGlobales = estadisticaCU.obtenerEstadisticasGlobales(proceso, idPrograma, null, null);
        }

        // Con filtro de proceso y de periodo/programa, los KPIs salen de las estadísticas de ese proceso
        if (hayProceso && estadisticasGlobales != null && resumen != null) {
            aplicarKpisDelProceso(estadisticasGlobales, resumen, TipoProcesoSolicitud.delFiltro(proceso));
        }

        Map<String, Object> dashboardEjecutivo = new HashMap<>();
        dashboardEjecutivo.put("resumenGlobal", estadisticasGlobales);
        dashboardEjecutivo.put("topProcesos", estadisticasGlobales != null ? estadisticasGlobales.get("porTipoProceso") : new HashMap<>());
        dashboardEjecutivo.put("resumenEstados", estadisticasGlobales != null ? estadisticasGlobales.get("porEstado") : new HashMap<>());
        dashboardEjecutivo.put("fechaGeneracion", new Date());

        if (periodoNormalizado != null || idPrograma != null || hayProceso) {
            Map<String, Object> filtrosAplicados = new HashMap<>();
            if (periodoNormalizado != null) {
                filtrosAplicados.put("periodoAcademico", periodoNormalizado);
            }
            if (idPrograma != null) {
                filtrosAplicados.put("idPrograma", idPrograma);
            }
            if (hayProceso) {
                filtrosAplicados.put("proceso", proceso);
            }
            dashboardEjecutivo.put("filtrosAplicados", filtrosAplicados);
        }

        return ResponseEntity.ok(dashboardEjecutivo);
    }

    /** Reemplaza los KPIs globales por los del proceso indicado, tomados de resumen.porTipoProceso. */
    private void aplicarKpisDelProceso(Map<String, Object> estadisticasGlobales, Map<String, Object> resumen, String procesoNorm) {
        if (procesoNorm == null) {
            return;
        }
        Object porTipoProceso = resumen.get("porTipoProceso");
        if (!(porTipoProceso instanceof Map<?, ?> porTipo) || !(porTipo.get(procesoNorm) instanceof Map<?, ?> datosProceso)) {
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> statsProceso = (Map<String, Object>) datosProceso;
        estadisticasGlobales.put("totalSolicitudes", statsProceso.getOrDefault("totalSolicitudes", 0));
        estadisticasGlobales.put("totalAprobadas", statsProceso.getOrDefault("totalAprobadas", 0));
        estadisticasGlobales.put("totalRechazadas", statsProceso.getOrDefault("totalRechazadas", 0));
        estadisticasGlobales.put("totalEnProceso", statsProceso.getOrDefault("totalEnProceso", 0));
        int totalEnviadas = 0;
        if (statsProceso.get("porEstado") instanceof Map<?, ?> porEstado && porEstado.get(EstadosSolicitud.ENVIADA.name()) instanceof Number env) {
            totalEnviadas = env.intValue();
        }
        estadisticasGlobales.put("totalEnviadas", totalEnviadas);
        estadisticasGlobales.put("porcentajeAprobacion", statsProceso.getOrDefault("porcentajeAprobacion", 0.0));
        estadisticasGlobales.put("porEstado", statsProceso.getOrDefault("porEstado", new HashMap<>()));
        estadisticasGlobales.put("porTipoProceso", Map.of(procesoNorm, statsProceso));
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
        byte[] pdf = generadorReportes.pdfGeneral(estadisticaCU.obtenerEstadisticasGlobales());
        return archivo(pdf, MediaType.APPLICATION_PDF, "estadisticas_generales_dashboard.pdf");
    }

    /** PDF del dashboard de cursos de verano (periodo "YYYY-P" y programa opcionales). */
    @GetMapping("/export/pdf/cursos-verano")
    public ResponseEntity<byte[]> exportarEstadisticasCursosVeranoPDF(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        Map<String, Object> datos = estadisticaCU.obtenerEstadisticasCursosVerano(periodoAcademico, idPrograma);
        byte[] pdf = generadorReportes.pdfCursosVerano(datos, periodoAcademico, idPrograma);
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
        var datos = estadisticaCU.prepararReporte(proceso, idPrograma,
                periodoAcademico, fechaInicio, fechaFin);
        byte[] pdf = generadorReportes.pdfCompleto(datos.estadisticas(), datos.cursosVerano(), periodoAcademico, idPrograma);
        return archivo(pdf, MediaType.APPLICATION_PDF, "estadisticas_generales.pdf");
    }

    /** Excel del dashboard general. */
    @GetMapping("/export/excel/general")
    public ResponseEntity<byte[]> exportarEstadisticasGeneralesExcel() {
        byte[] excel = generadorReportes.excel(estadisticaCU.obtenerEstadisticasGlobales(), null, null, null);
        return archivo(excel, EXCEL, "estadisticas_generales_dashboard.xlsx");
    }

    /** Excel del dashboard de cursos de verano (periodo "YYYY-P" y programa opcionales). */
    @GetMapping("/export/excel/cursos-verano")
    public ResponseEntity<byte[]> exportarEstadisticasCursosVeranoExcel(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        Map<String, Object> datos = estadisticaCU.obtenerEstadisticasCursosVerano(periodoAcademico, idPrograma);
        byte[] excel = generadorReportes.excel(null, datos, periodoAcademico, idPrograma);
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
        var datos = estadisticaCU.prepararReporte(proceso, idPrograma,
                periodoAcademico, fechaInicio, fechaFin);
        byte[] excel = generadorReportes.excel(datos.estadisticas(), datos.cursosVerano(), periodoAcademico, idPrograma);
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
        headers.setContentDispositionFormData("attachment", nombreArchivo);
        return new ResponseEntity<>(contenido, headers, HttpStatus.OK);
    }

    /**
     * Obtiene el número total de estudiantes registrados en el sistema.
     * 
     * @return ResponseEntity con el conteo total de estudiantes
     */
    @GetMapping("/total-estudiantes")
    public ResponseEntity<Map<String, Object>> obtenerNumeroTotalEstudiantes() {
        
        Map<String, Object> resultado = estadisticaCU.obtenerNumeroTotalEstudiantes();
        
        return ResponseEntity.ok(resultado);
    }

    /**
     * Obtiene la distribución de estudiantes por programa académico.
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return ResponseEntity con la distribución de estudiantes por programa
     */
    @GetMapping("/estudiantes-por-programa")
    public ResponseEntity<Map<String, Object>> obtenerEstudiantesPorPrograma(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        Map<String, Object> resultado = estadisticaCU.obtenerEstudiantesPorPrograma(periodoAcademico, idPrograma);
        return ResponseEntity.ok(resultado);
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
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasDetalladasPorProceso(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        Map<String, Object> resultado = estadisticaCU.obtenerEstadisticasDetalladasPorProceso(periodoAcademico, idPrograma);
        return ResponseEntity.ok(resultado);
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
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorEstado(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        Map<String, Object> resultado = estadisticaCU.obtenerEstadisticasPorEstado(periodoAcademico, idPrograma);
        return ResponseEntity.ok(resultado);
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
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorPeriodo(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        Map<String, Object> resultado = estadisticaCU.obtenerEstadisticasPorPeriodo(periodoAcademico, idPrograma);
        return ResponseEntity.ok(resultado);
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
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorPrograma(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        Map<String, Object> resultado = estadisticaCU.obtenerEstadisticasPorPrograma(periodoAcademico, idPrograma);
        return ResponseEntity.ok(resultado);
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
    public ResponseEntity<Map<String, Object>> obtenerTendenciasYComparativas(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        Map<String, Object> resultado = estadisticaCU.obtenerTendenciasYComparativas(periodoAcademico, idPrograma);
        return ResponseEntity.ok(resultado);
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
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasCursosVerano(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        
        Map<String, Object> resultado = estadisticaCU.obtenerEstadisticasCursosVerano(periodoAcademico, idPrograma);
        
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
    public ResponseEntity<Map<String, Object>> obtenerTendenciasTemporalesCursosVerano(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        
        // Obtener solo las tendencias temporales de manera optimizada
        Map<String, Object> tendencias = estadisticaCU.obtenerTendenciasTemporalesCursosVerano(periodoAcademico, idPrograma);
        
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
    public ResponseEntity<List<Map<String, Object>>> obtenerHistorialEstadisticas(
            @RequestParam(name = "anioInicio", required = false) Integer anioInicio,
            @RequestParam(name = "anioFin", required = false) Integer anioFin) {
        List<Map<String, Object>> historial = estadisticaCU.obtenerHistorialEstadisticas(anioInicio, anioFin);
        return ResponseEntity.ok(historial);
    }

}

