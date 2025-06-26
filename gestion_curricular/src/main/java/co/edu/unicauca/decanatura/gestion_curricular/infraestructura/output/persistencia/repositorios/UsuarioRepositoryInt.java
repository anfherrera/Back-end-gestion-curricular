package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;


public interface UsuarioRepositoryInt extends JpaRepository<UsuarioEntity,Integer> {

    @Query(value = "SELECT u FROM UsuarioEntity u WHERE u.codigo = :codigo")
    UsuarioEntity buscarIdUsuarioPorCodigo(@Param("codigo") String codigo);

    @Query(value = "SELECT * FROM usuariosolicitudes us LEFT JOIN Solicitudes ON us.idSolicitud  = Solicitudes.idSolicitud WHERE us.IdUsuario = :IdUsuario ", nativeQuery = true)
    Collection<UsuarioEntity> buscarSolicitudesPorUsuario(@Param("IdUsuario") Integer IdUsuario);
    
    // Consulta nativa: verificar si ya existe un correo
    @Query(value = "SELECT COUNT(*) FROM usuarios WHERE correo = ?1", nativeQuery = true)
    Integer contarPorCorreo(String correo);

    // Consulta JPQL: buscar usuario por correo exacto
    @Query("SELECT u FROM UsuarioEntity u WHERE u.correo = :correo")
    UsuarioEntity buscarPorCorreo(@Param("correo") String correo);

    // Consulta JPQL: buscar usuarios por nombre parcial
    @Query("SELECT u FROM UsuarioEntity u WHERE LOWER(u.nombre_completo) LIKE LOWER(CONCAT('%', :nombreParcial, '%')) ORDER BY u.nombre_completo ASC")
    List<UsuarioEntity> buscarPorNombreParcial(@Param("nombreParcial") String nombreParcial);

    // Consulta JPQL: obtener usuarios por rol
    @Query("SELECT u FROM UsuarioEntity u WHERE u.objRol.id_rol = :idRol")
    List<UsuarioEntity> buscarPorRol(@Param("idRol") Integer idRol);

    // Consulta JPQL: obtener usuarios por programa
    @Query("SELECT u FROM UsuarioEntity u WHERE u.objPrograma.id_programa = :idPrograma")
    List<UsuarioEntity> buscarPorPrograma(@Param("idPrograma") Integer idPrograma);

    // Consulta JPQL: traer todos los usuarios con sus solicitudes (ManyToMany)
    @Query("SELECT DISTINCT u FROM UsuarioEntity u LEFT JOIN FETCH u.solicitudes")
    List<UsuarioEntity> listarConSolicitudes();
    
}
