package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import java.util.Date;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para estadísticas de tendencias por período.
 * Contiene comparación entre dos períodos académicos.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TendenciasPeriodoDTORespuesta {
    
    /**
     * Estadísticas del primer período
     */
    private Map<String, Object> periodo1;
    
    /**
     * Estadísticas del segundo período
     */
    private Map<String, Object> periodo2;
    
    /**
     * Variación porcentual entre períodos
     */
    private Double variacionPorcentual;
    
    /**
     * Tipo de tendencia (CRECIENTE, DECRECIENTE, ESTABLE)
     */
    private String tendencia;
    
    /**
     * Fecha de inicio del primer período
     */
    private Date fechaInicio1;
    
    /**
     * Fecha de fin del primer período
     */
    private Date fechaFin1;
    
    /**
     * Fecha de inicio del segundo período
     */
    private Date fechaInicio2;
    
    /**
     * Fecha de fin del segundo período
     */
    private Date fechaFin2;
    
    /**
     * Fecha y hora de la consulta
     */
    private Date fechaConsulta;
    
    /**
     * Análisis detallado de la tendencia
     */
    private Map<String, Object> analisisDetallado;
    
    /**
     * Recomendaciones basadas en la tendencia
     */
    private Map<String, Object> recomendaciones;
}
