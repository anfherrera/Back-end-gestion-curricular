package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadisticaEntity;

@Repository
public interface EstadisticaRepositoryInt extends JpaRepository<EstadisticaEntity, Integer> {

}
