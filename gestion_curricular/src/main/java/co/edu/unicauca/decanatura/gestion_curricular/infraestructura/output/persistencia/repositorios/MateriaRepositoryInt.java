package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.MateriaEntity;

import org.springframework.data.jpa.repository.JpaRepository;


public interface MateriaRepositoryInt extends JpaRepository<MateriaEntity, Integer> {

    
}
