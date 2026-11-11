package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

public interface GestionarUsuarioCUIntPort {

    Usuario crearUsuario(Usuario usuario);

    Usuario actualizarUsuario(Usuario usuario);

    boolean eliminarUsuario(Integer id_usuario);

    /** Usuario con programa y rol cargados. Si no existe, responde 404. */
    Usuario obtenerUsuarioPorId(Integer id_usuario);

    /** Igual que {@link #obtenerUsuarioPorId}, pero sin error si no existe (o si el id es nulo). */
    Optional<Usuario> buscarUsuarioPorId(Integer id_usuario);

    Usuario buscarUsuarioPorCodigo(String codigo);

    Usuario buscarUsuarioPorCorreo(String correo);

    Usuario buscarUsuarioPorNombre(String nombre);

    Usuario buscarSolicitudesUsuarioPorCodigo(String codigo);

    List<Usuario> buscarUsuariosPorRol(Integer idRol);

    List<Usuario> buscarUsuariosPorPrograma(Integer idPrograma);

    List<Usuario> listarUsuarios();

    Usuario buscarUsuarioPorSolicitud(Integer idSolicitud);
}
