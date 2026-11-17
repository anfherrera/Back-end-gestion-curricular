package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.RolEntity;

public interface RolRepositoryInt extends JpaRepository<RolEntity, Integer> {

    @Query("SELECT r FROM RolEntity r WHERE r.nombre = :nombre")
    Optional<RolEntity> buscarPorNombre(@Param("nombre") String nombre);

    @Modifying
    @Query("DELETE FROM RolEntity r WHERE r.id_rol = :id")
    void eliminarPorId(@Param("id") Integer id);
}
