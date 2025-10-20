package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.FechaEcaesEntity;

public interface FechaEcaesRepositoryInt extends JpaRepository<FechaEcaesEntity, Integer> {
    
    /**
     * Buscar fechas ECAES por período académico
     */
    Optional<FechaEcaesEntity> findByPeriodoAcademico(String periodoAcademico);
    
    /**
     * Verificar si existen fechas para un período académico
     */
    boolean existsByPeriodoAcademico(String periodoAcademico);

}
