package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarEstadisticasCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Estadistica;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.PeriodoAcademicoEnum;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin) {
        try {
            // Si hay período académico, usar resumen completo que soporta filtros por período
            if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
                String periodoNormalizado = normalizarPeriodoAcademico(periodoAcademico);
                
                Map<String, Object> resumen = estadisticaCU.obtenerResumenCompleto(periodoNormalizado, idPrograma);
                @SuppressWarnings("unchecked")
                Map<String, Object> estadisticasGlobales = (Map<String, Object>) resumen.get("estadisticasGlobales");
                
                // Si hay filtro de proceso, aplicar filtro adicional
                if (proceso != null && !proceso.trim().isEmpty() && estadisticasGlobales != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> porTipoProceso = (Map<String, Object>) estadisticasGlobales.get("porTipoProceso");
                    if (porTipoProceso != null && porTipoProceso.containsKey(proceso)) {
                        // Filtrar solo el proceso solicitado
                        Map<String, Object> procesoFiltrado = new HashMap<>();
                        procesoFiltrado.put(proceso, porTipoProceso.get(proceso));
                        estadisticasGlobales.put("porTipoProceso", procesoFiltrado);
                    }
                }
                
                return ResponseEntity.ok(estadisticasGlobales != null ? estadisticasGlobales : new HashMap<>());
            }
            
            // Si no hay período académico, usar el método original
            Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasGlobales(proceso, idPrograma, fechaInicio, fechaFin);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            // Devolver 200 OK con bandera de error para que el frontend pueda parsear el JSON fácilmente
            // Devolver 200 OK en lugar de 500 para que el frontend pueda leer el JSON sin problemas
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("totalSolicitudes", 0);
            errorResponse.put("totalAprobadas", 0);
            errorResponse.put("totalRechazadas", 0);
            errorResponse.put("totalEnviadas", 0);
            errorResponse.put("totalEnProceso", 0);
            errorResponse.put("porcentajeAprobacion", 0.0);
            errorResponse.put("porTipoProceso", new HashMap<>());
            errorResponse.put("porPrograma", new HashMap<>());
            errorResponse.put("porEstado", new HashMap<>());
            errorResponse.put("fechaConsulta", new Date());
            // Bandera para indicar al frontend que debe usar endpoints alternativos
            errorResponse.put("usarEndpointsAlternativos", true);
            errorResponse.put("error", true);
            errorResponse.put("mensaje", "Error al obtener estadísticas globales. Usar endpoints alternativos: /api/estadisticas/estado-solicitudes, /api/estadisticas/estadisticas-por-proceso, /api/estadisticas/estudiantes-por-programa");
            // Devolver 200 OK para que el frontend pueda parsear el JSON sin problemas
            return ResponseEntity.ok(errorResponse);
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
            
        } catch (Exception e) {
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
        try {
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
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene estadísticas del último período académico.
     * Calcula automáticamente el último período basado en la fecha actual
     * y obtiene las estadísticas correspondientes.
     * 
     * @return ResponseEntity con estadísticas del último período
     */
    @GetMapping("/ultimo-periodo")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasUltimoPeriodo() {
        try {
            
            // Obtener el último período académico
            PeriodoAcademicoEnum ultimoPeriodo = PeriodoAcademicoEnum.getPeriodoActual();
            
            if (ultimoPeriodo == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "No se pudo determinar el último período académico");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Convertir período a fechas
            int año = ultimoPeriodo.getAño();
            int numeroPeriodo = ultimoPeriodo.getNumeroPeriodo();
            
            LocalDate fechaInicioLocal;
            LocalDate fechaFinLocal;
            
            if (numeroPeriodo == 1) {
                // Primer período: enero a junio
                fechaInicioLocal = LocalDate.of(año, 1, 1);
                fechaFinLocal = LocalDate.of(año, 6, 30);
            } else {
                // Segundo período: julio a diciembre
                fechaInicioLocal = LocalDate.of(año, 7, 1);
                fechaFinLocal = LocalDate.of(año, 12, 31);
            }
            
            // Convertir LocalDate a Date
            Date fechaInicio = java.sql.Date.valueOf(fechaInicioLocal);
            Date fechaFin = java.sql.Date.valueOf(fechaFinLocal);
            
            
            // Obtener estadísticas del período
            Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasPorPeriodo(fechaInicio, fechaFin);
            
            // Agregar información del período a la respuesta
            estadisticas.put("periodoAcademico", ultimoPeriodo.getValor());
            estadisticas.put("año", año);
            estadisticas.put("numeroPeriodo", numeroPeriodo);
            estadisticas.put("descripcionPeriodo", ultimoPeriodo.getDescripcion());
            estadisticas.put("fechaInicio", fechaInicioLocal.toString());
            estadisticas.put("fechaFin", fechaFinLocal.toString());
            
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener estadísticas del último período: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return ResponseEntity con resumen completo de estadísticas
     */
    @GetMapping("/resumen-completo")
    public ResponseEntity<Map<String, Object>> obtenerResumenCompleto(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        try {
            // Normalizar el período académico si viene en formato legible
            String periodoNormalizado = normalizarPeriodoAcademico(periodoAcademico);
            
            Map<String, Object> resumen = estadisticaCU.obtenerResumenCompleto(periodoNormalizado, idPrograma);
            return ResponseEntity.ok(resumen);
        } catch (Exception e) {
            log.error("Error al obtener resumen completo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Normaliza el formato del período académico a YYYY-P
     * Acepta formatos como "2025 - Primer Semestre", "2025 segundo semestre", "2025-2", "Segundo Período 2025", etc.
     */
    private String normalizarPeriodoAcademico(String periodo) {
        if (periodo == null || periodo.trim().isEmpty()) {
            return null;
        }
        
        String periodoLimpio = periodo.trim();
        
        // Si ya está en formato YYYY-P, retornarlo
        if (periodoLimpio.matches("\\d{4}-[12]")) {
            return periodoLimpio;
        }
        
        // Intentar extraer año y período de formatos como "2025 - Primer Semestre" o "2025 Segundo Semestre"
        try {
            // Buscar año (4 dígitos)
            java.util.regex.Pattern añoPattern = java.util.regex.Pattern.compile("(\\d{4})");
            java.util.regex.Matcher añoMatcher = añoPattern.matcher(periodoLimpio);
            
            if (añoMatcher.find()) {
                String año = añoMatcher.group(1);
                
                // Buscar indicador de período (case insensitive)
                String periodoLower = periodoLimpio.toLowerCase();
                int numeroPeriodo = 1; // Por defecto primer período
                
                if (periodoLower.contains("segundo") || periodoLower.contains("2") || 
                    periodoLower.contains("ii") || periodoLower.contains("2do") || 
                    periodoLower.contains("2º") || periodoLower.contains("segundo semestre")) {
                    numeroPeriodo = 2;
                } else if (periodoLower.contains("primer") || periodoLower.contains("primero") || 
                          periodoLower.contains("1") || periodoLower.contains("i") || 
                          periodoLower.contains("1er") || periodoLower.contains("1º") ||
                          periodoLower.contains("primer semestre")) {
                    numeroPeriodo = 1;
                }
                
                String periodoNormalizado = año + "-" + numeroPeriodo;
                return periodoNormalizado;
            }
        } catch (Exception e) {
        }
        
        // Si no se pudo normalizar, retornar el original
        return periodoLimpio;
    }
    
    /**
     * Formatea un período académico en formato legible para mostrar en reportes.
     * Convierte "2025-1" a "2025 - Primer Semestre" o "2025-2" a "2025 - Segundo Semestre"
     */
    private String formatearPeriodoAcademicoLegible(String periodo) {
        if (periodo == null || periodo.trim().isEmpty()) {
            return null;
        }
        
        String periodoLimpio = periodo.trim();
        
        // Si ya está en formato legible, retornarlo
        if (periodoLimpio.contains("Primer") || periodoLimpio.contains("Segundo") || 
            periodoLimpio.contains("primer") || periodoLimpio.contains("segundo")) {
            return periodoLimpio;
        }
        
        // Si está en formato YYYY-P, convertirlo
        if (periodoLimpio.matches("\\d{4}-[12]")) {
            String[] partes = periodoLimpio.split("-");
            String año = partes[0];
            String semestre = partes[1].equals("1") ? "Primer Semestre" : "Segundo Semestre";
            return año + " - " + semestre;
        }
        
        // Si no se puede formatear, retornar el original
        return periodoLimpio;
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
    public ResponseEntity<Map<String, Object>> obtenerDashboardEjecutivo(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        try {
            // Normalizar el período académico si viene en formato legible
            String periodoNormalizado = normalizarPeriodoAcademico(periodoAcademico);
            
            Map<String, Object> dashboard = estadisticaCU.obtenerResumenCompleto(periodoNormalizado, idPrograma);
            
            // Extraer solo las métricas más importantes para el dashboard
            Map<String, Object> dashboardEjecutivo = new HashMap<>();
            dashboardEjecutivo.put("resumenGlobal", dashboard.get("estadisticasGlobales"));
            dashboardEjecutivo.put("topProcesos", dashboard.get("porTipoProceso"));
            dashboardEjecutivo.put("resumenEstados", dashboard.get("porEstado"));
            dashboardEjecutivo.put("fechaGeneracion", dashboard.get("fechaGeneracion"));
            
            // Agregar filtros aplicados si existen
            if (periodoNormalizado != null || idPrograma != null) {
                Map<String, Object> filtrosAplicados = new HashMap<>();
                if (periodoNormalizado != null) {
                    filtrosAplicados.put("periodoAcademico", periodoNormalizado);
                }
                if (idPrograma != null) {
                    filtrosAplicados.put("idPrograma", idPrograma);
                }
                dashboardEjecutivo.put("filtrosAplicados", filtrosAplicados);
            }
            
            return ResponseEntity.ok(dashboardEjecutivo);
        } catch (Exception e) {
            log.error("Error al obtener dashboard ejecutivo", e);
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
                    "tiempoPromedioProcesamiento", "N/A"
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
            
            
            // Procesar fechas si están presentes
            Date fechaInicio = null;
            Date fechaFin = null;
            
            if (fechaInicioStr != null && !fechaInicioStr.trim().isEmpty()) {
                try {
                    fechaInicio = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(fechaInicioStr);
                } catch (Exception e) {
                }
            }
            
            if (fechaFinStr != null && !fechaFinStr.trim().isEmpty()) {
                try {
                    fechaFin = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(fechaFinStr);
                } catch (Exception e) {
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
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
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
    public ResponseEntity<Map<String, Object>> obtenerConsolidadoGeneral(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        try {
            
            // Normalizar el período académico si viene en formato legible
            String periodoNormalizado = normalizarPeriodoAcademico(periodoAcademico);
            
            Map<String, Object> estadisticasGlobales;
            
            // Si hay filtros, usar resumen completo
            if (periodoNormalizado != null || idPrograma != null) {
                Map<String, Object> resumen = estadisticaCU.obtenerResumenCompleto(periodoNormalizado, idPrograma);
                @SuppressWarnings("unchecked")
                Map<String, Object> estadisticasGlobalesTemp = (Map<String, Object>) resumen.get("estadisticasGlobales");
                estadisticasGlobales = estadisticasGlobalesTemp;
            } else {
                // Si no hay filtros, usar método original
                estadisticasGlobales = estadisticaCU.obtenerEstadisticasGlobales();
            }
            
            // Crear consolidado con estructura específica
            Map<String, Object> consolidado = new HashMap<>();
            consolidado.put("estadisticasGlobales", estadisticasGlobales);
            consolidado.put("porTipoProceso", estadisticasGlobales != null ? estadisticasGlobales.get("porTipoProceso") : new HashMap<>());
            consolidado.put("porEstado", estadisticasGlobales != null ? estadisticasGlobales.get("porEstado") : new HashMap<>());
            consolidado.put("totalProgramas", 4); // Número de programas
            consolidado.put("fechaGeneracion", new Date());
            
            // Agregar filtros aplicados si existen
            if (periodoNormalizado != null || idPrograma != null) {
                Map<String, Object> filtrosAplicados = new HashMap<>();
                if (periodoNormalizado != null) {
                    filtrosAplicados.put("periodoAcademico", periodoNormalizado);
                }
                if (idPrograma != null) {
                    filtrosAplicados.put("idPrograma", idPrograma);
                }
                consolidado.put("filtrosAplicados", filtrosAplicados);
            }
            
            return ResponseEntity.ok(consolidado);
            
        } catch (Exception e) {
            log.error("Error al obtener consolidado general", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Alias para obtener consolidado general (compatible con pruebas funcionales).
     * 
     * @return ResponseEntity con consolidado general
     */
    @GetMapping("/consolidadas")
    public ResponseEntity<Map<String, Object>> obtenerConsolidadas(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        return obtenerConsolidadoGeneral(periodoAcademico, idPrograma);
    }

    /**
     * Exporta estadísticas generales a PDF (para Dashboard General).
     * 
     * @return ResponseEntity con archivo PDF
     */
    @GetMapping("/export/pdf/general")
    public ResponseEntity<byte[]> exportarEstadisticasGeneralesPDF() {
        try {
            
            // Obtener solo estadísticas generales
            Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasGlobales();
            
            // Generar PDF solo con estadísticas generales
            byte[] pdfBytes = generarPDFEstadisticasGenerales(estadisticas);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "estadisticas_generales_dashboard.pdf");
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Exporta estadísticas de cursos de verano a PDF (para Dashboard Cursos de Verano).
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return ResponseEntity con archivo PDF
     */
    @GetMapping("/export/pdf/cursos-verano")
    public ResponseEntity<byte[]> exportarEstadisticasCursosVeranoPDF(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        try {
            
            // Obtener datos de cursos de verano con filtros
            Map<String, Object> datosCursosVerano = estadisticaCU.obtenerEstadisticasCursosVerano(periodoAcademico, idPrograma);
            
            // Generar PDF solo con datos de cursos de verano
            byte[] pdfBytes = generarPDFCursosVerano(datosCursosVerano, periodoAcademico, idPrograma);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String nombreArchivo = "estadisticas_cursos_verano";
            if (periodoAcademico != null) {
                nombreArchivo += "_" + periodoAcademico.replace("-", "_");
            }
            if (idPrograma != null) {
                nombreArchivo += "_programa_" + idPrograma;
            }
            nombreArchivo += ".pdf";
            headers.setContentDispositionFormData("attachment", nombreArchivo);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Exporta estadísticas a PDF con filtros opcionales (endpoint general).
     * 
     * @param proceso Tipo de proceso (opcional)
     * @param idPrograma ID del programa (opcional)
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param fechaInicio Fecha de inicio (opcional)
     * @param fechaFin Fecha de fin (opcional)
     * @return ResponseEntity con archivo PDF
     */
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportarEstadisticasPDF(
            @RequestParam(required = false) String proceso,
            @RequestParam(required = false) Integer idPrograma,
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin) {
        try {
            // Normalizar período académico si viene en formato legible
            String periodoNormalizado = normalizarPeriodoAcademico(periodoAcademico);
            
            // Si hay período académico, usar obtenerResumenCompleto que soporta este filtro
            Map<String, Object> estadisticas;
            if (periodoNormalizado != null) {
                Map<String, Object> resumen = estadisticaCU.obtenerResumenCompleto(periodoNormalizado, idPrograma);
                @SuppressWarnings("unchecked")
                Map<String, Object> estadisticasGlobales = (Map<String, Object>) resumen.get("estadisticasGlobales");
                estadisticas = estadisticasGlobales != null ? estadisticasGlobales : estadisticaCU.obtenerEstadisticasGlobales(proceso, idPrograma, fechaInicio, fechaFin);
            } else {
                // Si no hay período académico, usar el método original
                estadisticas = estadisticaCU.obtenerEstadisticasGlobales(proceso, idPrograma, fechaInicio, fechaFin);
            }
            
            // Obtener datos de cursos de verano si no hay filtros específicos o si se solicita
            Map<String, Object> datosCursosVerano = null;
            if (proceso == null || "CURSO_VERANO".equals(proceso)) {
                try {
                    datosCursosVerano = estadisticaCU.obtenerEstadisticasCursosVerano(periodoNormalizado, idPrograma);
                } catch (Exception e) {
                }
            }
            
            // Generar PDF con datos completos (pasar período académico para mostrarlo en el reporte)
            byte[] pdfBytes = generarPDFCompleto(estadisticas, datosCursosVerano, periodoAcademico, idPrograma);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "estadisticas_generales.pdf");
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Error al exportar estadísticas a PDF", e);
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
        return exportarEstadisticasPDF(null, null, null, null, null);
    }

    /**
     * Exporta estadísticas generales a Excel (para Dashboard General).
     * 
     * @return ResponseEntity con archivo Excel
     */
    @GetMapping("/export/excel/general")
    public ResponseEntity<byte[]> exportarEstadisticasGeneralesExcel() {
        try {
            
            // Obtener solo estadísticas generales
            Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasGlobales();
            
            // Generar Excel solo con estadísticas generales
            byte[] excelBytes = generarExcelEstadisticasGenerales(estadisticas);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "estadisticas_generales_dashboard.xlsx");
            
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Exporta estadísticas de cursos de verano a Excel (para Dashboard Cursos de Verano).
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return ResponseEntity con archivo Excel
     */
    @GetMapping("/export/excel/cursos-verano")
    public ResponseEntity<byte[]> exportarEstadisticasCursosVeranoExcel(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        try {
            
            // Obtener datos de cursos de verano con filtros
            Map<String, Object> datosCursosVerano = estadisticaCU.obtenerEstadisticasCursosVerano(periodoAcademico, idPrograma);
            
            // Generar Excel solo con datos de cursos de verano
            byte[] excelBytes = generarExcelCursosVerano(datosCursosVerano, periodoAcademico, idPrograma);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String nombreArchivo = "estadisticas_cursos_verano";
            if (periodoAcademico != null) {
                nombreArchivo += "_" + periodoAcademico.replace("-", "_");
            }
            if (idPrograma != null) {
                nombreArchivo += "_programa_" + idPrograma;
            }
            nombreArchivo += ".xlsx";
            headers.setContentDispositionFormData("attachment", nombreArchivo);
            
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Exporta estadísticas a Excel con filtros opcionales (endpoint general).
     * 
     * @param proceso Tipo de proceso (opcional)
     * @param idPrograma ID del programa (opcional)
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param fechaInicio Fecha de inicio (opcional)
     * @param fechaFin Fecha de fin (opcional)
     * @return ResponseEntity con archivo Excel
     */
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportarEstadisticasExcel(
            @RequestParam(required = false) String proceso,
            @RequestParam(required = false) Integer idPrograma,
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaFin) {
        try {
            // Normalizar período académico si viene en formato legible
            String periodoNormalizado = normalizarPeriodoAcademico(periodoAcademico);
            
            // Si hay período académico, usar obtenerResumenCompleto que soporta este filtro
            Map<String, Object> estadisticas;
            if (periodoNormalizado != null) {
                Map<String, Object> resumen = estadisticaCU.obtenerResumenCompleto(periodoNormalizado, idPrograma);
                @SuppressWarnings("unchecked")
                Map<String, Object> estadisticasGlobales = (Map<String, Object>) resumen.get("estadisticasGlobales");
                estadisticas = estadisticasGlobales != null ? estadisticasGlobales : estadisticaCU.obtenerEstadisticasGlobales(proceso, idPrograma, fechaInicio, fechaFin);
            } else {
                // Si no hay período académico, usar el método original
                estadisticas = estadisticaCU.obtenerEstadisticasGlobales(proceso, idPrograma, fechaInicio, fechaFin);
            }
            
            // Obtener datos de cursos de verano si no hay filtros específicos o si se solicita
            Map<String, Object> datosCursosVerano = null;
            if (proceso == null || "CURSO_VERANO".equals(proceso)) {
                try {
                    datosCursosVerano = estadisticaCU.obtenerEstadisticasCursosVerano(periodoNormalizado, idPrograma);
                } catch (Exception e) {
                }
            }
            
            // Generar Excel con datos completos (pasar período académico para mostrarlo en el reporte)
            byte[] excelBytes = generarExcelCompleto(estadisticas, datosCursosVerano, periodoAcademico, idPrograma);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "estadisticas_generales.xlsx");
            
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Error al exportar estadísticas a Excel", e);
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
            
            // Obtener datos filtrados
            Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasGlobales(null, null, null, null);
            
            // Obtener datos de cursos de verano
            Map<String, Object> datosCursosVerano = null;
            try {
                datosCursosVerano = estadisticaCU.obtenerEstadisticasCursosVerano(null, null);
            } catch (Exception e) {
            }
            
            // Generar Excel con datos completos (sin filtros)
            byte[] excelBytes = generarExcelCompleto(estadisticas, datosCursosVerano, null, null);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "estadisticas_generales.xlsx");
            
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
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
            }
        }
    }

    /**
     * Genera un PDF solo con estadísticas de cursos de verano (para Dashboard Cursos de Verano).
     * 
     * @param datosCursosVerano Datos de cursos de verano
     * @param periodoAcademico Período académico aplicado como filtro (opcional)
     * @param idPrograma ID del programa aplicado como filtro (opcional)
     * @return Array de bytes del PDF
     */
    private byte[] generarPDFCursosVerano(Map<String, Object> datosCursosVerano, String periodoAcademico, Integer idPrograma) {
        
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
            
            // Fecha de generación con formato mejorado
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
            com.itextpdf.text.Font dateFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
            String fechaTexto = "Fecha de generación: " + dateFormat.format(new Date());
            com.itextpdf.text.Paragraph fecha = new com.itextpdf.text.Paragraph(fechaTexto, dateFont);
            fecha.setSpacingAfter(15);
            document.add(fecha);
            
            // Información de filtros aplicados (si existen)
            if (periodoAcademico != null || idPrograma != null) {
                com.itextpdf.text.Font filterFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.ITALIC);
                StringBuilder filtrosTexto = new StringBuilder("Filtros aplicados: ");
                if (periodoAcademico != null) {
                    filtrosTexto.append("Período Académico: ").append(periodoAcademico);
                }
                if (idPrograma != null) {
                    if (periodoAcademico != null) {
                        filtrosTexto.append(" | ");
                    }
                    filtrosTexto.append("Programa ID: ").append(idPrograma);
                }
                com.itextpdf.text.Paragraph filtros = new com.itextpdf.text.Paragraph(filtrosTexto.toString(), filterFont);
                filtros.setSpacingAfter(10);
                document.add(filtros);
            }
            
            // Sección: Estadísticas de Cursos de Verano
            agregarSeccionCursosVerano(document, datosCursosVerano);
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            
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
            }
        }
    }

    /**
     * Genera un PDF completo con estadísticas generales y de cursos de verano.
     * 
     * @param estadisticas Datos de estadísticas generales
     * @param datosCursosVerano Datos de cursos de verano
     * @param periodoAcademico Período académico filtrado (opcional)
     * @param idPrograma ID del programa filtrado (opcional)
     * @return Array de bytes del PDF
     */
    private byte[] generarPDFCompleto(Map<String, Object> estadisticas, Map<String, Object> datosCursosVerano, String periodoAcademico, Integer idPrograma) {
        
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
            
            // Fecha de generación con formato mejorado (en español)
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd 'de' MMMM 'de' yyyy 'a las' HH:mm:ss", new java.util.Locale("es", "CO"));
            com.itextpdf.text.Font dateFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10);
            String fechaTexto = "Fecha de generación: " + dateFormat.format(new Date());
            com.itextpdf.text.Paragraph fecha = new com.itextpdf.text.Paragraph(fechaTexto, dateFont);
            fecha.setSpacingAfter(10);
            document.add(fecha);
            
            // Información de filtros aplicados
            if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
                String periodoFormateado = formatearPeriodoAcademicoLegible(periodoAcademico);
                com.itextpdf.text.Paragraph periodo = new com.itextpdf.text.Paragraph("Período académico filtrado: " + periodoFormateado, dateFont);
                periodo.setSpacingAfter(10);
                document.add(periodo);
            }
            
            if (idPrograma != null) {
                // Intentar obtener el nombre del programa desde las estadísticas
                String nombrePrograma = null;
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> porPrograma = (Map<String, Object>) estadisticas.get("porPrograma");
                    if (porPrograma != null) {
                        // Buscar el programa en el mapa (las claves son los nombres)
                        for (String nombre : porPrograma.keySet()) {
                            // Si solo hay un programa en el mapa y hay filtro, es probable que sea el filtrado
                            if (porPrograma.size() == 1) {
                                nombrePrograma = nombre;
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                }
                
                if (nombrePrograma != null) {
                    com.itextpdf.text.Paragraph programaFiltro = new com.itextpdf.text.Paragraph("Programa filtrado: " + nombrePrograma, dateFont);
                    programaFiltro.setSpacingAfter(15);
                    document.add(programaFiltro);
                } else {
                    com.itextpdf.text.Paragraph programaFiltro = new com.itextpdf.text.Paragraph("Programa filtrado: ID " + idPrograma, dateFont);
                    programaFiltro.setSpacingAfter(15);
                    document.add(programaFiltro);
                }
            }
            
            if (periodoAcademico == null && idPrograma == null) {
                // Espacio adicional si no hay filtros
                com.itextpdf.text.Paragraph espacio = new com.itextpdf.text.Paragraph(" ");
                espacio.setSpacingAfter(15);
                document.add(espacio);
            }
            
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
            
            Map<String, Object> resultado = estadisticaCU.obtenerNumeroTotalEstudiantes();
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene la distribución de estudiantes por programa académico.
     * Utiliza UsuarioRepositoryInt para contar estudiantes por programa.
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return ResponseEntity con la distribución de estudiantes por programa
     */
    @GetMapping("/estudiantes-por-programa")
    public ResponseEntity<Map<String, Object>> obtenerEstudiantesPorPrograma(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        try {
            String periodoNormalizado = normalizarPeriodoAcademico(periodoAcademico);
            Map<String, Object> resultado = estadisticaCU.obtenerEstudiantesPorPrograma(periodoNormalizado, idPrograma);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al obtener estudiantes por programa", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
        try {
            String periodoNormalizado = normalizarPeriodoAcademico(periodoAcademico);
            Map<String, Object> resultado = estadisticaCU.obtenerEstadisticasDetalladasPorProceso(periodoNormalizado, idPrograma);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al obtener estadísticas detalladas por proceso - Período: '{}', Programa: {}", periodoAcademico, idPrograma, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al obtener estadísticas por proceso");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtiene estadísticas resumidas por tipo de proceso para el dashboard.
     * Formato optimizado para gráficos y KPIs.
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return ResponseEntity con estadísticas resumidas por proceso
     */
    @GetMapping("/resumen-por-proceso")
    public ResponseEntity<Map<String, Object>> obtenerResumenPorProceso(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        try {
            String periodoNormalizado = normalizarPeriodoAcademico(periodoAcademico);
            Map<String, Object> resultado = estadisticaCU.obtenerResumenPorProceso(periodoNormalizado, idPrograma);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al obtener resumen por proceso", e);
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
            
            Map<String, Object> resultado = estadisticaCU.obtenerConfiguracionEstilos();
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
        try {
            String periodoNormalizado = normalizarPeriodoAcademico(periodoAcademico);
            Map<String, Object> resultado = estadisticaCU.obtenerEstadisticasPorEstado(periodoNormalizado, idPrograma);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al obtener estadísticas por estado", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
        try {
            String periodoNormalizado = normalizarPeriodoAcademico(periodoAcademico);
            Map<String, Object> resultado = estadisticaCU.obtenerEstadisticasPorPeriodo(periodoNormalizado, idPrograma);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al obtener estadísticas por período - Período: '{}', Programa: {}", periodoAcademico, idPrograma, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al obtener estadísticas por período");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
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
        try {
            String periodoNormalizado = normalizarPeriodoAcademico(periodoAcademico);
            Map<String, Object> resultado = estadisticaCU.obtenerEstadisticasPorPrograma(periodoNormalizado, idPrograma);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al obtener estadísticas por programa - Período: '{}', Programa: {}", periodoAcademico, idPrograma, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al obtener estadísticas por programa");
            error.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
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
            
            Map<String, Object> resultado = estadisticaCU.obtenerTiempoPromedioProcesamiento();
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
        try {
            String periodoNormalizado = normalizarPeriodoAcademico(periodoAcademico);
            Map<String, Object> resultado = estadisticaCU.obtenerTendenciasYComparativas(periodoNormalizado, idPrograma);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al obtener tendencias y comparativas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint de prueba simple para verificar ECAES
     */
    @GetMapping("/test-ecaes")
    public ResponseEntity<Map<String, Object>> testEcaes() {
        try {
            
            Map<String, Object> resultado = new HashMap<>();
            
            // Probar datos básicos de ECAES
            resultado.put("mensaje", "Test ECAES exitoso");
            resultado.put("fecha", new Date());
            resultado.put("procesos", Arrays.asList("Homologación", "Paz y Salvo", "Reingreso", "Cursos de Verano", "ECAES"));
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
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
            
            Map<String, Object> resultado = estadisticaCU.obtenerEstadisticasPorProceso("ECAES");
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
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
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return ResponseEntity con estadísticas detalladas de cursos de verano
     */
    @GetMapping("/cursos-verano")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasCursosVerano(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        try {
            
            Map<String, Object> resultado = estadisticaCU.obtenerEstadisticasCursosVerano(periodoAcademico, idPrograma);
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint optimizado para obtener solo las tendencias temporales de cursos de verano.
     * 
     * @param periodoAcademico Período académico opcional (formato: "YYYY-P", ej: "2025-2")
     * @param idPrograma ID del programa académico opcional para filtrar
     * @return ResponseEntity con tendencias temporales
     */
    @GetMapping("/cursos-verano/tendencias-temporales")
    public ResponseEntity<Map<String, Object>> obtenerTendenciasTemporalesCursosVerano(
            @RequestParam(required = false) String periodoAcademico,
            @RequestParam(required = false) Integer idPrograma) {
        try {
            
            // Obtener solo las tendencias temporales de manera optimizada
            Map<String, Object> tendencias = estadisticaCU.obtenerTendenciasTemporalesCursosVerano(periodoAcademico, idPrograma);
            
            return ResponseEntity.ok(tendencias);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint alternativo para estadísticas por proceso que funciona
     */
    @GetMapping("/por-proceso-funcional")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorProcesoFuncional() {
        try {
            
            Map<String, Object> resultado = new HashMap<>();
            
            // Obtener datos de cada proceso individualmente
            String[] procesos = {"Homologación", "Paz y Salvo", "Reingreso", "Cursos de Verano", "ECAES"};
            Map<String, Object> procesosDetallados = new HashMap<>();
            
            for (String proceso : procesos) {
                try {
                    Map<String, Object> datosProceso = estadisticaCU.obtenerEstadisticasPorProceso(proceso);
                    procesosDetallados.put(proceso, datosProceso);
                } catch (Exception e) {
                    // Continuar con los otros procesos
                }
            }
            
            resultado.put("procesos", procesosDetallados);
            resultado.put("totalProcesos", procesosDetallados.size());
            resultado.put("fechaConsulta", new Date());
            resultado.put("descripcion", "Estadísticas por proceso - Versión funcional");
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
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
        com.itextpdf.text.Font boldFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);
        
        // Resumen general
        Object totalSolicitudes = estadisticas.get("totalSolicitudes");
        Object totalAprobadas = estadisticas.get("totalAprobadas");
        Object totalRechazadas = estadisticas.get("totalRechazadas");
        Object totalEnProceso = estadisticas.get("totalEnProceso");
        Object porcentajeAprobacion = estadisticas.get("porcentajeAprobacion");
        
        if (totalSolicitudes != null) {
            document.add(new com.itextpdf.text.Paragraph("Total de Solicitudes: " + totalSolicitudes, boldFont));
        }
        
        if (totalAprobadas != null || totalRechazadas != null || totalEnProceso != null) {
            document.add(new com.itextpdf.text.Paragraph("\nResumen de Estados:", boldFont));
            if (totalAprobadas != null) {
                document.add(new com.itextpdf.text.Paragraph("  • Total Aprobadas: " + totalAprobadas, normalFont));
            }
            if (totalRechazadas != null) {
                document.add(new com.itextpdf.text.Paragraph("  • Total Rechazadas: " + totalRechazadas, normalFont));
            }
            if (totalEnProceso != null) {
                document.add(new com.itextpdf.text.Paragraph("  • Total en Proceso: " + totalEnProceso, normalFont));
            }
            if (porcentajeAprobacion != null) {
                document.add(new com.itextpdf.text.Paragraph("  • Porcentaje de Aprobación: " + porcentajeAprobacion + "%", normalFont));
            }
        }
        
        // Por estado (detallado)
        @SuppressWarnings("unchecked")
        Map<String, Object> porEstado = (Map<String, Object>) estadisticas.get("porEstado");
        if (porEstado != null && !porEstado.isEmpty()) {
            document.add(new com.itextpdf.text.Paragraph("\nEstadísticas por Estado:", boldFont));
            for (Map.Entry<String, Object> entry : porEstado.entrySet()) {
                document.add(new com.itextpdf.text.Paragraph("  • " + entry.getKey() + ": " + entry.getValue(), normalFont));
            }
        }
        
        // Por tipo de proceso
        @SuppressWarnings("unchecked")
        Map<String, Object> porTipoProceso = (Map<String, Object>) estadisticas.get("porTipoProceso");
        if (porTipoProceso != null && !porTipoProceso.isEmpty()) {
            document.add(new com.itextpdf.text.Paragraph("\nEstadísticas por Tipo de Proceso:", boldFont));
            for (Map.Entry<String, Object> entry : porTipoProceso.entrySet()) {
                document.add(new com.itextpdf.text.Paragraph("  • " + entry.getKey() + ": " + entry.getValue(), normalFont));
            }
        }
        
        // Por programa
        @SuppressWarnings("unchecked")
        Map<String, Object> porPrograma = (Map<String, Object>) estadisticas.get("porPrograma");
        if (porPrograma != null && !porPrograma.isEmpty()) {
            document.add(new com.itextpdf.text.Paragraph("\nEstadísticas por Programa:", boldFont));
            for (Map.Entry<String, Object> entry : porPrograma.entrySet()) {
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
            Object demandaEstimada = predicciones.get("demandaEstimadaProximoPeriodo");
            if (demandaEstimada != null) {
                document.add(new com.itextpdf.text.Paragraph("  • Demanda Estimada Próximo Período: " + demandaEstimada, normalFont));
            }
            Object confiabilidad = predicciones.get("confiabilidad");
            if (confiabilidad != null) {
                document.add(new com.itextpdf.text.Paragraph("  • Confiabilidad: " + confiabilidad, normalFont));
            }
            Object metodologia = predicciones.get("metodologia");
            if (metodologia != null && !"null".equals(String.valueOf(metodologia))) {
                document.add(new com.itextpdf.text.Paragraph("  • Metodología: " + metodologia, normalFont));
            }
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
     * 
     * @param estadisticas Datos de estadísticas generales
     * @param datosCursosVerano Datos de cursos de verano
     * @param periodoAcademico Período académico filtrado (opcional)
     * @param idPrograma ID del programa filtrado (opcional)
     */
    private byte[] generarExcelCompleto(Map<String, Object> estadisticas, Map<String, Object> datosCursosVerano, String periodoAcademico, Integer idPrograma) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            
            // Hoja 1: Estadísticas Generales
            if (estadisticas != null) {
                crearHojaEstadisticasGenerales(workbook, estadisticas, periodoAcademico, idPrograma);
            }
            
            // Hoja 2: Cursos de Verano
            if (datosCursosVerano != null) {
                crearHojaCursosVerano(workbook, datosCursosVerano, periodoAcademico, idPrograma);
            }
            
            workbook.write(baos);
            workbook.close();
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            
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
                return new byte[0]; // Devolver array vacío en caso de error crítico
            }
        }
    }

    /**
     * Crea la hoja de estadísticas generales en el Excel.
     */
    private void crearHojaEstadisticasGenerales(org.apache.poi.xssf.usermodel.XSSFWorkbook workbook, Map<String, Object> estadisticas, String periodoAcademico, Integer idPrograma) {
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
            
            org.apache.poi.ss.usermodel.CellStyle infoStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font infoFont = workbook.createFont();
            infoFont.setItalic(true);
            infoFont.setFontHeightInPoints((short) 10);
            infoStyle.setFont(infoFont);
            
            int rowNum = 0;
            
            // Título
            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("ESTADÍSTICAS GENERALES DEL SISTEMA");
            titleCell.setCellStyle(titleStyle);
            
        rowNum++; // Espacio
        
        // Fecha de generación
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd 'de' MMMM 'de' yyyy 'a las' HH:mm:ss", new java.util.Locale("es", "CO"));
        org.apache.poi.ss.usermodel.Row fechaRow = sheet.createRow(rowNum++);
        fechaRow.createCell(0).setCellValue("Fecha de generación: " + dateFormat.format(new Date()));
        fechaRow.getCell(0).setCellStyle(infoStyle);
        
        // Información de filtros aplicados
        if (periodoAcademico != null && !periodoAcademico.trim().isEmpty()) {
            String periodoFormateado = formatearPeriodoAcademicoLegible(periodoAcademico);
            org.apache.poi.ss.usermodel.Row periodoRow = sheet.createRow(rowNum++);
            periodoRow.createCell(0).setCellValue("Período académico filtrado: " + periodoFormateado);
            periodoRow.getCell(0).setCellStyle(infoStyle);
        }
        
        if (idPrograma != null) {
            // Intentar obtener el nombre del programa desde las estadísticas
            String nombrePrograma = null;
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> porPrograma = (Map<String, Object>) estadisticas.get("porPrograma");
                if (porPrograma != null && porPrograma.size() == 1) {
                    nombrePrograma = porPrograma.keySet().iterator().next();
                }
            } catch (Exception e) {
            }
            
            org.apache.poi.ss.usermodel.Row programaRow = sheet.createRow(rowNum++);
            String textoPrograma = nombrePrograma != null ? 
                "Programa filtrado: " + nombrePrograma : 
                "Programa filtrado: ID " + idPrograma;
            programaRow.createCell(0).setCellValue(textoPrograma);
            programaRow.getCell(0).setCellStyle(infoStyle);
        }
        
        rowNum++; // Espacio
        
        // Total de solicitudes
        Object totalSolicitudes = estadisticas.get("totalSolicitudes");
        if (totalSolicitudes != null) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("Total de Solicitudes");
            row.createCell(1).setCellValue(totalSolicitudes.toString());
        }
        
        // Resumen de estados
        Object totalAprobadas = estadisticas.get("totalAprobadas");
        Object totalRechazadas = estadisticas.get("totalRechazadas");
        Object totalEnProceso = estadisticas.get("totalEnProceso");
        Object totalEnviadas = estadisticas.get("totalEnviadas");
        Object porcentajeAprobacion = estadisticas.get("porcentajeAprobacion");
        
        if (totalAprobadas != null || totalRechazadas != null || totalEnProceso != null || totalEnviadas != null) {
            rowNum++; // Espacio
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("RESUMEN DE ESTADOS");
            headerCell.setCellStyle(headerStyle);
            
            if (totalAprobadas != null) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("Total Aprobadas");
                row.createCell(1).setCellValue(totalAprobadas.toString());
            }
            if (totalRechazadas != null) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("Total Rechazadas");
                row.createCell(1).setCellValue(totalRechazadas.toString());
            }
            if (totalEnProceso != null) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("Total en Proceso");
                row.createCell(1).setCellValue(totalEnProceso.toString());
            }
            if (totalEnviadas != null) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("Total Enviadas");
                row.createCell(1).setCellValue(totalEnviadas.toString());
            }
            if (porcentajeAprobacion != null) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("Porcentaje de Aprobación");
                row.createCell(1).setCellValue(porcentajeAprobacion.toString() + "%");
            }
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
        
        // Por programa
        @SuppressWarnings("unchecked")
        Map<String, Object> porPrograma = (Map<String, Object>) estadisticas.get("porPrograma");
        if (porPrograma != null && !porPrograma.isEmpty()) {
            rowNum++; // Espacio
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("ESTADÍSTICAS POR PROGRAMA");
            headerCell.setCellStyle(headerStyle);
            
            for (Map.Entry<String, Object> entry : porPrograma.entrySet()) {
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
    private void crearHojaCursosVerano(org.apache.poi.xssf.usermodel.XSSFWorkbook workbook, Map<String, Object> datosCursosVerano, String periodoAcademico, Integer idPrograma) {
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
        
        org.apache.poi.ss.usermodel.CellStyle filterStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font filterFont = workbook.createFont();
        filterFont.setItalic(true);
        filterFont.setFontHeightInPoints((short) 10);
        filterStyle.setFont(filterFont);
        
        int rowNum = 0;
        
        // Título
        org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("ESTADÍSTICAS DE CURSOS DE VERANO");
        titleCell.setCellStyle(titleStyle);
        
        // Fecha de generación
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        org.apache.poi.ss.usermodel.Row fechaRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell fechaCell = fechaRow.createCell(0);
        fechaCell.setCellValue("Fecha de generación: " + dateFormat.format(new Date()));
        
        // Información de filtros aplicados (si existen)
        if (periodoAcademico != null || idPrograma != null) {
            rowNum++; // Espacio
            org.apache.poi.ss.usermodel.Row filterRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell filterCell = filterRow.createCell(0);
            StringBuilder filtrosTexto = new StringBuilder("Filtros aplicados: ");
            if (periodoAcademico != null) {
                filtrosTexto.append("Período Académico: ").append(periodoAcademico);
            }
            if (idPrograma != null) {
                if (periodoAcademico != null) {
                    filtrosTexto.append(" | ");
                }
                filtrosTexto.append("Programa ID: ").append(idPrograma);
            }
            filterCell.setCellValue(filtrosTexto.toString());
            filterCell.setCellStyle(filterStyle);
        }
        
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
     * Obtiene estadísticas agrupadas por año.
     * 
     * @param anio Año a consultar
     * @return ResponseEntity con estadísticas del año
     */
    @GetMapping("/por-anio/{anio}")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorAnio(
            @PathVariable Integer anio) {
        try {
            Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasPorAnio(anio);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene estadísticas agrupadas por semestre.
     * 
     * @param anio Año a consultar
     * @param semestre Número de semestre (1 o 2)
     * @return ResponseEntity con estadísticas del semestre
     */
    @GetMapping("/por-semestre")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorSemestre(
            @RequestParam(name = "anio", required = true) Integer anio,
            @RequestParam(name = "semestre", required = true) Integer semestre) {
        try {
            Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasPorSemestre(anio, semestre);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
        try {
            List<Map<String, Object>> historial = estadisticaCU.obtenerHistorialEstadisticas(anioInicio, anioFin);
            return ResponseEntity.ok(historial);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene estadísticas por período académico usando formato YYYY-P.
     * 
     * @param periodoAcademico Período en formato "YYYY-P" (ej: "2024-2")
     * @return ResponseEntity con estadísticas del período
     */
    @GetMapping("/periodo-academico/{periodoAcademico}")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorPeriodoAcademico(
            @PathVariable String periodoAcademico) {
        try {
            Map<String, Object> estadisticas = estadisticaCU.obtenerEstadisticasPorPeriodoAcademico(periodoAcademico);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
            
            // Hoja: Estadísticas Generales (sin filtros para este método)
            crearHojaEstadisticasGenerales(workbook, estadisticas, null, null);
            
            workbook.write(baos);
            workbook.close();
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            
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
                return new byte[0];
            }
        }
    }

    /**
     * Genera un Excel solo con estadísticas de cursos de verano (para Dashboard Cursos de Verano).
     * 
     * @param datosCursosVerano Datos de cursos de verano
     * @param periodoAcademico Período académico aplicado como filtro (opcional)
     * @param idPrograma ID del programa aplicado como filtro (opcional)
     * @return Array de bytes del Excel
     */
    private byte[] generarExcelCursosVerano(Map<String, Object> datosCursosVerano, String periodoAcademico, Integer idPrograma) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            
            // Hoja: Cursos de Verano
            crearHojaCursosVerano(workbook, datosCursosVerano, periodoAcademico, idPrograma);
            
            workbook.write(baos);
            workbook.close();
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            
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
                return new byte[0];
            }
        }
    }
}

