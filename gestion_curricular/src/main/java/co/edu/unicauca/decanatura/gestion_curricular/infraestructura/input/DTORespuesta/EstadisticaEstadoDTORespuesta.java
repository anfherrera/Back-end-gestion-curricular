package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import java.util.Date;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para estadísticas por estado de solicitud.
 * Contiene métricas específicas de un estado particular.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticaEstadoDTORespuesta {
    
    /**
     * Estado de la solicitud (EN_PROCESO, APROBADA, RECHAZADA, etc.)
     */
    private String estado;
    
    /**
     * Total de solicitudes en este estado
     */
    private Integer totalSolicitudes;
    
    /**
     * Descripción del estado
     */
    private String descripcionEstado;
    
    /**
     * Estadísticas por tipo de proceso para este estado
     */
    private Map<String, Integer> porTipoProceso;
    
    /**
     * Estadísticas por programa para este estado
     */
    private Map<String, Integer> porPrograma;
    
    /**
     * Porcentaje que representa este estado del total
     */
    private Double porcentajeDelTotal;
    
    /**
     * Tiempo promedio en este estado (en días)
     */
    private Double tiempoPromedioEnEstado;
    
    /**
     * Fecha y hora de la consulta
     */
    private Date fechaConsulta;
    
    /**
     * Indicadores adicionales del estado
     */
    private Map<String, Object> indicadoresAdicionales;
}
