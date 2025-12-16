package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.SalonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SalonRepositoryInt extends JpaRepository<SalonEntity, Integer> {

    /**
     * Buscar salón por número
     * @param numeroSalon Número del salón
     * @return Salón encontrado
     */
    @Query("SELECT s FROM SalonEntity s WHERE s.numero_salon = :numeroSalon")
    Optional<SalonEntity> buscarPorNumero(@Param("numeroSalon") String numeroSalon);

    /**
     * Listar salones activos
     * @return Lista de salones activos
     */
    @Query("SELECT s FROM SalonEntity s WHERE s.activo = true OR s.activo IS NULL ORDER BY s.numero_salon ASC")
    List<SalonEntity> listarSalonesActivos();

    /**
     * Listar salones por edificio
     * @param edificio Nombre del edificio
     * @return Lista de salones del edificio
     */
    @Query("SELECT s FROM SalonEntity s WHERE s.edificio = :edificio ORDER BY s.numero_salon ASC")
    List<SalonEntity> buscarPorEdificio(@Param("edificio") String edificio);

}


