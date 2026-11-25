package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.controladores;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.PeriodoAcademicoEnum;

/** Utilidades compartidas por los controladores de cursos de verano. */
final class SoporteCursosVerano {

    private SoporteCursosVerano() {
    }

    
    /**
     * Normaliza el formato del período académico a YYYY-P
     * Acepta formatos como "2025 segundo semestre", "2025-2", "Segundo Período 2025", etc.
     */
    static String normalizarPeriodoAcademico(String periodo) {
        // Ambos llamadores descartan antes el texto en blanco.
        return PeriodoAcademicoEnum.normalizar(periodo);
    }
}
