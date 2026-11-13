package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

/** Utilidades compartidas por los controladores de cursos de verano. */
final class SoporteCursosVerano {

    private SoporteCursosVerano() {
    }

    
    /**
     * Normaliza el formato del período académico a YYYY-P
     * Acepta formatos como "2025 segundo semestre", "2025-2", "Segundo Período 2025", etc.
     */
    static String normalizarPeriodoAcademico(String periodo) {
        // Ambos llamadores descartan antes el texto en blanco. BUG-12 se conserva.
        return co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.PeriodoAcademicoEnum.normalizar(periodo);
    }

    /** 409 con código CUPO_AGOTADO. */
    static ResponseEntity<Map<String, Object>> respuestaCupoAgotado(
            co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.CupoAgotadoException e) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", e.getMessage());
        error.put("codigo", "CUPO_AGOTADO");
        error.put("cupo", e.getCupo());
        error.put("ocupados", e.getOcupados());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
