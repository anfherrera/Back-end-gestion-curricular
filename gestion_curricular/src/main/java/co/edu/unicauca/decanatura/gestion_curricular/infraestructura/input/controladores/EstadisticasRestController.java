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
import java.util.Arrays;
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
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin) {
        try {
            log.info("Generando estadísticas globales con filtros múltiples - Proceso: {}, Programa: {}, Fechas: {} - {}", 
                    proceso, idPrograma, fechaInicio, fechaFin);
            
            // El método obtenerEstadisticasGlobales ya acepta TODOS los filtros simultáneamente
            Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasGlobales(proceso, idPrograma, fechaInicio, fechaFin);
            
            log.info("Resultado final: {}", estadisticas);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas globales: {}", e.getMessage(), e);
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
            log.info("Obteniendo estadísticas filtradas");
            log.info("Parámetros recibidos - nombreProceso: {}, idPrograma: {}, estado: {}, fechaInicio: {}, fechaFin: {}", 
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
            
            log.info("Resultado final: {}", respuesta);
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas filtradas: {}", e.getMessage(), e);
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
            @RequestParam(name = "fechaInicio1", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio1,
            @RequestParam(name = "fechaFin1", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin1,
            @RequestParam(name = "fechaInicio2", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio2,
            @RequestParam(name = "fechaFin2", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin2) {
        try {
            // Si no se proporcionan parámetros, devolver estadísticas globales como tendencias
            if (fechaInicio1 == null || fechaFin1 == null || fechaInicio2 == null || fechaFin2 == null) {
                Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasGlobales();
                return ResponseEntity.ok(estadisticas);
            }
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
        return procesarFiltrosDinamicos(filtros);
    }

    /**
     * Alias GET para filtros dinámicos (compatible con pruebas funcionales).
     * 
     * @param proceso Tipo de proceso (opcional)
     * @param estado Estado de solicitud (opcional)
     * @param idPrograma ID del programa (opcional)
     * @return ResponseEntity con estadísticas filtradas
     */
    @GetMapping("/filtros-dinamicos")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasConFiltrosDinamicosGET(
            @RequestParam(required = false) String proceso,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer idPrograma) {
        
        // Crear un Map con los parámetros recibidos
        Map<String, Object> filtros = new HashMap<>();
        if (proceso != null) filtros.put("nombreProceso", proceso);
        if (estado != null) filtros.put("estado", estado);
        if (idPrograma != null) filtros.put("idPrograma", idPrograma);
        
        return procesarFiltrosDinamicos(filtros);
    }

    /**
     * Método auxiliar para procesar filtros dinámicos.
     */
    private ResponseEntity<Map<String, Object>> procesarFiltrosDinamicos(Map<String, Object> filtros) {
        
        try {
            log.info("Obteniendo estadísticas con filtros dinámicos");
            log.info("Filtros recibidos: {}", filtros);
            
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
            
            log.info("Parámetros extraídos - nombreProceso: {}, idPrograma: {}, estado: {}, fechaInicio: {}, fechaFin: {}", 
                    nombreProceso, idPrograma, estado, fechaInicioStr, fechaFinStr);
            
            // Procesar fechas si están presentes
            Date fechaInicio = null;
            Date fechaFin = null;
            
            if (fechaInicioStr != null && !fechaInicioStr.trim().isEmpty()) {
                try {
                    fechaInicio = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(fechaInicioStr);
                } catch (Exception e) {
                    log.warn("Error parseando fechaInicio: {}", e.getMessage());
                }
            }
            
            if (fechaFinStr != null && !fechaFinStr.trim().isEmpty()) {
                try {
                    fechaFin = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(fechaFinStr);
                } catch (Exception e) {
                    log.warn("Error parseando fechaFin: {}", e.getMessage());
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
            
            log.info("Resultado final: {}", respuesta);
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas con filtros dinámicos: {}", e.getMessage(), e);
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
            log.info("Generando consolidado general...");
            
            // Obtener estadísticas globales
            Map<String, Object> estadisticasGlobales = estadisticaCU.obtenerEstadisticasGlobales();
            
            // Crear consolidado con estructura específica
            Map<String, Object> consolidado = new HashMap<>();
            consolidado.put("estadisticasGlobales", estadisticasGlobales);
            consolidado.put("porTipoProceso", estadisticasGlobales.get("porTipoProceso"));
            consolidado.put("porEstado", estadisticasGlobales.get("porEstado"));
            consolidado.put("totalProgramas", 3); // Número fijo de programas por ahora
            consolidado.put("fechaGeneracion", new Date());
            
            log.info("Resultado final: {}", consolidado);
            return ResponseEntity.ok(consolidado);
            
        } catch (Exception e) {
            log.error("Error obteniendo consolidado general: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Alias para obtener consolidado general (compatible con pruebas funcionales).
     * 
     * @return ResponseEntity con consolidado general
     */
    @GetMapping("/consolidadas")
    public ResponseEntity<Map<String, Object>> obtenerConsolidadas() {
        return obtenerConsolidadoGeneral();
    }

    /**
     * Exporta estadísticas generales a PDF (para Dashboard General).
     * 
     * @return ResponseEntity con archivo PDF
     */
    @GetMapping("/export/pdf/general")
    public ResponseEntity<byte[]> exportarEstadisticasGeneralesPDF() {
        try {
            log.info("Generando el PDF con estadísticas generales...");
            
            // Obtener solo estadísticas generales
            Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasGlobales();
            
            // Generar PDF solo con estadísticas generales
            byte[] pdfBytes = generarPDFEstadisticasGenerales(estadisticas);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "estadisticas_generales_dashboard.pdf");
            
            log.info("El PDF con estadísticas generales se generó correctamente.");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("No fue posible generar el PDF de estadísticas generales: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Exporta estadísticas de cursos de verano a PDF (para Dashboard Cursos de Verano).
     * 
     * @return ResponseEntity con archivo PDF
     */
    @GetMapping("/export/pdf/cursos-verano")
    public ResponseEntity<byte[]> exportarEstadisticasCursosVeranoPDF() {
        try {
            log.info("Generando el PDF con información de cursos de verano...");
            
            // Obtener solo datos de cursos de verano
            Map<String, Object> datosCursosVerano = estadisticaCU.obtenerEstadisticasCursosVerano();
            
            // Generar PDF solo con datos de cursos de verano
            byte[] pdfBytes = generarPDFCursosVerano(datosCursosVerano);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "estadisticas_cursos_verano.pdf");
            
            log.info("El PDF con datos de cursos de verano se generó correctamente.");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("No fue posible generar el PDF de cursos de verano: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Exporta estadísticas a PDF con filtros opcionales (endpoint general).
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
            log.info("Generando PDF con filtros. Proceso: {}, Programa: {}, Fechas: {} - {}", 
                    proceso, idPrograma, fechaInicio, fechaFin);
            
            // Obtener datos filtrados
            Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasGlobales(proceso, idPrograma, fechaInicio, fechaFin);
            
            // Obtener datos de cursos de verano si no hay filtros específicos o si se solicita
            Map<String, Object> datosCursosVerano = null;
            if (proceso == null || "CURSO_VERANO".equals(proceso)) {
                try {
                    datosCursosVerano = estadisticaCU.obtenerEstadisticasCursosVerano();
                    log.info("Datos de cursos de verano obtenidos para el PDF: {}", datosCursosVerano != null);
                } catch (Exception e) {
                    log.warn("No se pudieron obtener datos de cursos de verano para el PDF: {}", e.getMessage());
                }
            }
            
            // Generar PDF con datos completos
            byte[] pdfBytes = generarPDFCompleto(estadisticas, datosCursosVerano);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "estadisticas_generales.pdf");
            
            log.info("El PDF con filtros se generó correctamente.");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Ocurrió un error al generar el PDF con filtros: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Alias para exportar estadísticas a PDF (compatible con pruebas de aceptación).
     * 
     * @return ResponseEntity con archivo PDF
     */
    @GetMapping("/exportar/pdf")
    public ResponseEntity<byte[]> exportarEstadisticasPDFAlias() {
        return exportarEstadisticasPDF(null, null, null, null);
    }

    /**
     * Exporta estadísticas generales a Excel (para Dashboard General).
     * 
     * @return ResponseEntity con archivo Excel
     */
    @GetMapping("/export/excel/general")
    public ResponseEntity<byte[]> exportarEstadisticasGeneralesExcel() {
        try {
            log.info("Generando archivo Excel con estadísticas generales...");
            
            // Obtener solo estadísticas generales
            Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasGlobales();
            
            // Generar Excel solo con estadísticas generales
            byte[] excelBytes = generarExcelEstadisticasGenerales(estadisticas);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "estadisticas_generales_dashboard.xlsx");
            
            log.info("El Excel con estadísticas generales se generó correctamente.");
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("No fue posible generar el Excel de estadísticas generales: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Exporta estadísticas de cursos de verano a Excel (para Dashboard Cursos de Verano).
     * 
     * @return ResponseEntity con archivo Excel
     */
    @GetMapping("/export/excel/cursos-verano")
    public ResponseEntity<byte[]> exportarEstadisticasCursosVeranoExcel() {
        try {
            log.info("Generando archivo Excel con información de cursos de verano...");
            
            // Obtener solo datos de cursos de verano
            Map<String, Object> datosCursosVerano = estadisticaCU.obtenerEstadisticasCursosVerano();
            
            // Generar Excel solo con datos de cursos de verano
            byte[] excelBytes = generarExcelCursosVerano(datosCursosVerano);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "estadisticas_cursos_verano.xlsx");
            
            log.info("El Excel con datos de cursos de verano se generó correctamente.");
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("No fue posible generar el Excel de cursos de verano: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Exporta estadísticas a Excel con filtros opcionales (endpoint general).
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
            log.info("Generando Excel con filtros. Proceso: {}, Programa: {}, Fechas: {} - {}", 
                    proceso, idPrograma, fechaInicio, fechaFin);
            
            // Obtener datos filtrados
            Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasGlobales(proceso, idPrograma, fechaInicio, fechaFin);
            
            // Obtener datos de cursos de verano si no hay filtros específicos o si se solicita
            Map<String, Object> datosCursosVerano = null;
            if (proceso == null || "CURSO_VERANO".equals(proceso)) {
                try {
                    datosCursosVerano = estadisticaCU.obtenerEstadisticasCursosVerano();
                    log.info("Datos de cursos de verano disponibles para el Excel: {}", datosCursosVerano != null);
                } catch (Exception e) {
                    log.warn("No se pudieron obtener datos de cursos de verano para el Excel: {}", e.getMessage());
                }
            }
            
            // Generar Excel con datos completos
            byte[] excelBytes = generarExcelCompleto(estadisticas, datosCursosVerano);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "estadisticas_generales.xlsx");
            
            log.info("El Excel con filtros se generó correctamente.");
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Ocurrió un error al generar el Excel con filtros: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Alias para exportar estadísticas a Excel (compatible con pruebas de aceptación).
     * 
     * @return ResponseEntity con archivo Excel
     */
    @GetMapping("/exportar/excel")
    public ResponseEntity<byte[]> exportarEstadisticasExcelAlias() {
        try {
            log.info("Generando Excel sin filtros para exportación...");
            
            // Obtener datos filtrados
            Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasGlobales(null, null, null, null);
            
            // Obtener datos de cursos de verano
            Map<String, Object> datosCursosVerano = null;
            try {
                datosCursosVerano = estadisticaCU.obtenerEstadisticasCursosVerano();
                log.info("Datos de cursos de verano obtenidos para el Excel sin filtros: {}", datosCursosVerano != null);
            } catch (Exception e) {
                log.warn("No se pudieron obtener datos de cursos de verano para el Excel sin filtros: {}", e.getMessage());
            }
            
            // Generar Excel con datos completos
            byte[] excelBytes = generarExcelCompleto(estadisticas, datosCursosVerano);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "estadisticas_generales.xlsx");
            
            log.info("El Excel sin filtros se generó correctamente.");
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Ocurrió un error al generar el Excel sin filtros: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Genera un PDF solo con estadísticas generales (para Dashboard General).
     * 
     * @param estadisticas Datos de estadísticas generales
     * @return Array de bytes del PDF
     */
    private byte[] generarPDFEstadisticasGenerales(Map<String, Object> estadisticas) {
        System.out.println("Iniciando la generación del PDF con estadísticas generales...");
        
        ByteArrayOutputStream baos = null;
        com.itextpdf.text.Document document = null;
        
        try {
            baos = new ByteArrayOutputStream();
            document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, baos);
            
            document.open();
            
            // Título principal
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("DASHBOARD GENERAL - ESTADÍSTICAS DEL SISTEMA", titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            // Fecha de generación
            com.itextpdf.text.Font dateFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
            com.itextpdf.text.Paragraph fecha = new com.itextpdf.text.Paragraph("Fecha de generación: " + new java.util.Date().toString(), dateFont);
            fecha.setSpacingAfter(15);
            document.add(fecha);
            
            // Sección: Estadísticas Generales
            agregarSeccionEstadisticasGenerales(document, estadisticas);
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            System.err.println("Error al generar el PDF de estadísticas generales: " + e.getMessage());
            e.printStackTrace();
            
            // Generar PDF de error
            try {
                ByteArrayOutputStream errorBaos = new ByteArrayOutputStream();
                com.itextpdf.text.Document errorDoc = new com.itextpdf.text.Document();
                com.itextpdf.text.pdf.PdfWriter.getInstance(errorDoc, errorBaos);
                
                errorDoc.open();
                com.itextpdf.text.Font errorFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12);
                com.itextpdf.text.Paragraph errorMsg = new com.itextpdf.text.Paragraph("Error al generar el reporte: " + e.getMessage(), errorFont);
                errorDoc.add(errorMsg);
                errorDoc.close();
                
                return errorBaos.toByteArray();
            } catch (Exception ex) {
                System.err.println("No se pudo generar el PDF alterno con información de error: " + ex.getMessage());
                return new byte[0];
            }
        } finally {
            try {
                if (document != null && document.isOpen()) {
                    document.close();
                }
                if (baos != null) {
                    baos.close();
                }
            } catch (Exception e) {
                System.err.println("Ocurrió un error al cerrar los recursos del PDF general: " + e.getMessage());
            }
        }
    }

    /**
     * Genera un PDF solo con estadísticas de cursos de verano (para Dashboard Cursos de Verano).
     * 
     * @param datosCursosVerano Datos de cursos de verano
     * @return Array de bytes del PDF
     */
    private byte[] generarPDFCursosVerano(Map<String, Object> datosCursosVerano) {
        System.out.println("Iniciando la generación del PDF con información de cursos de verano...");
        
        ByteArrayOutputStream baos = null;
        com.itextpdf.text.Document document = null;
        
        try {
            baos = new ByteArrayOutputStream();
            document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, baos);
            
            document.open();
            
            // Título principal
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("DASHBOARD CURSOS DE VERANO - ANÁLISIS DE DEMANDA", titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            // Fecha de generación
            com.itextpdf.text.Font dateFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
            com.itextpdf.text.Paragraph fecha = new com.itextpdf.text.Paragraph("Fecha de generación: " + new java.util.Date().toString(), dateFont);
            fecha.setSpacingAfter(15);
            document.add(fecha);
            
            // Sección: Estadísticas de Cursos de Verano
            agregarSeccionCursosVerano(document, datosCursosVerano);
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            System.err.println("Error al generar el PDF de cursos de verano: " + e.getMessage());
            e.printStackTrace();
            
            // Generar PDF de error
            try {
                ByteArrayOutputStream errorBaos = new ByteArrayOutputStream();
                com.itextpdf.text.Document errorDoc = new com.itextpdf.text.Document();
                com.itextpdf.text.pdf.PdfWriter.getInstance(errorDoc, errorBaos);
                
                errorDoc.open();
                com.itextpdf.text.Font errorFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12);
                com.itextpdf.text.Paragraph errorMsg = new com.itextpdf.text.Paragraph("Error al generar el reporte: " + e.getMessage(), errorFont);
                errorDoc.add(errorMsg);
                errorDoc.close();
                
                return errorBaos.toByteArray();
            } catch (Exception ex) {
                System.err.println("No se pudo generar el PDF de error para cursos de verano: " + ex.getMessage());
                return new byte[0];
            }
        } finally {
            try {
                if (document != null && document.isOpen()) {
                    document.close();
                }
                if (baos != null) {
                    baos.close();
                }
            } catch (Exception e) {
                System.err.println("Ocurrió un error al cerrar los recursos del PDF de cursos de verano: " + e.getMessage());
            }
        }
    }

    /**
     * Genera un PDF completo con estadísticas generales y de cursos de verano.
     * 
     * @param estadisticas Datos de estadísticas generales
     * @param datosCursosVerano Datos de cursos de verano
     * @return Array de bytes del PDF
     */
    private byte[] generarPDFCompleto(Map<String, Object> estadisticas, Map<String, Object> datosCursosVerano) {
        System.out.println("Iniciando la generación del PDF completo con estadísticas generales y de cursos de verano...");
        System.out.println("¿Se recibieron estadísticas generales? " + (estadisticas != null));
        System.out.println("¿Se recibieron datos de cursos de verano? " + (datosCursosVerano != null));
        
        ByteArrayOutputStream baos = null;
        com.itextpdf.text.Document document = null;
        
        try {
            baos = new ByteArrayOutputStream();
            document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, baos);
            
            document.open();
            
            // Título principal
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("REPORTE GENERAL DE ESTADÍSTICAS", titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            // Fecha de generación
            com.itextpdf.text.Font dateFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
            com.itextpdf.text.Paragraph fecha = new com.itextpdf.text.Paragraph("Fecha de generación: " + new java.util.Date().toString(), dateFont);
            fecha.setSpacingAfter(15);
            document.add(fecha);
            
            // Sección 1: Estadísticas Generales
            if (estadisticas != null) {
                agregarSeccionEstadisticasGenerales(document, estadisticas);
            }
            
            // Sección 2: Estadísticas de Cursos de Verano
            if (datosCursosVerano != null) {
                document.newPage(); // Nueva página para cursos de verano
                agregarSeccionCursosVerano(document, datosCursosVerano);
            }
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            System.err.println("Error al generar el PDF completo: " + e.getMessage());
            e.printStackTrace();
            
            // Generar un PDF de error en lugar de texto
            try {
                ByteArrayOutputStream errorBaos = new ByteArrayOutputStream();
                com.itextpdf.text.Document errorDoc = new com.itextpdf.text.Document();
                com.itextpdf.text.pdf.PdfWriter.getInstance(errorDoc, errorBaos);
                
                errorDoc.open();
                com.itextpdf.text.Font errorFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12);
                com.itextpdf.text.Paragraph errorMsg = new com.itextpdf.text.Paragraph("Error al generar el reporte: " + e.getMessage(), errorFont);
                errorDoc.add(errorMsg);
                errorDoc.close();
                
                return errorBaos.toByteArray();
            } catch (Exception ex) {
                System.err.println("No se pudo generar el PDF de error para el reporte completo: " + ex.getMessage());
                return new byte[0]; // Devolver array vacío en caso de error crítico
            }
        } finally {
            try {
                if (document != null && document.isOpen()) {
                    document.close();
                }
                if (baos != null) {
                    baos.close();
                }
            } catch (Exception e) {
                System.err.println("Ocurrió un error al cerrar los recursos del PDF completo: " + e.getMessage());
            }
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
            log.info("Obteniendo número total de estudiantes...");
            
            Map<String, Object> resultado = estadisticaCU.obtenerNumeroTotalEstudiantes();
            
            log.info("Resultado: {} estudiantes", resultado.get("totalEstudiantes"));
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            log.error("Error obteniendo número total de estudiantes: {}", e.getMessage(), e);
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
            log.info("Obteniendo distribución de estudiantes por programa...");
            
            Map<String, Object> resultado = estadisticaCU.obtenerEstudiantesPorPrograma();
            
            log.info("Resultado: {} programas con estudiantes", 
                    ((Map<?, ?>) resultado.get("estudiantesPorPrograma")).size());
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            log.error("Error obteniendo estudiantes por programa: {}", e.getMessage(), e);
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
            log.info("Obteniendo estadísticas detalladas por proceso...");
            
            Map<String, Object> resultado = estadisticaCU.obtenerEstadisticasDetalladasPorProceso();
            
            log.info("Resultado: {} procesos analizados", resultado.get("totalProcesos"));
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas por proceso: {}", e.getMessage(), e);
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
            log.info("Obteniendo resumen por proceso...");
            
            Map<String, Object> resultado = estadisticaCU.obtenerResumenPorProceso();
            
            log.info("Resumen generado exitosamente");
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            log.error("Error obteniendo resumen por proceso: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene configuración de estilos y colores para el dashboard.
     * Incluye tema claro con fondo blanco.
     * 
     * @return ResponseEntity con configuración de estilos
     */
    @GetMapping("/configuracion-estilos")
    public ResponseEntity<Map<String, Object>> obtenerConfiguracionEstilos() {
        try {
            log.info("Obteniendo configuración de estilos...");
            
            Map<String, Object> resultado = estadisticaCU.obtenerConfiguracionEstilos();
            
            log.info("Configuración de estilos generada exitosamente");
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            log.error("Error obteniendo configuración de estilos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene estadísticas por estado de solicitudes.
     * Incluye conteos, porcentajes y análisis por estado.
     * 
     * @return ResponseEntity con estadísticas por estado
     */
    @GetMapping("/estado-solicitudes")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorEstado() {
        try {
            log.info("Obteniendo estadísticas por estado de solicitudes...");
            
            Map<String, Object> resultado = estadisticaCU.obtenerEstadisticasPorEstado();
            
            log.info("Estadísticas por estado generadas exitosamente");
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas por estado: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene estadísticas por período/mes.
     * Incluye tendencias, picos de actividad y análisis temporal.
     * 
     * @return ResponseEntity con estadísticas por período
     */
    @GetMapping("/por-periodo")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorPeriodo() {
        try {
            log.info("Obteniendo estadísticas por período...");
            
            Map<String, Object> resultado = estadisticaCU.obtenerEstadisticasPorPeriodo();
            
            log.info("Estadísticas por período generadas exitosamente");
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas por período: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene estadísticas por programa académico.
     * Incluye distribución de solicitudes, estudiantes y análisis por programa.
     * 
     * @return ResponseEntity con estadísticas por programa
     */
    @GetMapping("/por-programa")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorPrograma() {
        try {
            log.info("Obteniendo estadísticas por programa académico...");
            
            Map<String, Object> resultado = estadisticaCU.obtenerEstadisticasPorPrograma();
            
            log.info("Estadísticas por programa generadas exitosamente");
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas por programa: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene estadísticas de tiempo promedio de procesamiento.
     * Incluye tiempos por proceso, funcionario y análisis de eficiencia.
     * 
     * @return ResponseEntity con estadísticas de tiempo de procesamiento
     */
    @GetMapping("/tiempo-procesamiento")
    public ResponseEntity<Map<String, Object>> obtenerTiempoPromedioProcesamiento() {
        try {
            log.info("Obteniendo estadísticas de tiempo de procesamiento...");
            
            Map<String, Object> resultado = estadisticaCU.obtenerTiempoPromedioProcesamiento();
            
            log.info("Estadísticas de tiempo de procesamiento generadas exitosamente");
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas de tiempo de procesamiento: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene tendencias y comparativas del sistema.
     * Incluye análisis de crecimiento, comparaciones entre períodos y tendencias estratégicas.
     * 
     * @return ResponseEntity con tendencias y comparativas
     */
    @GetMapping("/tendencias-comparativas")
    public ResponseEntity<Map<String, Object>> obtenerTendenciasYComparativas() {
        try {
            log.info("Obteniendo tendencias y comparativas...");
            
            Map<String, Object> resultado = estadisticaCU.obtenerTendenciasYComparativas();
            
            log.info("Tendencias y comparativas generadas exitosamente");
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            log.error("Error obteniendo tendencias y comparativas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint de prueba simple para verificar ECAES
     */
    @GetMapping("/test-ecaes")
    public ResponseEntity<Map<String, Object>> testEcaes() {
        try {
            log.info("Prueba - Probando ECAES...");
            
            Map<String, Object> resultado = new HashMap<>();
            
            // Probar datos básicos de ECAES
            resultado.put("mensaje", "Test ECAES exitoso");
            resultado.put("fecha", new Date());
            resultado.put("procesos", Arrays.asList("Homologación", "Paz y Salvo", "Reingreso", "Cursos de Verano", "ECAES"));
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            log.error("Prueba - Error en test ECAES: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("fecha", new Date());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Endpoint de prueba para estadísticas simples por proceso
     */
    @GetMapping("/test-procesos-simples")
    public ResponseEntity<Map<String, Object>> testProcesosSimples() {
        try {
            log.info("Prueba - Probando estadísticas simples por proceso...");
            
            Map<String, Object> resultado = estadisticaCU.obtenerEstadisticasPorProceso("ECAES");
            
            log.info("Prueba - Resultado: {}", resultado);
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            log.error("Prueba - Error en test procesos simples: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("fecha", new Date());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtiene estadísticas específicas para cursos de verano.
     * Incluye análisis de demanda por materia, tendencias temporales y recomendaciones.
     * 
     * @return ResponseEntity con estadísticas detalladas de cursos de verano
     */
    @GetMapping("/cursos-verano")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasCursosVerano() {
        try {
            log.info("Cursos de verano - Obteniendo estadísticas de cursos de verano...");
            
            Map<String, Object> resultado = estadisticaCU.obtenerEstadisticasCursosVerano();
            
            log.info("Cursos de verano - Estadísticas de cursos de verano generadas exitosamente");
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            log.error("Cursos de verano - Error obteniendo estadísticas de cursos de verano: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint optimizado para obtener solo las tendencias temporales de cursos de verano.
     * 
     * @return ResponseEntity con tendencias temporales
     */
    @GetMapping("/cursos-verano/tendencias-temporales")
    public ResponseEntity<Map<String, Object>> obtenerTendenciasTemporalesCursosVerano() {
        try {
            log.info("Tendencias temporales - Obteniendo tendencias temporales de cursos de verano...");
            
            // Obtener solo las tendencias temporales de manera optimizada
            Map<String, Object> tendencias = estadisticaCU.obtenerTendenciasTemporalesCursosVerano();
            
            log.info("Tendencias temporales - Tendencias obtenidas exitosamente");
            return ResponseEntity.ok(tendencias);
            
        } catch (Exception e) {
            log.error("Tendencias temporales - Error obteniendo tendencias: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint alternativo para estadísticas por proceso que funciona
     */
    @GetMapping("/por-proceso-funcional")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorProcesoFuncional() {
        try {
            log.info("Obteniendo estadísticas por proceso (funcional)...");
            
            Map<String, Object> resultado = new HashMap<>();
            
            // Obtener datos de cada proceso individualmente
            String[] procesos = {"Homologación", "Paz y Salvo", "Reingreso", "Cursos de Verano", "ECAES"};
            Map<String, Object> procesosDetallados = new HashMap<>();
            
            for (String proceso : procesos) {
                try {
                    Map<String, Object> datosProceso = estadisticaCU.obtenerEstadisticasPorProceso(proceso);
                    procesosDetallados.put(proceso, datosProceso);
                } catch (Exception e) {
                    log.warn("Error obteniendo datos para {}: {}", proceso, e.getMessage());
                    // Continuar con los otros procesos
                }
            }
            
            resultado.put("procesos", procesosDetallados);
            resultado.put("totalProcesos", procesosDetallados.size());
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Estadísticas por proceso - Versión funcional");
            
            log.info("Resultado: {} procesos analizados", procesosDetallados.size());
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas por proceso: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Agrega la sección de estadísticas generales al PDF.
     */
    private void agregarSeccionEstadisticasGenerales(com.itextpdf.text.Document document, Map<String, Object> estadisticas) throws Exception {
        // Título de sección
        com.itextpdf.text.Font sectionFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Paragraph sectionTitle = new com.itextpdf.text.Paragraph("1. ESTADÍSTICAS GENERALES", sectionFont);
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);
        
        // Estadísticas principales
        com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
        
        // Total de solicitudes
        Object totalSolicitudes = estadisticas.get("totalSolicitudes");
        if (totalSolicitudes != null) {
            document.add(new com.itextpdf.text.Paragraph("Total de Solicitudes: " + totalSolicitudes, normalFont));
        }
        
        // Por tipo de proceso
        @SuppressWarnings("unchecked")
        Map<String, Object> porTipoProceso = (Map<String, Object>) estadisticas.get("porTipoProceso");
        if (porTipoProceso != null && !porTipoProceso.isEmpty()) {
            document.add(new com.itextpdf.text.Paragraph("\nEstadísticas por Tipo de Proceso:", normalFont));
            for (Map.Entry<String, Object> entry : porTipoProceso.entrySet()) {
                document.add(new com.itextpdf.text.Paragraph("  • " + entry.getKey() + ": " + entry.getValue(), normalFont));
            }
        }
        
        // Por estado
        @SuppressWarnings("unchecked")
        Map<String, Object> porEstado = (Map<String, Object>) estadisticas.get("porEstado");
        if (porEstado != null && !porEstado.isEmpty()) {
            document.add(new com.itextpdf.text.Paragraph("\nEstadísticas por Estado:", normalFont));
            for (Map.Entry<String, Object> entry : porEstado.entrySet()) {
                document.add(new com.itextpdf.text.Paragraph("  • " + entry.getKey() + ": " + entry.getValue(), normalFont));
            }
        }
    }

    /**
     * Agrega la sección de cursos de verano al PDF.
     */
    private void agregarSeccionCursosVerano(com.itextpdf.text.Document document, Map<String, Object> datosCursosVerano) throws Exception {
        // Título de sección
        com.itextpdf.text.Font sectionFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Paragraph sectionTitle = new com.itextpdf.text.Paragraph("2. ESTADÍSTICAS DE CURSOS DE VERANO", sectionFont);
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);
        
        com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
        
        // Resumen
        @SuppressWarnings("unchecked")
        Map<String, Object> resumen = (Map<String, Object>) datosCursosVerano.get("resumen");
        if (resumen != null) {
            document.add(new com.itextpdf.text.Paragraph("Resumen General:", normalFont));
            document.add(new com.itextpdf.text.Paragraph("  • Total Solicitudes: " + resumen.get("totalSolicitudes"), normalFont));
            document.add(new com.itextpdf.text.Paragraph("  • Materias Únicas: " + resumen.get("materiasUnicas"), normalFont));
            document.add(new com.itextpdf.text.Paragraph("  • Programas Participantes: " + resumen.get("programasParticipantes"), normalFont));
            document.add(new com.itextpdf.text.Paragraph("  • Tasa de Aprobación: " + resumen.get("tasaAprobacion") + "%", normalFont));
        }
        
        // Top materias
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topMaterias = (List<Map<String, Object>>) datosCursosVerano.get("topMaterias");
        if (topMaterias != null && !topMaterias.isEmpty()) {
            document.add(new com.itextpdf.text.Paragraph("\nTop Materias por Demanda:", normalFont));
            for (Map<String, Object> materia : topMaterias) {
                document.add(new com.itextpdf.text.Paragraph("  • " + materia.get("nombre") + ": " + 
                    materia.get("solicitudes") + " solicitudes (" + materia.get("porcentaje") + "%)", normalFont));
            }
        }
        
        // Análisis por programa
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> analisisPorPrograma = (List<Map<String, Object>>) datosCursosVerano.get("analisisPorPrograma");
        if (analisisPorPrograma != null && !analisisPorPrograma.isEmpty()) {
            document.add(new com.itextpdf.text.Paragraph("\nAnálisis por Programa:", normalFont));
            for (Map<String, Object> programa : analisisPorPrograma) {
                document.add(new com.itextpdf.text.Paragraph("  • " + programa.get("nombre") + ": " + 
                    programa.get("solicitudes") + " solicitudes (" + programa.get("porcentaje") + "%)", normalFont));
            }
        }
        
        // Predicciones
        @SuppressWarnings("unchecked")
        Map<String, Object> predicciones = (Map<String, Object>) datosCursosVerano.get("predicciones");
        if (predicciones != null) {
            document.add(new com.itextpdf.text.Paragraph("\nPredicciones:", normalFont));
            document.add(new com.itextpdf.text.Paragraph("  • Demanda Estimada Próximo Período: " + predicciones.get("demandaEstimadaProximoPeriodo"), normalFont));
            document.add(new com.itextpdf.text.Paragraph("  • Confiabilidad: " + predicciones.get("confiabilidad"), normalFont));
            document.add(new com.itextpdf.text.Paragraph("  • Metodología: " + predicciones.get("metodologia"), normalFont));
        }
        
        // Recomendaciones
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> recomendaciones = (List<Map<String, Object>>) datosCursosVerano.get("recomendaciones");
        if (recomendaciones != null && !recomendaciones.isEmpty()) {
            document.add(new com.itextpdf.text.Paragraph("\nRecomendaciones:", normalFont));
            for (Map<String, Object> rec : recomendaciones) {
                document.add(new com.itextpdf.text.Paragraph("  • " + rec.get("titulo") + ": " + rec.get("descripcion"), normalFont));
            }
        }
    }

    /**
     * Genera un Excel completo con estadísticas generales y de cursos de verano.
     */
    private byte[] generarExcelCompleto(Map<String, Object> estadisticas, Map<String, Object> datosCursosVerano) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            
            // Hoja 1: Estadísticas Generales
            if (estadisticas != null) {
                crearHojaEstadisticasGenerales(workbook, estadisticas);
            }
            
            // Hoja 2: Cursos de Verano
            if (datosCursosVerano != null) {
                crearHojaCursosVerano(workbook, datosCursosVerano);
            }
            
            workbook.write(baos);
            workbook.close();
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            System.err.println("Excel completo - Error generando Excel completo: " + e.getMessage());
            e.printStackTrace();
            
            // Generar un Excel de error en lugar de texto
            try {
                ByteArrayOutputStream errorBaos = new ByteArrayOutputStream();
                org.apache.poi.xssf.usermodel.XSSFWorkbook errorWorkbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
                org.apache.poi.ss.usermodel.Sheet errorSheet = errorWorkbook.createSheet("Error");
                
                org.apache.poi.ss.usermodel.Row errorRow = errorSheet.createRow(0);
                errorRow.createCell(0).setCellValue("Error al generar el reporte: " + e.getMessage());
                
                errorWorkbook.write(errorBaos);
                errorWorkbook.close();
                
                return errorBaos.toByteArray();
            } catch (Exception ex) {
                System.err.println("Excel completo - Error generando Excel de error: " + ex.getMessage());
                return new byte[0]; // Devolver array vacío en caso de error crítico
            }
        }
    }

    /**
     * Crea la hoja de estadísticas generales en el Excel.
     */
    private void crearHojaEstadisticasGenerales(org.apache.poi.xssf.usermodel.XSSFWorkbook workbook, Map<String, Object> estadisticas) {
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
        titleCell.setCellValue("ESTADÍSTICAS GENERALES DEL SISTEMA");
            titleCell.setCellStyle(titleStyle);
            
        rowNum++; // Espacio
        
        // Total de solicitudes
        Object totalSolicitudes = estadisticas.get("totalSolicitudes");
        if (totalSolicitudes != null) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("Total de Solicitudes");
            row.createCell(1).setCellValue(totalSolicitudes.toString());
        }
        
        // Por tipo de proceso
        @SuppressWarnings("unchecked")
        Map<String, Object> porTipoProceso = (Map<String, Object>) estadisticas.get("porTipoProceso");
        if (porTipoProceso != null && !porTipoProceso.isEmpty()) {
            rowNum++; // Espacio
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("ESTADÍSTICAS POR TIPO DE PROCESO");
            headerCell.setCellStyle(headerStyle);
            
            for (Map.Entry<String, Object> entry : porTipoProceso.entrySet()) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue().toString());
            }
            }
            
        // Por estado
            @SuppressWarnings("unchecked")
        Map<String, Object> porEstado = (Map<String, Object>) estadisticas.get("porEstado");
        if (porEstado != null && !porEstado.isEmpty()) {
            rowNum++; // Espacio
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("ESTADÍSTICAS POR ESTADO");
            headerCell.setCellStyle(headerStyle);
            
            for (Map.Entry<String, Object> entry : porEstado.entrySet()) {
                    org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(entry.getKey());
                    row.createCell(1).setCellValue(entry.getValue().toString());
            }
        }
        
        // Ajustar ancho de columnas
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    /**
     * Crea la hoja de cursos de verano en el Excel.
     */
    private void crearHojaCursosVerano(org.apache.poi.xssf.usermodel.XSSFWorkbook workbook, Map<String, Object> datosCursosVerano) {
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Cursos de Verano");
        
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
        titleCell.setCellValue("ESTADÍSTICAS DE CURSOS DE VERANO");
        titleCell.setCellStyle(titleStyle);
        
        rowNum++; // Espacio
        
        // Resumen
            @SuppressWarnings("unchecked")
        Map<String, Object> resumen = (Map<String, Object>) datosCursosVerano.get("resumen");
        if (resumen != null) {
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("RESUMEN GENERAL");
            headerCell.setCellStyle(headerStyle);
            
            for (Map.Entry<String, Object> entry : resumen.entrySet()) {
                    org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(entry.getKey());
                    row.createCell(1).setCellValue(entry.getValue().toString());
                }
            rowNum++; // Espacio
        }
        
        // Top materias
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topMaterias = (List<Map<String, Object>>) datosCursosVerano.get("topMaterias");
        if (topMaterias != null && !topMaterias.isEmpty()) {
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("TOP MATERIAS POR DEMANDA");
            headerCell.setCellStyle(headerStyle);
            
            for (Map<String, Object> materia : topMaterias) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(materia.get("nombre").toString());
                row.createCell(1).setCellValue(materia.get("solicitudes").toString());
                row.createCell(2).setCellValue(materia.get("porcentaje").toString() + "%");
            }
            rowNum++; // Espacio
        }
        
        // Análisis por programa
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> analisisPorPrograma = (List<Map<String, Object>>) datosCursosVerano.get("analisisPorPrograma");
        if (analisisPorPrograma != null && !analisisPorPrograma.isEmpty()) {
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("ANÁLISIS POR PROGRAMA");
            headerCell.setCellStyle(headerStyle);
            
            for (Map<String, Object> programa : analisisPorPrograma) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(programa.get("nombre").toString());
                row.createCell(1).setCellValue(programa.get("solicitudes").toString());
                row.createCell(2).setCellValue(programa.get("porcentaje").toString() + "%");
            }
            }
            
            // Ajustar ancho de columnas
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
    }

    /**
     * Genera un Excel solo con estadísticas generales (para Dashboard General).
     * 
     * @param estadisticas Datos de estadísticas generales
     * @return Array de bytes del Excel
     */
    private byte[] generarExcelEstadisticasGenerales(Map<String, Object> estadisticas) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            
            // Hoja: Estadísticas Generales
            crearHojaEstadisticasGenerales(workbook, estadisticas);
            
            workbook.write(baos);
            workbook.close();
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            System.err.println("Excel general - Error generando Excel: " + e.getMessage());
            e.printStackTrace();
            
            // Generar Excel de error
            try {
                ByteArrayOutputStream errorBaos = new ByteArrayOutputStream();
                org.apache.poi.xssf.usermodel.XSSFWorkbook errorWorkbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
                org.apache.poi.ss.usermodel.Sheet errorSheet = errorWorkbook.createSheet("Error");
                
                org.apache.poi.ss.usermodel.Row errorRow = errorSheet.createRow(0);
                errorRow.createCell(0).setCellValue("Error al generar el reporte: " + e.getMessage());
                
                errorWorkbook.write(errorBaos);
                errorWorkbook.close();
                
                return errorBaos.toByteArray();
            } catch (Exception ex) {
                System.err.println("Excel general - Error generando Excel de error: " + ex.getMessage());
                return new byte[0];
            }
        }
    }

    /**
     * Genera un Excel solo con estadísticas de cursos de verano (para Dashboard Cursos de Verano).
     * 
     * @param datosCursosVerano Datos de cursos de verano
     * @return Array de bytes del Excel
     */
    private byte[] generarExcelCursosVerano(Map<String, Object> datosCursosVerano) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            
            // Hoja: Cursos de Verano
            crearHojaCursosVerano(workbook, datosCursosVerano);
            
            workbook.write(baos);
            workbook.close();
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            System.err.println("Excel cursos verano - Error generando Excel: " + e.getMessage());
            e.printStackTrace();
            
            // Generar Excel de error
            try {
                ByteArrayOutputStream errorBaos = new ByteArrayOutputStream();
                org.apache.poi.xssf.usermodel.XSSFWorkbook errorWorkbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
                org.apache.poi.ss.usermodel.Sheet errorSheet = errorWorkbook.createSheet("Error");
                
                org.apache.poi.ss.usermodel.Row errorRow = errorSheet.createRow(0);
                errorRow.createCell(0).setCellValue("Error al generar el reporte: " + e.getMessage());
                
                errorWorkbook.write(errorBaos);
                errorWorkbook.close();
                
                return errorBaos.toByteArray();
            } catch (Exception ex) {
                System.err.println("Excel cursos verano - Error generando Excel de error: " + ex.getMessage());
                return new byte[0];
            }
        }
    }
}

