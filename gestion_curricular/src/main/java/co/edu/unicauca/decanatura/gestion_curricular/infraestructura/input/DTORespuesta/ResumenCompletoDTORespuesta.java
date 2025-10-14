package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import java.util.Date;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para el resumen completo de estadísticas del sistema.
 * Contiene un dashboard completo con todas las métricas disponibles.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumenCompletoDTORespuesta {
    
    /**
     * Estadísticas globales del sistema
     */
    private Map<String, Object> estadisticasGlobales;
    
    /**
     * Estadísticas desglosadas por tipo de proceso
     */
    private Map<String, Object> porTipoProceso;
    
    /**
     * Estadísticas desglosadas por estado
     */
    private Map<String, Object> porEstado;
    
    /**
     * Total de programas académicos en el sistema
     */
    private Integer totalProgramas;
    
    /**
     * Total de usuarios en el sistema
     */
    private Integer totalUsuarios;
    
    /**
     * Fecha y hora de generación del resumen
     */
    private Date fechaGeneracion;
    
    /**
     * Versión del sistema
     */
    private String versionSistema;
    
    /**
     * Indicadores de rendimiento del sistema
     */
    private Map<String, Object> indicadoresRendimiento;
    
    /**
     * Alertas o notificaciones importantes
     */
    private Map<String, Object> alertas;
}
