package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.DocumentoEntity;

public interface DocumentoRepositoryInt extends JpaRepository<DocumentoEntity, Integer> {
    List<DocumentoEntity> findByobjSolicitudIsNull();

}
