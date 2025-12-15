package co.edu.unicauca.decanatura.gestion_curricular.controladores;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.PeriodoAcademicoEnum;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para manejar operaciones relacionadas con períodos académicos
 * 
 * @author Sistema de Gestión Curricular
 * @version 1.0
 */
@RestController
@RequestMapping("/api/periodos-academicos")
public class PeriodoAcademicoController {

    /**
     * Obtiene todos los períodos académicos disponibles
     * 
     * @return Lista de todos los períodos académicos
     */
    @GetMapping("/todos")
    public ResponseEntity<Map<String, Object>> obtenerTodosLosPeriodos() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", PeriodoAcademicoEnum.getTodosLosValores());
            response.put("message", "Períodos académicos obtenidos exitosamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener períodos académicos: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Obtiene los períodos académicos futuros (año actual en adelante)
     * 
     * @return Lista de períodos académicos futuros
     */
    @GetMapping("/futuros")
    public ResponseEntity<Map<String, Object>> obtenerPeriodosFuturos() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", PeriodoAcademicoEnum.getPeriodosFuturos());
            response.put("message", "Períodos académicos futuros obtenidos exitosamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener períodos futuros: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Obtiene los períodos académicos recientes (últimos 5 años)
     * 
     * @return Lista de períodos académicos recientes
     */
    @GetMapping("/recientes")
    public ResponseEntity<Map<String, Object>> obtenerPeriodosRecientes() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", PeriodoAcademicoEnum.getPeriodosRecientes());
            response.put("message", "Períodos académicos recientes obtenidos exitosamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener períodos recientes: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Obtiene el período académico actual
     * 
     * @return El período académico actual
     */
    @GetMapping("/actual")
    public ResponseEntity<Map<String, Object>> obtenerPeriodoActual() {
        try {
            PeriodoAcademicoEnum periodoActual = PeriodoAcademicoEnum.getPeriodoActual();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", periodoActual != null ? periodoActual.getValor() : null);
            response.put("message", periodoActual != null ? 
                "Período actual obtenido exitosamente" : 
                "No se pudo determinar el período actual");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
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
     * @param periodo El período del cual obtener información
     * @return Información detallada del período
     */
    @GetMapping("/info/{periodo}")
    public ResponseEntity<Map<String, Object>> obtenerInformacionPeriodo(@PathVariable String periodo) {
        try {
            if (!PeriodoAcademicoEnum.esValido(periodo)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Período académico no válido: " + periodo);
                
                return ResponseEntity.badRequest().body(errorResponse);
            }

            PeriodoAcademicoEnum periodoEnum = PeriodoAcademicoEnum.fromValor(periodo);
            
            Map<String, Object> info = new HashMap<>();
            info.put("valor", periodoEnum.getValor());
            info.put("año", periodoEnum.getAño());
            info.put("numeroPeriodo", periodoEnum.getNumeroPeriodo());
            info.put("descripcion", periodoEnum.getDescripcion());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", info);
            response.put("message", "Información del período obtenida exitosamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
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
     * Convierte un período académico a fechas de inicio y fin
     * 
     * @param periodo El período académico (ej: "2024-2")
     * @return Fechas de inicio y fin del período
     */
    @GetMapping("/fechas/{periodo}")
    public ResponseEntity<Map<String, Object>> obtenerFechasPeriodo(@PathVariable String periodo) {
        try {
            if (!PeriodoAcademicoEnum.esValido(periodo)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Período académico no válido: " + periodo);
                
                return ResponseEntity.badRequest().body(errorResponse);
            }

            PeriodoAcademicoEnum periodoEnum = PeriodoAcademicoEnum.fromValor(periodo);
            int año = periodoEnum.getAño();
            int numeroPeriodo = periodoEnum.getNumeroPeriodo();
            
            // Calcular fechas según el período
            // Primer período: enero a junio
            // Segundo período: julio a diciembre
            java.time.LocalDate fechaInicio;
            java.time.LocalDate fechaFin;
            
            if (numeroPeriodo == 1) {
                fechaInicio = java.time.LocalDate.of(año, 1, 1);
                fechaFin = java.time.LocalDate.of(año, 6, 30);
            } else {
                fechaInicio = java.time.LocalDate.of(año, 7, 1);
                fechaFin = java.time.LocalDate.of(año, 12, 31);
            }
            
            Map<String, Object> fechas = new HashMap<>();
            fechas.put("periodo", periodo);
            fechas.put("fechaInicio", fechaInicio.toString());
            fechas.put("fechaFin", fechaFin.toString());
            fechas.put("año", año);
            fechas.put("numeroPeriodo", numeroPeriodo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", fechas);
            response.put("message", "Fechas del período obtenidas exitosamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener fechas del período: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}

