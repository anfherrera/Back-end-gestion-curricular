package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadisticaEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EstadisticaRepositoryInt extends JpaRepository<EstadisticaEntity, Integer> {

}
