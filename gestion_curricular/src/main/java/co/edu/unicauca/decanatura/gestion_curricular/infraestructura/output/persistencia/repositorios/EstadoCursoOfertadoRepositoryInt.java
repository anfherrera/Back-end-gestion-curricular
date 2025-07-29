package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.EstadoCursoOfertadoEntity;

public interface EstadoCursoOfertadoRepositoryInt extends JpaRepository<EstadoCursoOfertadoEntity, Integer> {
    // Aquí puedes definir métodos adicionales específicos para EstadoCursoOfertado si es necesario
    
}
