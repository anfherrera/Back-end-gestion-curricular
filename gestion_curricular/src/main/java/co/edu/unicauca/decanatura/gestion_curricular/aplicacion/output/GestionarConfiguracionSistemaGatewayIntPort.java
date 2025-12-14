package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.Optional;

/**
 * Puerto de salida (Gateway) para gestión de configuración del sistema
 * 
 * @author Sistema de Gestión Curricular
 */
public interface GestionarConfiguracionSistemaGatewayIntPort {
    
    /**
     * Obtiene el período académico activo configurado
     * @return El valor del período activo (ej: "2025-1") o null si es automático
     */
    Optional<String> obtenerPeriodoActivo();
    
    /**
     * Establece el período académico activo del sistema
     * @param periodoAcademico El período a establecer (ej: "2025-1") o null para modo automático
     */
    void establecerPeriodoActivo(String periodoAcademico);
}

