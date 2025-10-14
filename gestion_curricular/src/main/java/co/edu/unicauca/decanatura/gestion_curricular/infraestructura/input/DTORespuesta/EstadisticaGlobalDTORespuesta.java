package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para estadísticas globales del sistema.
 * Contiene métricas generales combinando todos los procesos.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticaGlobalDTORespuesta {
    
    /**
     * Total de solicitudes en todo el sistema
     */
    private Integer totalSolicitudes;
    
    /**
     * Total de solicitudes aprobadas
     */
    private Integer totalAprobadas;
    
    /**
     * Total de solicitudes rechazadas
     */
    private Integer totalRechazadas;
    
    /**
     * Total de solicitudes en proceso
     */
    private Integer totalEnProceso;
    
    /**
     * Porcentaje de aprobación general
     */
    private Double porcentajeAprobacion;
    
    /**
     * Estadísticas desglosadas por tipo de proceso
     */
    private Map<String, Integer> porTipoProceso;
    
    /**
     * Total de programas académicos
     */
    private Integer totalProgramas;
    
    /**
     * Lista de nombres de programas
     */
    private List<String> nombresProgramas;
    
    /**
     * Fecha y hora de la consulta
     */
    private Date fechaConsulta;
    
    /**
     * Descripción de las estadísticas
     */
    private String descripcion;
}
