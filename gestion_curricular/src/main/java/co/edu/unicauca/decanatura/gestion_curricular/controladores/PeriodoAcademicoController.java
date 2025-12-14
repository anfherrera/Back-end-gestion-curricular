package co.edu.unicauca.decanatura.gestion_curricular.controladores;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.PeriodoAcademicoEnum;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.PeriodoAcademico;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarPeriodoAcademicoCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarConfiguracionSistemaCUIntPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador para manejar operaciones relacionadas con períodos académicos
 * 
 * @author Sistema de Gestión Curricular
 * @version 2.0 - Actualizado para usar servicio de BD
 */
@RestController
@RequestMapping("/api/periodos-academicos")
@RequiredArgsConstructor
@Slf4j
public class PeriodoAcademicoController {
    
    private final GestionarPeriodoAcademicoCUIntPort periodoAcademicoCU;
    private final GestionarConfiguracionSistemaCUIntPort configuracionCU;

    /**
     * Obtiene todos los períodos académicos disponibles
     * Útil para mostrar un selector de períodos en el frontend
     * 
     * @return Lista de todos los períodos académicos
     */
    @GetMapping("/todos")
    public ResponseEntity<Map<String, Object>> obtenerTodosLosPeriodos() {
        try {
            // Intentar obtener desde BD
            List<PeriodoAcademico> periodosBD = periodoAcademicoCU.listarPeriodosActivos();
            
            List<Map<String, Object>> periodosData;
            
            if (periodosBD != null && !periodosBD.isEmpty()) {
                // Usar períodos desde BD
                periodosData = periodosBD.stream()
                    .map(p -> {
                        Map<String, Object> periodo = new HashMap<>();
                        periodo.put("valor", p.getValor());
                        periodo.put("año", p.getAño());
                        periodo.put("numeroPeriodo", p.getNumero_periodo());
                        periodo.put("nombrePeriodo", p.getNombre_periodo());
                        periodo.put("fechaInicio", p.getFecha_inicio().toString());
                        periodo.put("fechaFin", p.getFecha_fin().toString());
                        periodo.put("activo", p.getActivo());
                        periodo.put("tipoPeriodo", p.getTipo_periodo());
                        return periodo;
                    })
                    .collect(Collectors.toList());
                log.debug("Períodos obtenidos desde BD: {}", periodosData.size());
            } else {
                // Fallback al enum
                String[] periodosEnum = PeriodoAcademicoEnum.getTodosLosValores();
                periodosData = java.util.Arrays.stream(periodosEnum)
                    .map(valor -> {
                        Map<String, Object> periodo = new HashMap<>();
                        periodo.put("valor", valor);
                        String[] partes = valor.split("-");
                        if (partes.length == 2) {
                            periodo.put("año", Integer.parseInt(partes[0]));
                            periodo.put("numeroPeriodo", Integer.parseInt(partes[1]));
                        }
                        return periodo;
                    })
                    .collect(Collectors.toList());
                log.debug("Períodos obtenidos desde enum (fallback): {}", periodosData.size());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", periodosData);
            response.put("message", "Períodos académicos obtenidos exitosamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al obtener períodos académicos: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener períodos académicos: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Obtiene los períodos académicos futuros (año actual en adelante)
     * Útil para mostrar períodos disponibles para selección
     * 
     * @return Lista de períodos académicos futuros
     */
    @GetMapping("/futuros")
    public ResponseEntity<Map<String, Object>> obtenerPeriodosFuturos() {
        try {
            // Intentar obtener desde BD
            List<PeriodoAcademico> periodosFuturos = periodoAcademicoCU.listarPeriodosFuturos();
            
            List<Map<String, Object>> periodosData;
            
            if (periodosFuturos != null && !periodosFuturos.isEmpty()) {
                periodosData = periodosFuturos.stream()
                    .map(p -> {
                        Map<String, Object> periodo = new HashMap<>();
                        periodo.put("valor", p.getValor());
                        periodo.put("año", p.getAño());
                        periodo.put("numeroPeriodo", p.getNumero_periodo());
                        periodo.put("nombrePeriodo", p.getNombre_periodo());
                        periodo.put("fechaInicio", p.getFecha_inicio().toString());
                        periodo.put("fechaFin", p.getFecha_fin().toString());
                        return periodo;
                    })
                    .collect(Collectors.toList());
            } else {
                // Fallback al enum
                String[] periodosEnum = PeriodoAcademicoEnum.getPeriodosFuturos();
                periodosData = java.util.Arrays.stream(periodosEnum)
                    .map(valor -> {
                        Map<String, Object> periodo = new HashMap<>();
                        periodo.put("valor", valor);
                        return periodo;
                    })
                    .collect(Collectors.toList());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", periodosData);
            response.put("message", "Períodos académicos futuros obtenidos exitosamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al obtener períodos futuros: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener períodos futuros: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Obtiene los períodos académicos recientes (últimos 5 años)
     * Útil para mostrar períodos históricos en selectores
     * 
     * @return Lista de períodos académicos recientes
     */
    @GetMapping("/recientes")
    public ResponseEntity<Map<String, Object>> obtenerPeriodosRecientes() {
        try {
            // Intentar obtener desde BD
            List<PeriodoAcademico> periodosRecientes = periodoAcademicoCU.listarPeriodosRecientes();
            
            List<Map<String, Object>> periodosData;
            
            if (periodosRecientes != null && !periodosRecientes.isEmpty()) {
                periodosData = periodosRecientes.stream()
                    .map(p -> {
                        Map<String, Object> periodo = new HashMap<>();
                        periodo.put("valor", p.getValor());
                        periodo.put("año", p.getAño());
                        periodo.put("numeroPeriodo", p.getNumero_periodo());
                        periodo.put("nombrePeriodo", p.getNombre_periodo());
                        periodo.put("fechaInicio", p.getFecha_inicio().toString());
                        periodo.put("fechaFin", p.getFecha_fin().toString());
                        return periodo;
                    })
                    .collect(Collectors.toList());
            } else {
                // Fallback al enum
                String[] periodosEnum = PeriodoAcademicoEnum.getPeriodosRecientes();
                periodosData = java.util.Arrays.stream(periodosEnum)
                    .map(valor -> {
                        Map<String, Object> periodo = new HashMap<>();
                        periodo.put("valor", valor);
                        return periodo;
                    })
                    .collect(Collectors.toList());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", periodosData);
            response.put("message", "Períodos académicos recientes obtenidos exitosamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al obtener períodos recientes: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener períodos recientes: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Obtiene el período académico actual con información completa
     * Este es el endpoint principal que el frontend debe llamar al iniciar
     * 
     * @return El período académico actual con toda su información
     */
    @GetMapping("/actual")
    public ResponseEntity<Map<String, Object>> obtenerPeriodoActual() {
        try {
            // Intentar obtener desde BD (con fallback automático al enum)
            Optional<PeriodoAcademico> periodoOpt = periodoAcademicoCU.obtenerPeriodoActual();
            
            Map<String, Object> response = new HashMap<>();
            
            if (periodoOpt.isPresent()) {
                PeriodoAcademico periodo = periodoOpt.get();
                
                Map<String, Object> data = new HashMap<>();
                data.put("valor", periodo.getValor());
                data.put("año", periodo.getAño());
                data.put("numeroPeriodo", periodo.getNumero_periodo());
                data.put("nombrePeriodo", periodo.getNombre_periodo());
                data.put("fechaInicio", periodo.getFecha_inicio().toString());
                data.put("fechaFin", periodo.getFecha_fin().toString());
                data.put("fechaInicioClases", periodo.getFecha_inicio_clases() != null ? periodo.getFecha_inicio_clases().toString() : null);
                data.put("fechaFinClases", periodo.getFecha_fin_clases() != null ? periodo.getFecha_fin_clases().toString() : null);
                data.put("activo", periodo.getActivo());
                data.put("tipoPeriodo", periodo.getTipo_periodo());
                data.put("esPeriodoEspecial", periodo.getEs_periodo_especial());
                
                // Descripción completa
                String descripcion = periodo.getNombre_periodo() != null && !periodo.getNombre_periodo().trim().isEmpty() 
                    ? periodo.getNombre_periodo() 
                    : (periodo.getNumero_periodo() == 1 ? "Primer Período" : "Segundo Período") + " " + periodo.getAño();
                data.put("descripcion", descripcion);
                
                response.put("success", true);
                response.put("data", data);
                response.put("message", "Período actual obtenido exitosamente desde BD");
                log.debug("Período actual obtenido desde BD: {}", periodo.getValor());
            } else {
                // Fallback al enum
                PeriodoAcademicoEnum periodoEnum = PeriodoAcademicoEnum.getPeriodoActual();
                if (periodoEnum != null) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("valor", periodoEnum.getValor());
                    data.put("año", periodoEnum.getAño());
                    data.put("numeroPeriodo", periodoEnum.getNumeroPeriodo());
                    data.put("descripcion", periodoEnum.getDescripcion());
                    data.put("fechaInicio", periodoEnum.getFechaInicio().toString());
                    data.put("fechaFin", periodoEnum.getFechaFin().toString());
                    data.put("activo", true);
                    data.put("tipoPeriodo", "REGULAR");
                    data.put("esPeriodoEspecial", false);
                    
                    response.put("success", true);
                    response.put("data", data);
                    response.put("message", "Período actual obtenido exitosamente (fallback enum)");
                    log.debug("Período actual obtenido desde enum (fallback): {}", periodoEnum.getValor());
                } else {
                    response.put("success", false);
                    response.put("data", null);
                    response.put("message", "No se pudo determinar el período actual");
                }
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al obtener período actual: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener período actual: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Valida si un período académico es válido
     * 
     * @param periodo El período a validar
     * @return true si es válido, false en caso contrario
     */
    @GetMapping("/validar/{periodo}")
    public ResponseEntity<Map<String, Object>> validarPeriodo(@PathVariable String periodo) {
        try {
            boolean esValido = PeriodoAcademicoEnum.esValido(periodo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", esValido);
            response.put("message", esValido ? 
                "Período válido" : 
                "Período no válido");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al validar período: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Obtiene información detallada de un período académico
     * 
     * @param periodo El período del cual obtener información (formato: "2025-1")
     * @return Información detallada del período con fechas reales
     */
    @GetMapping("/info/{periodo}")
    public ResponseEntity<Map<String, Object>> obtenerInformacionPeriodo(@PathVariable String periodo) {
        try {
            // Intentar obtener desde BD (con fallback automático al enum)
            Optional<PeriodoAcademico> periodoOpt = periodoAcademicoCU.obtenerPeriodoPorValor(periodo);
            
            Map<String, Object> info;
            
            if (periodoOpt.isPresent()) {
                // Usar información desde BD con fechas reales
                PeriodoAcademico p = periodoOpt.get();
                info = new HashMap<>();
                info.put("valor", p.getValor());
                info.put("año", p.getAño());
                info.put("numeroPeriodo", p.getNumero_periodo());
                info.put("nombrePeriodo", p.getNombre_periodo());
                info.put("fechaInicio", p.getFecha_inicio().toString());
                info.put("fechaFin", p.getFecha_fin().toString());
                info.put("fechaInicioClases", p.getFecha_inicio_clases() != null ? p.getFecha_inicio_clases().toString() : null);
                info.put("fechaFinClases", p.getFecha_fin_clases() != null ? p.getFecha_fin_clases().toString() : null);
                info.put("activo", p.getActivo());
                info.put("tipoPeriodo", p.getTipo_periodo());
                info.put("esPeriodoEspecial", p.getEs_periodo_especial());
                
                String descripcion = p.getNombre_periodo() != null && !p.getNombre_periodo().trim().isEmpty() 
                    ? p.getNombre_periodo() 
                    : (p.getNumero_periodo() == 1 ? "Primer Período" : "Segundo Período") + " " + p.getAño();
                info.put("descripcion", descripcion);
            } else {
                // Fallback al enum
                if (!PeriodoAcademicoEnum.esValido(periodo)) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "Período académico no válido: " + periodo);
                    
                    return ResponseEntity.badRequest().body(errorResponse);
                }
                
                PeriodoAcademicoEnum periodoEnum = PeriodoAcademicoEnum.fromValor(periodo);
                info = new HashMap<>();
                info.put("valor", periodoEnum.getValor());
                info.put("año", periodoEnum.getAño());
                info.put("numeroPeriodo", periodoEnum.getNumeroPeriodo());
                info.put("descripcion", periodoEnum.getDescripcion());
                info.put("fechaInicio", periodoEnum.getFechaInicio().toString());
                info.put("fechaFin", periodoEnum.getFechaFin().toString());
                info.put("activo", true);
                info.put("tipoPeriodo", "REGULAR");
                info.put("esPeriodoEspecial", false);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", info);
            response.put("message", "Información del período obtenida exitosamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al obtener información del período: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener información del período: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Obtiene el último período académico (más reciente)
     * Basado en el período actual calculado por fecha
     * 
     * @return El último período académico
     */
    @GetMapping("/ultimo")
    public ResponseEntity<Map<String, Object>> obtenerUltimoPeriodo() {
        try {
            PeriodoAcademicoEnum ultimoPeriodo = PeriodoAcademicoEnum.getPeriodoActual();
            
            Map<String, Object> response = new HashMap<>();
            if (ultimoPeriodo != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("valor", ultimoPeriodo.getValor());
                data.put("año", ultimoPeriodo.getAño());
                data.put("numeroPeriodo", ultimoPeriodo.getNumeroPeriodo());
                data.put("descripcion", ultimoPeriodo.getDescripcion());
                
                response.put("success", true);
                response.put("data", data);
                response.put("message", "Último período académico obtenido exitosamente");
            } else {
                response.put("success", false);
                response.put("data", null);
                response.put("message", "No se pudo determinar el último período académico");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener último período: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Obtiene las fechas de inicio y fin de un período académico
     * Usa fechas reales del calendario académico desde BD
     * 
     * @param periodo El período académico (ej: "2025-2")
     * @return Fechas de inicio y fin del período (fechas reales del calendario)
     */
    @GetMapping("/fechas/{periodo}")
    public ResponseEntity<Map<String, Object>> obtenerFechasPeriodo(@PathVariable String periodo) {
        try {
            // Intentar obtener desde BD (con fallback automático al enum)
            Optional<PeriodoAcademico> periodoOpt = periodoAcademicoCU.obtenerPeriodoPorValor(periodo);
            
            Map<String, Object> fechas;
            
            if (periodoOpt.isPresent()) {
                // Usar fechas reales desde BD
                PeriodoAcademico p = periodoOpt.get();
                fechas = new HashMap<>();
                fechas.put("periodo", p.getValor());
                fechas.put("fechaInicio", p.getFecha_inicio().toString());
                fechas.put("fechaFin", p.getFecha_fin().toString());
                fechas.put("fechaInicioClases", p.getFecha_inicio_clases() != null ? p.getFecha_inicio_clases().toString() : null);
                fechas.put("fechaFinClases", p.getFecha_fin_clases() != null ? p.getFecha_fin_clases().toString() : null);
                fechas.put("año", p.getAño());
                fechas.put("numeroPeriodo", p.getNumero_periodo());
            } else {
                // Fallback al enum
                if (!PeriodoAcademicoEnum.esValido(periodo)) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "Período académico no válido: " + periodo);
                    
                    return ResponseEntity.badRequest().body(errorResponse);
                }
                
                PeriodoAcademicoEnum periodoEnum = PeriodoAcademicoEnum.fromValor(periodo);
                fechas = new HashMap<>();
                fechas.put("periodo", periodoEnum.getValor());
                fechas.put("fechaInicio", periodoEnum.getFechaInicio().toString());
                fechas.put("fechaFin", periodoEnum.getFechaFin().toString());
                fechas.put("año", periodoEnum.getAño());
                fechas.put("numeroPeriodo", periodoEnum.getNumeroPeriodo());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", fechas);
            response.put("message", "Fechas del período obtenidas exitosamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al obtener fechas del período: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener fechas del período: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Obtiene el período académico activo configurado por el admin
     * Si no hay período configurado, retorna null (modo automático)
     * 
     * @return El período activo configurado o null si es automático
     */
    @GetMapping("/admin/periodo-activo")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<Map<String, Object>> obtenerPeriodoActivo() {
        try {
            Optional<String> periodoActivo = configuracionCU.obtenerPeriodoActivo();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            
            if (periodoActivo.isPresent()) {
                Map<String, Object> data = new HashMap<>();
                data.put("periodoAcademico", periodoActivo.get());
                data.put("modo", "manual");
                data.put("descripcion", "Período configurado manualmente por administrador");
                
                response.put("data", data);
                response.put("message", "Período activo obtenido exitosamente");
            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("periodoAcademico", null);
                data.put("modo", "automatico");
                data.put("descripcion", "Sistema usa período actual basado en fecha");
                
                response.put("data", data);
                response.put("message", "Sistema en modo automático (basado en fecha)");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al obtener período activo: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener período activo: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Establece el período académico activo del sistema
     * Solo el administrador puede usar este endpoint
     * Si se envía null o vacío, el sistema vuelve a modo automático
     * 
     * @param request Objeto con el período académico a establecer
     * @return Confirmación de la operación
     */
    @PutMapping("/admin/periodo-activo")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<Map<String, Object>> establecerPeriodoActivo(@RequestBody Map<String, String> request) {
        try {
            String periodoAcademico = request.get("periodoAcademico");
            
            // Si viene null o vacío, establecer modo automático
            if (periodoAcademico != null && periodoAcademico.trim().isEmpty()) {
                periodoAcademico = null;
            }
            
            configuracionCU.establecerPeriodoActivo(periodoAcademico);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            
            if (periodoAcademico != null) {
                response.put("message", "Período activo establecido manualmente: " + periodoAcademico);
                response.put("data", Map.of(
                    "periodoAcademico", periodoAcademico,
                    "modo", "manual"
                ));
                log.info("Admin estableció período activo: {}", periodoAcademico);
            } else {
                response.put("message", "Sistema configurado en modo automático (basado en fecha)");
                response.put("data", Map.of(
                    "periodoAcademico", null,
                    "modo", "automatico"
                ));
                log.info("Admin estableció modo automático para período académico");
            }
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error de validación al establecer período activo: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error de validación: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error al establecer período activo: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al establecer período activo: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}

