package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.HashMap;

/** Utilidades compartidas por los controladores de cursos de verano. */
@Slf4j
final class SoporteCursosVerano {

    private SoporteCursosVerano() {
    }

    
    /**
     * Normaliza el formato del período académico a YYYY-P
     * Acepta formatos como "2025 segundo semestre", "2025-2", "Segundo Período 2025", etc.
     */
    static String normalizarPeriodoAcademico(String periodo) {
        if (periodo == null || periodo.trim().isEmpty()) {
            return periodo;
        }
        
        String periodoLimpio = periodo.trim();
        
        // Si ya está en formato YYYY-P, retornarlo
        if (periodoLimpio.matches("\\d{4}-[12]")) {
            return periodoLimpio;
        }
        
        // Intentar extraer año y período de formatos como "2025 segundo semestre" o "Segundo Período 2025"
        try {
            // Buscar año (4 dígitos)
            java.util.regex.Pattern añoPattern = java.util.regex.Pattern.compile("(\\d{4})");
            java.util.regex.Matcher añoMatcher = añoPattern.matcher(periodoLimpio);
            
            if (añoMatcher.find()) {
                String año = añoMatcher.group(1);
                
                // Buscar indicador de período
                String periodoLower = periodoLimpio.toLowerCase();
                int numeroPeriodo = 1; // Por defecto primer período
                
                if (periodoLower.contains("segundo") || periodoLower.contains("2") || periodoLower.contains("ii")) {
                    numeroPeriodo = 2;
                } else if (periodoLower.contains("primer") || periodoLower.contains("primero") || periodoLower.contains("1") || periodoLower.contains("i")) {
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

    /** 409 con código CUPO_AGOTADO. */
    static ResponseEntity<Map<String, Object>> respuestaCupoAgotado(
            co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CupoAgotadoException e) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", e.getMessage());
        error.put("codigo", "CUPO_AGOTADO");
        error.put("cupo", e.getCupo());
        error.put("ocupados", e.getOcupados());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
