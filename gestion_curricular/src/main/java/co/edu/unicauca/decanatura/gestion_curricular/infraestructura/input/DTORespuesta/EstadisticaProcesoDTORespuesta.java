package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import java.util.Date;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para estadísticas por tipo de proceso específico.
 * Contiene métricas detalladas de un proceso académico particular.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticaProcesoDTORespuesta {
    
    /**
     * Tipo de proceso (REINGRESO, HOMOLOGACION, ECAES, CURSO_VERANO, PAZ_SALVO)
     */
    private String tipoProceso;
    
    /**
     * Total de solicitudes para este proceso
     */
    private Integer totalSolicitudes;
    
    /**
     * Descripción del proceso
     */
    private String descripcion;
    
    /**
     * Estadísticas por estado para este proceso
     */
    private Map<String, Integer> porEstado;
    
    /**
     * Estadísticas por programa para este proceso
     */
    private Map<String, Integer> porPrograma;
    
    /**
     * Porcentaje de aprobación para este proceso
     */
    private Double porcentajeAprobacion;
    
    /**
     * Tiempo promedio de procesamiento (en días)
     */
    private Double tiempoPromedioProcesamiento;
    
    /**
     * Fecha y hora de la consulta
     */
    private Date fechaConsulta;
    
    /**
     * Indicadores adicionales del proceso
     */
    private Map<String, Object> indicadoresAdicionales;
}
