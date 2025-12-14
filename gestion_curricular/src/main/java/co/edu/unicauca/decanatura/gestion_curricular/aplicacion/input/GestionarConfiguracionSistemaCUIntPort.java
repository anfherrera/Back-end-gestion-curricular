package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.Optional;

/**
 * Puerto de entrada (Caso de Uso) para gestión de configuración del sistema
 * 
 * @author Sistema de Gestión Curricular
 */
public interface GestionarConfiguracionSistemaCUIntPort {
    
    /**
     * Obtiene el período académico activo configurado
     * @return El valor del período activo (ej: "2025-1") o null si es automático
     */
    Optional<String> obtenerPeriodoActivo();
    
    /**
     * Establece el período académico activo del sistema
     * Solo el admin puede usar este método
     * @param periodoAcademico El período a establecer (ej: "2025-1") o null para modo automático
     * @throws IllegalArgumentException Si el período no es válido
     */
    void establecerPeriodoActivo(String periodoAcademico);
}

