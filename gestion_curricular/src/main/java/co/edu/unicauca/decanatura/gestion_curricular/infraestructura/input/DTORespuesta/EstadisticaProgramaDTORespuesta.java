package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import java.util.Date;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para estadísticas por programa académico.
 * Contiene métricas específicas de un programa particular.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticaProgramaDTORespuesta {
    
    /**
     * ID del programa académico
     */
    private Integer idPrograma;
    
    /**
     * Nombre del programa académico
     */
    private String nombrePrograma;
    
    /**
     * Total de solicitudes para este programa
     */
    private Integer totalSolicitudes;
    
    /**
     * Estadísticas por tipo de proceso para este programa
     */
    private Map<String, Integer> porTipoProceso;
    
    /**
     * Estadísticas por estado para este programa
     */
    private Map<String, Integer> porEstado;
    
    /**
     * Porcentaje de aprobación para este programa
     */
    private Double porcentajeAprobacion;
    
    /**
     * Número de estudiantes activos en el programa
     */
    private Integer estudiantesActivos;
    
    /**
     * Fecha y hora de la consulta
     */
    private Date fechaConsulta;
    
    /**
     * Indicadores adicionales del programa
     */
    private Map<String, Object> indicadoresAdicionales;
}
