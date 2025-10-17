package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Estadistica;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.EstadisticaDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers.EstadisticaMapperDominio;
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

import java.io.ByteArrayOutputStream;
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

    @GetMapping("/porSolicitudPeriodoPrograma")
    public ResponseEntity<EstadisticaDTORespuesta> obtenerPorSolicitudPeriodoYPrograma(
            @RequestParam(name = "idEstadistica", required = true) @Min(value =  1) Integer idEstadistica,
            @RequestParam(name = "proceso", required = true) String proceso,
            @RequestParam(name = "fechaInicio", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(name = "fechaFin", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin,
            @RequestParam(name = "idPrograma", required = true) @Min(value =  1) Integer idPrograma) {

        Estadistica resultado = estadisticaCU.obtenerEstadisticasSolicitudPeriodoYPrograma(
                idEstadistica, proceso, fechaInicio, fechaFin, idPrograma
        );

        return new ResponseEntity<>(
                mapper.mappearDeEstadisticaAEstadisticaDTORespuesta(resultado),
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
     * Utiliza SolicitudRepositoryInt para obtener conteos totales.
     * 
     * @param proceso Tipo de proceso (opcional)
     * @param idPrograma ID del programa (opcional)
     * @param fechaInicio Fecha de inicio (opcional)
     * @param fechaFin Fecha de fin (opcional)
     * @return ResponseEntity con estadísticas globales
     */
    @GetMapping("/globales")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasGlobales(
            @RequestParam(required = false) String proceso,
            @RequestParam(required = false) Integer idPrograma,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin) {
        try {
            log.info("📊 [ESTADISTICAS] Generando estadísticas globales con filtros - Proceso: {}, Programa: {}, Fechas: {} - {}", 
                    proceso, idPrograma, fechaInicio, fechaFin);
            Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasGlobales(proceso, idPrograma, fechaInicio, fechaFin);
            log.info("📊 [ESTADISTICAS] Resultado final: {}", estadisticas);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            log.error("❌ [ESTADISTICAS] Error obteniendo estadísticas globales: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint mejorado para obtener estadísticas con filtros dinámicos.
     * Maneja parámetros opcionales y devuelve la estructura JSON solicitada.
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
        
        try {
            log.info("📊 [ESTADISTICAS] Obteniendo estadísticas filtradas");
            log.info("📊 [ESTADISTICAS] Parámetros recibidos - nombreProceso: {}, idPrograma: {}, estado: {}, fechaInicio: {}, fechaFin: {}", 
                    nombreProceso, idPrograma, estado, fechaInicio, fechaFin);
            
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
            
            log.info("📊 [ESTADISTICAS] Resultado final: {}", respuesta);
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            log.error("❌ [ESTADISTICAS] Error obteniendo estadísticas filtradas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene estadísticas específicas por tipo de proceso.
     * Utiliza SolicitudRepositoryInt para filtrar por tipo de solicitud.
     * 
     * @param tipoProceso Tipo de proceso (REINGRESO, HOMOLOGACION, ECAES, CURSO_VERANO, PAZ_SALVO)
     * @return ResponseEntity con estadísticas del proceso específico
     */
    @GetMapping("/proceso/{tipoProceso}")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorProceso(
            @PathVariable String tipoProceso) {
        try {
            Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasPorProceso(tipoProceso);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene estadísticas por estado de solicitud en todos los procesos.
     * Utiliza SolicitudRepositoryInt para contar por estado.
     * 
     * @param estado Estado de la solicitud (EN_PROCESO, APROBADA, RECHAZADA, etc.)
     * @return ResponseEntity con estadísticas por estado
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorEstado(
            @PathVariable String estado) {
        try {
            Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasPorEstado(estado);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene estadísticas por programa académico.
     * Utiliza SolicitudRepositoryInt y ProgramaRepositoryInt para filtrar por programa.
     * 
     * @param idPrograma ID del programa académico
     * @return ResponseEntity con estadísticas del programa
     */
    @GetMapping("/programa/{idPrograma}")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorPrograma(
            @Min(value = 1) @PathVariable Integer idPrograma) {
        try {
            Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasPorPrograma(idPrograma);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene estadísticas por período académico.
     * Utiliza SolicitudRepositoryInt para filtrar por rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin Fecha de fin del período
     * @return ResponseEntity con estadísticas del período
     */
    @GetMapping("/periodo")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorPeriodo(
            @RequestParam(name = "fechaInicio", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(name = "fechaFin", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin) {
        try {
            Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasPorPeriodo(fechaInicio, fechaFin);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene estadísticas combinadas por programa y período académico.
     * Utiliza SolicitudRepositoryInt para filtrar por programa y fechas.
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
        try {
            Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasPorProgramaYPeriodo(idPrograma, fechaInicio, fechaFin);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene estadísticas detalladas por proceso y estado.
     * Utiliza SolicitudRepositoryInt para filtrar por tipo de solicitud y estado.
     * 
     * @param tipoProceso Tipo de proceso
     * @param estado Estado de la solicitud
     * @return ResponseEntity con estadísticas detalladas
     */
    @GetMapping("/proceso/{tipoProceso}/estado/{estado}")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorProcesoYEstado(
            @PathVariable String tipoProceso,
            @PathVariable String estado) {
        try {
            Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasPorProcesoYEstado(tipoProceso, estado);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene un resumen completo de todas las estadísticas del sistema.
     * Utiliza múltiples repositorios para generar un dashboard completo.
     * 
     * @return ResponseEntity con resumen completo de estadísticas
     */
    @GetMapping("/resumen-completo")
    public ResponseEntity<Map<String, Object>> obtenerResumenCompleto() {
        try {
            Map<String, Object> resumen = estadisticaCU.obtenerResumenCompleto();
            return ResponseEntity.ok(resumen);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene estadísticas de tendencias por período (comparación entre períodos).
     * Utiliza SolicitudRepositoryInt para analizar tendencias temporales.
     * 
     * @param fechaInicio1 Fecha de inicio del primer período
     * @param fechaFin1 Fecha de fin del primer período
     * @param fechaInicio2 Fecha de inicio del segundo período
     * @param fechaFin2 Fecha de fin del segundo período
     * @return ResponseEntity con comparación de tendencias
     */
    @GetMapping("/tendencias")
    public ResponseEntity<Map<String, Object>> obtenerTendenciasPorPeriodo(
            @RequestParam(name = "fechaInicio1", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio1,
            @RequestParam(name = "fechaFin1", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin1,
            @RequestParam(name = "fechaInicio2", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio2,
            @RequestParam(name = "fechaFin2", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin2) {
        try {
            Map<String, Object> tendencias = estadisticaCU.obtenerTendenciasPorPeriodo(fechaInicio1, fechaFin1, fechaInicio2, fechaFin2);
            return ResponseEntity.ok(tendencias);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene un dashboard ejecutivo con las métricas más importantes.
     * Endpoint simplificado para interfaces de alto nivel.
     * 
     * @return ResponseEntity con dashboard ejecutivo
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> obtenerDashboardEjecutivo() {
        try {
            Map<String, Object> dashboard = estadisticaCU.obtenerResumenCompleto();
            
            // Extraer solo las métricas más importantes para el dashboard
            Map<String, Object> dashboardEjecutivo = Map.of(
                "resumenGlobal", dashboard.get("estadisticasGlobales"),
                "topProcesos", dashboard.get("porTipoProceso"),
                "resumenEstados", dashboard.get("porEstado"),
                "fechaGeneracion", dashboard.get("fechaGeneracion")
            );
            
            return ResponseEntity.ok(dashboardEjecutivo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene estadísticas de rendimiento del sistema.
     * Métricas específicas de rendimiento y eficiencia.
     * 
     * @return ResponseEntity con estadísticas de rendimiento
     */
    @GetMapping("/rendimiento")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasRendimiento() {
        try {
            // Combinar estadísticas globales con métricas de rendimiento
            Map<String, Object> estadisticasGlobales = estadisticaCU.obtenerEstadisticasGlobales();
            
            Map<String, Object> rendimiento = Map.of(
                "estadisticasGlobales", estadisticasGlobales,
                "indicadoresRendimiento", Map.of(
                    "tasaProcesamiento", calcularTasaProcesamiento(estadisticasGlobales),
                    "eficienciaAprobacion", calcularEficienciaAprobacion(estadisticasGlobales),
                    "tiempoPromedioProcesamiento", "N/A" // Placeholder para implementación futura
                ),
                "fechaConsulta", new Date()
            );
            
            return ResponseEntity.ok(rendimiento);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Método auxiliar para calcular tasa de procesamiento
     */
    private Double calcularTasaProcesamiento(Map<String, Object> estadisticas) {
        Integer totalSolicitudes = (Integer) estadisticas.get("totalSolicitudes");
        Integer totalAprobadas = (Integer) estadisticas.get("totalAprobadas");
        Integer totalRechazadas = (Integer) estadisticas.get("totalRechazadas");
        
        if (totalSolicitudes == null || totalSolicitudes == 0) {
            return 0.0;
        }
        
        Integer procesadas = (totalAprobadas != null ? totalAprobadas : 0) + 
                           (totalRechazadas != null ? totalRechazadas : 0);
        
        return (double) procesadas / totalSolicitudes * 100;
    }

    /**
     * Método auxiliar para calcular eficiencia de aprobación
     */
    private Double calcularEficienciaAprobacion(Map<String, Object> estadisticas) {
        Integer totalAprobadas = (Integer) estadisticas.get("totalAprobadas");
        Integer totalRechazadas = (Integer) estadisticas.get("totalRechazadas");
        
        if (totalAprobadas == null && totalRechazadas == null) {
            return 0.0;
        }
        
        Integer totalDecididas = (totalAprobadas != null ? totalAprobadas : 0) + 
                                (totalRechazadas != null ? totalRechazadas : 0);
        
        if (totalDecididas == 0) {
            return 0.0;
        }
        
        return (double) (totalAprobadas != null ? totalAprobadas : 0) / totalDecididas * 100;
    }

    /**
     * Endpoint específico para manejar filtros dinámicos del frontend.
     * Acepta parámetros como "{nombreProceso}" y los procesa dinámicamente.
     * 
     * @param filtros Map con todos los filtros posibles
     * @return ResponseEntity con estadísticas filtradas
     */
    @PostMapping("/filtros-dinamicos")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasConFiltrosDinamicos(
            @RequestBody(required = false) Map<String, Object> filtros) {
        
        try {
            log.info("📊 [ESTADISTICAS] Obteniendo estadísticas con filtros dinámicos");
            log.info("📊 [ESTADISTICAS] Filtros recibidos: {}", filtros);
            
            if (filtros == null || filtros.isEmpty()) {
                // Sin filtros, devolver estadísticas globales
                Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasGlobales();
                return ResponseEntity.ok(estadisticas);
            }
            
            // Extraer parámetros de los filtros
            String nombreProceso = (String) filtros.get("nombreProceso");
            Integer idPrograma = (Integer) filtros.get("idPrograma");
            String estado = (String) filtros.get("estado");
            String fechaInicioStr = (String) filtros.get("fechaInicio");
            String fechaFinStr = (String) filtros.get("fechaFin");
            
            log.info("📊 [ESTADISTICAS] Parámetros extraídos - nombreProceso: {}, idPrograma: {}, estado: {}, fechaInicio: {}, fechaFin: {}", 
                    nombreProceso, idPrograma, estado, fechaInicioStr, fechaFinStr);
            
            // Procesar fechas si están presentes
            Date fechaInicio = null;
            Date fechaFin = null;
            
            if (fechaInicioStr != null && !fechaInicioStr.trim().isEmpty()) {
                try {
                    fechaInicio = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(fechaInicioStr);
                } catch (Exception e) {
                    log.warn("⚠️ [ESTADISTICAS] Error parseando fechaInicio: {}", e.getMessage());
                }
            }
            
            if (fechaFinStr != null && !fechaFinStr.trim().isEmpty()) {
                try {
                    fechaFin = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(fechaFinStr);
                } catch (Exception e) {
                    log.warn("⚠️ [ESTADISTICAS] Error parseando fechaFin: {}", e.getMessage());
                }
            }
            
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
            
            log.info("📊 [ESTADISTICAS] Resultado final: {}", respuesta);
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            log.error("❌ [ESTADISTICAS] Error obteniendo estadísticas con filtros dinámicos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint para obtener consolidado general del sistema.
     * Devuelve estadísticas globales con información adicional de programas.
     * 
     * @return ResponseEntity con consolidado general
     */
    @GetMapping("/consolidado")
    public ResponseEntity<Map<String, Object>> obtenerConsolidadoGeneral() {
        try {
            log.info("📊 [ESTADISTICAS] Generando consolidado general...");
            
            // Obtener estadísticas globales
            Map<String, Object> estadisticasGlobales = estadisticaCU.obtenerEstadisticasGlobales();
            
            // Crear consolidado con estructura específica
            Map<String, Object> consolidado = new HashMap<>();
            consolidado.put("estadisticasGlobales", estadisticasGlobales);
            consolidado.put("porTipoProceso", estadisticasGlobales.get("porTipoProceso"));
            consolidado.put("porEstado", estadisticasGlobales.get("porEstado"));
            consolidado.put("totalProgramas", 3); // Número fijo de programas por ahora
            consolidado.put("fechaGeneracion", new Date());
            
            log.info("📊 [ESTADISTICAS] Resultado final: {}", consolidado);
            return ResponseEntity.ok(consolidado);
            
        } catch (Exception e) {
            log.error("❌ [ESTADISTICAS] Error obteniendo consolidado general: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Exporta estadísticas a PDF con filtros opcionales.
     * 
     * @param proceso Tipo de proceso (opcional)
     * @param idPrograma ID del programa (opcional)
     * @param fechaInicio Fecha de inicio (opcional)
     * @param fechaFin Fecha de fin (opcional)
     * @return ResponseEntity con archivo PDF
     */
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportarEstadisticasPDF(
            @RequestParam(required = false) String proceso,
            @RequestParam(required = false) Integer idPrograma,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin) {
        try {
            log.info("📄 [EXPORT_PDF] Generando PDF con filtros - Proceso: {}, Programa: {}, Fechas: {} - {}", 
                    proceso, idPrograma, fechaInicio, fechaFin);
            
            // Obtener datos filtrados
            Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasGlobales(proceso, idPrograma, fechaInicio, fechaFin);
            
            // Generar PDF (implementación básica)
            byte[] pdfBytes = generarPDF(estadisticas);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "estadisticas.txt");
            
            log.info("✅ [EXPORT_PDF] PDF generado exitosamente");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("❌ [EXPORT_PDF] Error generando PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Exporta estadísticas a Excel con filtros opcionales.
     * 
     * @param proceso Tipo de proceso (opcional)
     * @param idPrograma ID del programa (opcional)
     * @param fechaInicio Fecha de inicio (opcional)
     * @param fechaFin Fecha de fin (opcional)
     * @return ResponseEntity con archivo Excel
     */
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportarEstadisticasExcel(
            @RequestParam(required = false) String proceso,
            @RequestParam(required = false) Integer idPrograma,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin) {
        try {
            log.info("📊 [EXPORT_EXCEL] Generando Excel con filtros - Proceso: {}, Programa: {}, Fechas: {} - {}", 
                    proceso, idPrograma, fechaInicio, fechaFin);
            
            // Obtener datos filtrados
            Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasGlobales(proceso, idPrograma, fechaInicio, fechaFin);
            
            // Generar Excel (implementación básica)
            byte[] excelBytes = generarExcel(estadisticas);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "estadisticas.xlsx");
            
            log.info("✅ [EXPORT_EXCEL] Excel generado exitosamente");
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("❌ [EXPORT_EXCEL] Error generando Excel: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Genera un PDF básico con las estadísticas.
     * 
     * @param estadisticas Datos de estadísticas
     * @return Array de bytes del PDF
     */
    private byte[] generarPDF(Map<String, Object> estadisticas) {
        System.out.println("🔧 [PDF] Iniciando generación de PDF...");
        System.out.println("🔧 [PDF] Datos recibidos: " + estadisticas);
        
        ByteArrayOutputStream baos = null;
        com.itextpdf.text.Document document = null;
        
        try {
            baos = new ByteArrayOutputStream();
            document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);
            com.itextpdf.text.pdf.PdfWriter writer = com.itextpdf.text.pdf.PdfWriter.getInstance(document, baos);
            
            System.out.println("🔧 [PDF] Documento y writer creados");
            
            document.open();
            System.out.println("🔧 [PDF] Documento abierto");
            
            // Título
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("ESTADISTICAS DEL SISTEMA DE GESTION CURRICULAR", titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            System.out.println("🔧 [PDF] Título agregado");
            
            // Fecha de consulta
            String fechaConsulta = (String) estadisticas.get("fechaConsulta");
            if (fechaConsulta == null) fechaConsulta = "N/A";
            com.itextpdf.text.Font dateFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
            com.itextpdf.text.Paragraph fecha = new com.itextpdf.text.Paragraph("Fecha de consulta: " + fechaConsulta, dateFont);
            fecha.setSpacingAfter(15);
            document.add(fecha);
            System.out.println("🔧 [PDF] Fecha agregada");
            
            // Estadísticas principales
            com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12);
            
            document.add(new com.itextpdf.text.Paragraph("RESUMEN GENERAL", headerFont));
            document.add(new com.itextpdf.text.Paragraph("Total de Solicitudes: " + estadisticas.get("totalSolicitudes"), normalFont));
            document.add(new com.itextpdf.text.Paragraph("Solicitudes Aprobadas: " + estadisticas.get("totalAprobadas"), normalFont));
            document.add(new com.itextpdf.text.Paragraph("Solicitudes en Proceso: " + estadisticas.get("totalEnProceso"), normalFont));
            document.add(new com.itextpdf.text.Paragraph("Solicitudes Rechazadas: " + estadisticas.get("totalRechazadas"), normalFont));
            document.add(new com.itextpdf.text.Paragraph("Porcentaje de Aprobacion: " + estadisticas.get("porcentajeAprobacion") + "%", normalFont));
            System.out.println("🔧 [PDF] Resumen general agregado");
            
            document.add(new com.itextpdf.text.Paragraph(" "));
            
            // Estadísticas por programa
            @SuppressWarnings("unchecked")
            Map<String, Object> porPrograma = (Map<String, Object>) estadisticas.get("porPrograma");
            if (porPrograma != null && !porPrograma.isEmpty()) {
                document.add(new com.itextpdf.text.Paragraph("ESTADISTICAS POR PROGRAMA", headerFont));
                for (Map.Entry<String, Object> entry : porPrograma.entrySet()) {
                    document.add(new com.itextpdf.text.Paragraph(entry.getKey() + ": " + entry.getValue(), normalFont));
                }
                document.add(new com.itextpdf.text.Paragraph(" "));
                System.out.println("🔧 [PDF] Estadísticas por programa agregadas");
            }
            
            // Estadísticas por estado
            @SuppressWarnings("unchecked")
            Map<String, Object> porEstado = (Map<String, Object>) estadisticas.get("porEstado");
            if (porEstado != null && !porEstado.isEmpty()) {
                document.add(new com.itextpdf.text.Paragraph("ESTADISTICAS POR ESTADO", headerFont));
                for (Map.Entry<String, Object> entry : porEstado.entrySet()) {
                    document.add(new com.itextpdf.text.Paragraph(entry.getKey() + ": " + entry.getValue(), normalFont));
                }
                System.out.println("🔧 [PDF] Estadísticas por estado agregadas");
            }
            
            document.close();
            System.out.println("🔧 [PDF] Documento cerrado");
            
            byte[] pdfBytes = baos.toByteArray();
            System.out.println("🔧 [PDF] PDF generado exitosamente. Tamaño: " + pdfBytes.length + " bytes");
            
            return pdfBytes;
            
        } catch (Exception e) {
            System.err.println("❌ [PDF] Error generando PDF: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback a texto simple si hay error
            String contenido = "ESTADISTICAS DEL SISTEMA DE GESTION CURRICULAR\n\n";
            contenido += "Fecha de consulta: " + estadisticas.get("fechaConsulta") + "\n\n";
            contenido += "RESUMEN GENERAL:\n";
            contenido += "Total de Solicitudes: " + estadisticas.get("totalSolicitudes") + "\n";
            contenido += "Solicitudes Aprobadas: " + estadisticas.get("totalAprobadas") + "\n";
            contenido += "Solicitudes en Proceso: " + estadisticas.get("totalEnProceso") + "\n";
            contenido += "Solicitudes Rechazadas: " + estadisticas.get("totalRechazadas") + "\n";
            contenido += "Porcentaje de Aprobacion: " + estadisticas.get("porcentajeAprobacion") + "%\n\n";
            
            @SuppressWarnings("unchecked")
            Map<String, Object> porPrograma = (Map<String, Object>) estadisticas.get("porPrograma");
            if (porPrograma != null && !porPrograma.isEmpty()) {
                contenido += "ESTADISTICAS POR PROGRAMA:\n";
                for (Map.Entry<String, Object> entry : porPrograma.entrySet()) {
                    contenido += entry.getKey() + ": " + entry.getValue() + "\n";
                }
                contenido += "\n";
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> porEstado = (Map<String, Object>) estadisticas.get("porEstado");
            if (porEstado != null && !porEstado.isEmpty()) {
                contenido += "ESTADISTICAS POR ESTADO:\n";
                for (Map.Entry<String, Object> entry : porEstado.entrySet()) {
                    contenido += entry.getKey() + ": " + entry.getValue() + "\n";
                }
            }
            
            return contenido.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        } finally {
            try {
                if (document != null && document.isOpen()) {
                    document.close();
                }
                if (baos != null) {
                    baos.close();
                }
            } catch (Exception e) {
                System.err.println("❌ [PDF] Error cerrando recursos: " + e.getMessage());
            }
        }
    }

    /**
     * Genera un Excel básico con las estadísticas.
     * 
     * @param estadisticas Datos de estadísticas
     * @return Array de bytes del Excel
     */
    private byte[] generarExcel(Map<String, Object> estadisticas) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            
            // Hoja principal
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Estadísticas Generales");
            
            // Estilos
            org.apache.poi.ss.usermodel.CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            
            int rowNum = 0;
            
            // Título
            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("ESTADÍSTICAS DEL SISTEMA DE GESTIÓN CURRICULAR");
            titleCell.setCellStyle(titleStyle);
            
            // Fecha
            rowNum++;
            org.apache.poi.ss.usermodel.Row dateRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue("Fecha de consulta: " + estadisticas.get("fechaConsulta"));
            
            // Espacio
            rowNum++;
            
            // Resumen general
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("RESUMEN GENERAL");
            headerCell.setCellStyle(headerStyle);
            
            // Datos principales
            String[][] datosPrincipales = {
                {"Total de Solicitudes", estadisticas.get("totalSolicitudes").toString()},
                {"Solicitudes Aprobadas", estadisticas.get("totalAprobadas").toString()},
                {"Solicitudes en Proceso", estadisticas.get("totalEnProceso").toString()},
                {"Solicitudes Rechazadas", estadisticas.get("totalRechazadas").toString()},
                {"Porcentaje de Aprobación", estadisticas.get("porcentajeAprobacion").toString() + "%"}
            };
            
            for (String[] dato : datosPrincipales) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(dato[0]);
                row.createCell(1).setCellValue(dato[1]);
            }
            
            // Espacio
            rowNum++;
            
            // Estadísticas por programa
            @SuppressWarnings("unchecked")
            Map<String, Object> porPrograma = (Map<String, Object>) estadisticas.get("porPrograma");
            if (porPrograma != null && !porPrograma.isEmpty()) {
                org.apache.poi.ss.usermodel.Row programaHeaderRow = sheet.createRow(rowNum++);
                org.apache.poi.ss.usermodel.Cell programaHeaderCell = programaHeaderRow.createCell(0);
                programaHeaderCell.setCellValue("ESTADÍSTICAS POR PROGRAMA");
                programaHeaderCell.setCellStyle(headerStyle);
                
                for (Map.Entry<String, Object> entry : porPrograma.entrySet()) {
                    org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(entry.getKey());
                    row.createCell(1).setCellValue(entry.getValue().toString());
                }
                
                rowNum++;
            }
            
            // Estadísticas por estado
            @SuppressWarnings("unchecked")
            Map<String, Object> porEstado = (Map<String, Object>) estadisticas.get("porEstado");
            if (porEstado != null && !porEstado.isEmpty()) {
                org.apache.poi.ss.usermodel.Row estadoHeaderRow = sheet.createRow(rowNum++);
                org.apache.poi.ss.usermodel.Cell estadoHeaderCell = estadoHeaderRow.createCell(0);
                estadoHeaderCell.setCellValue("ESTADÍSTICAS POR ESTADO");
                estadoHeaderCell.setCellStyle(headerStyle);
                
                for (Map.Entry<String, Object> entry : porEstado.entrySet()) {
                    org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(entry.getKey());
                    row.createCell(1).setCellValue(entry.getValue().toString());
                }
            }
            
            // Ajustar ancho de columnas
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            
            workbook.write(baos);
            workbook.close();
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback a CSV simple si hay error
            String contenido = "Métrica,Valor\n" +
                              "Total Solicitudes," + estadisticas.get("totalSolicitudes") + "\n" +
                              "Aprobadas," + estadisticas.get("totalAprobadas") + "\n" +
                              "En Proceso," + estadisticas.get("totalEnProceso") + "\n" +
                              "Rechazadas," + estadisticas.get("totalRechazadas") + "\n" +
                              "Porcentaje Aprobación," + estadisticas.get("porcentajeAprobacion") + "%";
            return contenido.getBytes();
        }
    }

    /**
     * Obtiene el número total de estudiantes registrados en el sistema.
     * Utiliza UsuarioRepositoryInt para contar usuarios con rol de estudiante.
     * 
     * @return ResponseEntity con el conteo total de estudiantes
     */
    @GetMapping("/total-estudiantes")
    public ResponseEntity<Map<String, Object>> obtenerNumeroTotalEstudiantes() {
        try {
            log.info("👥 [ESTADISTICAS] Obteniendo número total de estudiantes...");
            
            Map<String, Object> resultado = estadisticaCU.obtenerNumeroTotalEstudiantes();
            
            log.info("👥 [ESTADISTICAS] Resultado: {} estudiantes", resultado.get("totalEstudiantes"));
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            log.error("❌ [ESTADISTICAS] Error obteniendo número total de estudiantes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene la distribución de estudiantes por programa académico.
     * Utiliza UsuarioRepositoryInt para contar estudiantes por programa.
     * 
     * @return ResponseEntity con la distribución de estudiantes por programa
     */
    @GetMapping("/estudiantes-por-programa")
    public ResponseEntity<Map<String, Object>> obtenerEstudiantesPorPrograma() {
        try {
            log.info("📊 [ESTADISTICAS] Obteniendo distribución de estudiantes por programa...");
            
            Map<String, Object> resultado = estadisticaCU.obtenerEstudiantesPorPrograma();
            
            log.info("📊 [ESTADISTICAS] Resultado: {} programas con estudiantes", 
                    ((Map<?, ?>) resultado.get("estudiantesPorPrograma")).size());
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            log.error("❌ [ESTADISTICAS] Error obteniendo estudiantes por programa: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene estadísticas detalladas por tipo de proceso.
     * Incluye conteos, porcentajes y análisis por proceso.
     * 
     * @return ResponseEntity con estadísticas detalladas por proceso
     */
    @GetMapping("/estadisticas-por-proceso")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasDetalladasPorProceso() {
        try {
            log.info("📈 [ESTADISTICAS] Obteniendo estadísticas detalladas por proceso...");
            
            Map<String, Object> resultado = estadisticaCU.obtenerEstadisticasDetalladasPorProceso();
            
            log.info("📈 [ESTADISTICAS] Resultado: {} procesos analizados", resultado.get("totalProcesos"));
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            log.error("❌ [ESTADISTICAS] Error obteniendo estadísticas por proceso: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene estadísticas resumidas por tipo de proceso para el dashboard.
     * Formato optimizado para gráficos y KPIs.
     * 
     * @return ResponseEntity con estadísticas resumidas por proceso
     */
    @GetMapping("/resumen-por-proceso")
    public ResponseEntity<Map<String, Object>> obtenerResumenPorProceso() {
        try {
            log.info("📊 [ESTADISTICAS] Obteniendo resumen por proceso...");
            
            Map<String, Object> resultado = estadisticaCU.obtenerResumenPorProceso();
            
            log.info("📊 [ESTADISTICAS] Resumen generado exitosamente");
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            log.error("❌ [ESTADISTICAS] Error obteniendo resumen por proceso: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
