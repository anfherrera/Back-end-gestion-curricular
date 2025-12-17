package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.repositorios;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades.UsuarioEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepositoryInt extends JpaRepository<UsuarioEntity, Integer> {

    // Buscar usuario por código exacto (usado en obtener usuario o para encontrar ID)
    @Query("SELECT u FROM UsuarioEntity u WHERE u.codigo = :codigo")
    UsuarioEntity buscarIdUsuarioPorCodigo(@Param("codigo") String codigo);
    
    // Verificar existencia de correo (retorna count)
    @Query(value = "SELECT COUNT(*) FROM usuarios WHERE correo = ?1", nativeQuery = true)
    Integer contarPorCorreo(String correo);

    // Buscar por correo exacto
    @Query("SELECT u FROM UsuarioEntity u WHERE u.correo = :correo")
    Optional<UsuarioEntity> buscarPorCorreo(@Param("correo") String correo);
    
    // Buscar por correo con relaciones cargadas (JOIN FETCH) - para coordinadores
    @Query("SELECT u FROM UsuarioEntity u LEFT JOIN FETCH u.objRol LEFT JOIN FETCH u.objPrograma WHERE u.correo = :correo")
    Optional<UsuarioEntity> buscarPorCorreoWithRelations(@Param("correo") String correo);

    // Buscar por cédula exacta
    @Query("SELECT u FROM UsuarioEntity u WHERE u.cedula = :cedula")
    Optional<UsuarioEntity> buscarPorCedula(@Param("cedula") String cedula);

    // Buscar por nombre parcial, sin importar mayúsculas
    @Query("SELECT u FROM UsuarioEntity u WHERE LOWER(u.nombre_completo) LIKE LOWER(CONCAT('%', :nombreParcial, '%')) ORDER BY u.nombre_completo ASC")
    List<UsuarioEntity> buscarPorNombreParcial(@Param("nombreParcial") String nombreParcial);

    // Buscar todos los usuarios con cierto rol
    @Query("SELECT u FROM UsuarioEntity u WHERE u.objRol.id_rol = :idRol")
    List<UsuarioEntity> buscarPorRol(@Param("idRol") Integer idRol);

    // Buscar todos los usuarios pertenecientes a un programa específico
    @Query("SELECT u FROM UsuarioEntity u WHERE u.objPrograma.id_programa = :idPrograma")
    List<UsuarioEntity> buscarPorPrograma(@Param("idPrograma") Integer idPrograma);

    
    // Traer todos los usuarios con sus solicitudes (para evitar lazy loading)
    @Query("SELECT u FROM UsuarioEntity u")
    List<UsuarioEntity> listarConSolicitudes();

    @Query("SELECT s.objUsuario FROM SolicitudEntity s WHERE s.id_solicitud = :idSolicitud")
    UsuarioEntity buscarUsuariosPorSolicitud(@Param("idSolicitud") Integer idSolicitud);

    // Buscar usuario por ID con relaciones cargadas (JOIN FETCH)
    @Query("SELECT u FROM UsuarioEntity u LEFT JOIN FETCH u.objRol LEFT JOIN FETCH u.objPrograma WHERE u.id_usuario = :id")
    Optional<UsuarioEntity> findByIdWithRelations(@Param("id") Integer id);

    @Modifying
    @Query("DELETE FROM UsuarioEntity u WHERE u.id = :id")
    void eliminarPorId(@Param("id") Integer id);

}
