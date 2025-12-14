package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Modelo de dominio para períodos académicos
 * 
 * @author Sistema de Gestión Curricular
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeriodoAcademico {
    
    private Integer id_periodo;
    private Integer año;
    private Integer numero_periodo;
    private String nombre_periodo;
    private LocalDate fecha_inicio;
    private LocalDate fecha_fin;
    private LocalDate fecha_inicio_clases;
    private LocalDate fecha_fin_clases;
    private Boolean activo;
    private Boolean es_periodo_especial;
    private String tipo_periodo;
    
    /**
     * Obtiene el valor del período en formato "YYYY-P"
     * @return El valor del período (ej: "2025-1")
     */
    public String getValor() {
        return año + "-" + numero_periodo;
    }
    
    /**
     * Verifica si una fecha está dentro de este período académico
     * @param fecha La fecha a verificar
     * @return true si la fecha está dentro del período, false en caso contrario
     */
    public boolean contieneFecha(LocalDate fecha) {
        if (fecha == null) {
            return false;
        }
        return !fecha.isBefore(fecha_inicio) && !fecha.isAfter(fecha_fin);
    }
}

