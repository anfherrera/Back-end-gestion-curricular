package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.FechaEcaesEntity;

public interface FechaEcaesRepositoryInt extends JpaRepository<FechaEcaesEntity, Integer> {
    // Aquí puedes definir métodos adicionales si es necesario  

}
