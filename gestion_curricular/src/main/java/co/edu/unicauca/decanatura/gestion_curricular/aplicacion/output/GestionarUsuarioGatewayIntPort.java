package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.List;
import java.util.Optional;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

public interface GestionarUsuarioGatewayIntPort {

    Usuario crearUsuario(Usuario usuario);

    Usuario actualizarUsuario(Usuario usuario);

    boolean eliminarUsuario(Integer id_usuario);

    Usuario obtenerUsuarioPorId(Integer id_usuario);

    Optional<Usuario> buscarUsuarioPorId(Integer idUsuario);

    Usuario buscarUsuarioPorCorreo(String correo);

    Usuario buscarUsuarioPorCedula(String cedula);

    List<Usuario> buscarUsuariosPorRol(Integer idRol);

    List<Usuario> listarUsuarios();

    Usuario buscarUsuarioPorSolicitud(Integer idSolicitud);

    Usuario buscarUsuarioPorCodigo(String codigo);
}
