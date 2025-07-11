package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

public interface GestionarUsuarioGatewayIntPort {

    Usuario crearUsuario(Usuario usuario);

    Usuario actualizarUsuario(Usuario usuario);

    boolean eliminarUsuario(Integer id_usuario);

    Usuario obtenerUsuarioPorId(Integer id_usuario);

    Usuario buscarUsuarioPorCodigo(String codigo);

    Usuario buscarUsuarioPorCorreo(String correo);

    Usuario buscarUsuarioPorNombre(String nombre);

    Usuario buscarSolicitudesUsuarioPorCodigo(String codigo);

    List<Usuario> buscarUsuariosPorRol(Integer idRol);

    List<Usuario> buscarUsuariosPorPrograma(Integer idPrograma);

    List<Usuario> listarUsuarios();

    Usuario buscarUsuarioPorSolicitud(Integer idSolicitud);
}
