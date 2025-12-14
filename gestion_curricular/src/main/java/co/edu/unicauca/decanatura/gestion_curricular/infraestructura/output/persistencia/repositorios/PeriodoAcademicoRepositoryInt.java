package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.PeriodoAcademicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para períodos académicos
 * 
 * @author Sistema de Gestión Curricular
 */
@Repository
public interface PeriodoAcademicoRepositoryInt extends JpaRepository<PeriodoAcademicoEntity, Integer> {
    
    /**
     * Busca el período académico activo para una fecha específica
     * @param fecha La fecha para la cual buscar el período
     * @return El período académico que contiene la fecha, o vacío si no se encuentra
     */
    @Query("SELECT p FROM PeriodoAcademicoEntity p " +
           "WHERE p.activo = true " +
           "AND p.tipo_periodo = 'REGULAR' " +
           "AND :fecha >= p.fecha_inicio AND :fecha <= p.fecha_fin")
    Optional<PeriodoAcademicoEntity> findPeriodoActivoPorFecha(@Param("fecha") LocalDate fecha);
    
    /**
     * Busca el período académico actual (período activo que contiene la fecha actual)
     * @return El período académico actual
     */
    @Query("SELECT p FROM PeriodoAcademicoEntity p " +
           "WHERE p.activo = true " +
           "AND p.tipo_periodo = 'REGULAR' " +
           "AND CURRENT_DATE >= p.fecha_inicio AND CURRENT_DATE <= p.fecha_fin")
    Optional<PeriodoAcademicoEntity> findPeriodoActual();
    
    /**
     * Busca un período académico por año y número de período
     * @param año El año del período
     * @param numeroPeriodo El número del período (1 o 2)
     * @param tipoPeriodo El tipo de período (REGULAR, VERANO, etc.)
     * @return El período académico encontrado
     */
    @Query("SELECT p FROM PeriodoAcademicoEntity p " +
           "WHERE p.año = :año " +
           "AND p.numero_periodo = :numeroPeriodo " +
           "AND p.tipo_periodo = :tipoPeriodo")
    Optional<PeriodoAcademicoEntity> findByAñoAndNumero_periodoAndTipo_periodo(
            @Param("año") Integer año, 
            @Param("numeroPeriodo") Integer numeroPeriodo, 
            @Param("tipoPeriodo") String tipoPeriodo);
    
    /**
     * Busca un período académico por su valor (formato "YYYY-P")
     * @param valor El valor del período (ej: "2025-1")
     * @return El período académico encontrado
     */
    @Query("SELECT p FROM PeriodoAcademicoEntity p " +
           "WHERE p.tipo_periodo = 'REGULAR' " +
           "AND CONCAT(p.año, '-', p.numero_periodo) = :valor")
    Optional<PeriodoAcademicoEntity> findByValor(@Param("valor") String valor);
    
    /**
     * Obtiene todos los períodos académicos activos
     * @return Lista de períodos activos
     */
    List<PeriodoAcademicoEntity> findByActivoTrue();
    
    /**
     * Obtiene todos los períodos académicos regulares activos ordenados por año y período
     * @return Lista de períodos regulares activos
     */
    @Query("SELECT p FROM PeriodoAcademicoEntity p " +
           "WHERE p.activo = true AND p.tipo_periodo = 'REGULAR' " +
           "ORDER BY p.año DESC, p.numero_periodo DESC")
    List<PeriodoAcademicoEntity> findPeriodosRegularesActivos();
    
    /**
     * Obtiene los períodos académicos futuros (a partir del año actual)
     * @param añoActual El año actual
     * @return Lista de períodos futuros
     */
    @Query("SELECT p FROM PeriodoAcademicoEntity p " +
           "WHERE p.activo = true AND p.tipo_periodo = 'REGULAR' " +
           "AND p.año >= :añoActual " +
           "ORDER BY p.año ASC, p.numero_periodo ASC")
    List<PeriodoAcademicoEntity> findPeriodosFuturos(@Param("añoActual") Integer añoActual);
    
    /**
     * Obtiene los períodos académicos recientes (últimos 5 años)
     * @param añoInicio El año de inicio para buscar
     * @return Lista de períodos recientes
     */
    @Query("SELECT p FROM PeriodoAcademicoEntity p " +
           "WHERE p.activo = true AND p.tipo_periodo = 'REGULAR' " +
           "AND p.año >= :añoInicio " +
           "ORDER BY p.año DESC, p.numero_periodo DESC")
    List<PeriodoAcademicoEntity> findPeriodosRecientes(@Param("añoInicio") Integer añoInicio);
}

