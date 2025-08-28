package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SolicitudReingresoEntity;

public interface SolicitudReingresoRepositoryInt extends JpaRepository<SolicitudReingresoEntity, Integer> {

}
