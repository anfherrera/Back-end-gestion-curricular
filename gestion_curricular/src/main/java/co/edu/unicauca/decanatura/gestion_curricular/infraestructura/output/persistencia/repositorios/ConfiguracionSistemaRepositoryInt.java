package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.ConfiguracionSistemaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para configuración del sistema
 * 
 * @author Sistema de Gestión Curricular
 */
@Repository
public interface ConfiguracionSistemaRepositoryInt extends JpaRepository<ConfiguracionSistemaEntity, Integer> {
    
    /**
     * Obtiene la configuración del sistema (debe haber solo una fila)
     * @return La configuración del sistema
     */
    @Query("SELECT c FROM ConfiguracionSistemaEntity c ORDER BY c.id_configuracion ASC")
    List<ConfiguracionSistemaEntity> findAllOrdered();
    
    /**
     * Obtiene la primera configuración del sistema
     * @return La configuración del sistema o vacío si no existe
     */
    default Optional<ConfiguracionSistemaEntity> findFirst() {
        List<ConfiguracionSistemaEntity> all = findAllOrdered();
        return all.isEmpty() ? Optional.empty() : Optional.of(all.get(0));
    }
}

