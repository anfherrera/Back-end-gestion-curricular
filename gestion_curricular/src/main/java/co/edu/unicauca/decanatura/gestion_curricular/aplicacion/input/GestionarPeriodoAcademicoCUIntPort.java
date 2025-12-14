package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.PeriodoAcademico;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de entrada (Caso de Uso) para gestión de períodos académicos
 * 
 * @author Sistema de Gestión Curricular
 */
public interface GestionarPeriodoAcademicoCUIntPort {
    
    /**
     * Obtiene el período académico actual
     * Si no hay período en BD, usa el enum como fallback
     * @return El período académico actual
     */
    Optional<PeriodoAcademico> obtenerPeriodoActual();
    
    /**
     * Obtiene el período académico para una fecha específica
     * Si no hay período en BD, usa el enum como fallback
     * @param fecha La fecha para la cual obtener el período
     * @return El período académico que contiene la fecha
     */
    Optional<PeriodoAcademico> obtenerPeriodoPorFecha(LocalDate fecha);
    
    /**
     * Obtiene un período académico por su valor (formato "YYYY-P")
     * Si no hay período en BD, usa el enum como fallback
     * @param valor El valor del período (ej: "2025-1")
     * @return El período académico encontrado
     */
    Optional<PeriodoAcademico> obtenerPeriodoPorValor(String valor);
    
    /**
     * Obtiene el valor del período académico actual como String
     * Útil para compatibilidad con código existente
     * @return El valor del período (ej: "2025-1") o null si no se puede determinar
     */
    String obtenerPeriodoActualComoString();
    
    /**
     * Obtiene todos los períodos académicos activos
     * @return Lista de períodos activos
     */
    List<PeriodoAcademico> listarPeriodosActivos();
    
    /**
     * Obtiene los períodos académicos futuros
     * @return Lista de períodos futuros
     */
    List<PeriodoAcademico> listarPeriodosFuturos();
    
    /**
     * Obtiene los períodos académicos recientes (últimos 5 años)
     * @return Lista de períodos recientes
     */
    List<PeriodoAcademico> listarPeriodosRecientes();
}

