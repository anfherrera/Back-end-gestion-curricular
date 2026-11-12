package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoSolicitudEntity;

import org.springframework.data.jpa.repository.JpaRepository;


public interface EstadoSolicitudRepositoryInt extends JpaRepository<EstadoSolicitudEntity, Integer> {

}
