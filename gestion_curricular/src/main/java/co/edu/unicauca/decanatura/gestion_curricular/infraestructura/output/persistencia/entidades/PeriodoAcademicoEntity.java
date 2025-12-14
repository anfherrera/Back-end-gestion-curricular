package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entidad para períodos académicos configurables
 * Permite gestionar períodos académicos con fechas específicas del calendario
 * 
 * @author Sistema de Gestión Curricular
 * @version 1.0
 */
@Entity
@Table(name = "PeriodosAcademicos", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"año", "numero_periodo", "tipo_periodo"}))
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeriodoAcademicoEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_periodo")
    private Integer id_periodo;
    
    @Column(name = "año", nullable = false)
    private Integer año;
    
    @Column(name = "numero_periodo", nullable = false)
    private Integer numero_periodo; // 1 o 2
    
    @Column(name = "nombre_periodo", length = 100)
    private String nombre_periodo; // Ej: "Primer Período 2025"
    
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fecha_inicio;
    
    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fecha_fin;
    
    @Column(name = "fecha_inicio_clases")
    private LocalDate fecha_inicio_clases;
    
    @Column(name = "fecha_fin_clases")
    private LocalDate fecha_fin_clases;
    
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
    
    @Column(name = "es_periodo_especial", nullable = false)
    private Boolean es_periodo_especial = false; // Para verano, intersemestral
    
    @Column(name = "tipo_periodo", length = 20, nullable = false)
    private String tipo_periodo = "REGULAR"; // 'REGULAR', 'VERANO', 'INTERSEMESTRAL'
    
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
    
    /**
     * Obtiene el valor del período en formato "YYYY-P"
     * @return El valor del período (ej: "2025-1")
     */
    public String getValor() {
        return año + "-" + numero_periodo;
    }
    
    /**
     * Obtiene la descripción completa del período
     * @return Descripción del período
     */
    public String getDescripcion() {
        if (nombre_periodo != null && !nombre_periodo.trim().isEmpty()) {
            return nombre_periodo;
        }
        String periodo = (numero_periodo == 1) ? "Primer Período" : "Segundo Período";
        return periodo + " " + año;
    }
}


