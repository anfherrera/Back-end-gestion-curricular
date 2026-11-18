package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarUsuarioCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.CodificadorContrasenaIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarProgramaGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarRolGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadNoExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadYaExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

public class GestionarUsuarioCUAdapter implements GestionarUsuarioCUIntPort {

    private final GestionarUsuarioGatewayIntPort gestionarUsuarioGateway;
    private final GestionarRolGatewayIntPort gestionarRolGateway;
    private final GestionarProgramaGatewayIntPort gestionarProgramaGateway;
    private final CodificadorContrasenaIntPort codificadorContrasena;

    public GestionarUsuarioCUAdapter(GestionarUsuarioGatewayIntPort gestionarUsuarioGateway,
                                    GestionarProgramaGatewayIntPort gestionarProgramaGateway,
                                    GestionarRolGatewayIntPort gestionarRolGateway,
                                     CodificadorContrasenaIntPort codificadorContrasena) {
        this.gestionarUsuarioGateway = gestionarUsuarioGateway;
        this.gestionarProgramaGateway = gestionarProgramaGateway;
        this.gestionarRolGateway = gestionarRolGateway;
        this.codificadorContrasena = codificadorContrasena;
    }

@Override
public Usuario crearUsuario(Usuario usuario) {

    Programa programa = null;
    Rol objRol = null;
    Usuario usuarioExistenteCorreo = gestionarUsuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo());
    if (usuarioExistenteCorreo != null) {
        throw new EntidadYaExisteException("Ya existe un usuario con ese correo.");
    }

    Usuario usuarioExistenteCodigo = gestionarUsuarioGateway.buscarUsuarioPorCodigo(usuario.getCodigo());
    if (usuarioExistenteCodigo != null) {
        throw new EntidadYaExisteException("Ya existe un usuario con ese código.");
    }

    // Validar unicidad de cédula si se proporciona
    if (usuario.getCedula() != null && !usuario.getCedula().trim().isEmpty()) {
        Usuario usuarioExistenteCedula = gestionarUsuarioGateway.buscarUsuarioPorCedula(usuario.getCedula());
        if (usuarioExistenteCedula != null) {
            throw new EntidadYaExisteException("Ya existe un usuario con esa cédula.");
        }
    }
    

    if (usuario.getObjPrograma() == null) {
        throw new EntidadYaExisteException("El programa del usuario no puede ser nulo o su ID no puede ser nulo.");
    }

    programa = this.gestionarProgramaGateway.buscarPorIdPrograma(usuario.getObjPrograma().getId_programa());

    if (programa == null) {
        throw new EntidadYaExisteException("El programa enviado no existe.");
    }
    objRol = this.gestionarRolGateway.buscarRolPorId(usuario.getObjRol().getId_rol());
    if (objRol == null) {
        throw new EntidadYaExisteException("El rol con ID: '" + usuario.getObjRol().getId_rol() + "' no existe.");
    }
    String passwordCodificada = codificadorContrasena.codificar(usuario.getPassword());
    usuario.setPassword(passwordCodificada);
    objRol.getUsuarios().add(usuario);
    usuario.setObjRol(objRol);
    usuario.setObjPrograma(programa);
    usuario.setId_usuario(null);

    return this.gestionarUsuarioGateway.crearUsuario(usuario);
}

    @Override
    public Usuario actualizarUsuario(Usuario usuario) {
        Programa programa = null;
        Rol objRol = null;
        Usuario usuarioActualizado = null;
        if (usuario == null) {
            throw new EntidadYaExisteException("El usuario no puede ser nulo.");
        }

        if(usuario.getId_usuario() == null) {
            throw new EntidadYaExisteException("El ID del usuario no puede ser nulo.");

        }

        Usuario existente = this.gestionarUsuarioGateway.obtenerUsuarioPorId(usuario.getId_usuario());
        if (existente == null) {
            throw new EntidadYaExisteException("No se encontró el usuario a actualizar.");
        }

        if (usuario.getObjPrograma() == null) {
        throw new EntidadYaExisteException("El programa del usuario no puede ser nulo");
        }

        if(usuario.getObjPrograma().getId_programa() == null) {
            throw new EntidadYaExisteException("El ID del programa del usuario no puede ser nulo.");

        }

        programa = this.gestionarProgramaGateway.buscarPorIdPrograma(usuario.getObjPrograma().getId_programa());

        if (programa == null) {
            throw new EntidadYaExisteException("El programa enviado no existe.");
        }

       
        if (usuario.getObjRol() == null) {
            throw new EntidadYaExisteException("El rol es nulo");
        }

        if (usuario.getObjRol().getId_rol() == null) {
            throw new EntidadYaExisteException("El ID del rol no puede ser nulo.");
        }

        objRol = this.gestionarRolGateway.buscarRolPorId(usuario.getObjRol().getId_rol());
        if (objRol == null) {
            throw new EntidadYaExisteException("El rol enviado no existe.");
        }

        // Validar unicidad de cédula si se proporciona y es diferente a la existente
        if (usuario.getCedula() != null && !usuario.getCedula().trim().isEmpty()) {
            Usuario usuarioExistenteCedula = gestionarUsuarioGateway.buscarUsuarioPorCedula(usuario.getCedula());
            if (usuarioExistenteCedula != null && !usuarioExistenteCedula.getId_usuario().equals(usuario.getId_usuario())) {
                throw new EntidadYaExisteException("Ya existe otro usuario con esa cédula.");
            }
        }

        // Manejar password: si se proporciona, codificarla; si no, mantener la existente
        if (usuario.getPassword() != null && !usuario.getPassword().trim().isEmpty()) {
            String passwordCodificada = codificadorContrasena.codificar(usuario.getPassword());
            usuario.setPassword(passwordCodificada);
        } else {
            // Mantener la password existente
            usuario.setPassword(existente.getPassword());
        }

        objRol.getUsuarios().add(usuario);
        usuario.setObjRol(objRol);
        usuario.setObjPrograma(programa);


        try{
            usuarioActualizado = this.gestionarUsuarioGateway.actualizarUsuario(usuario);
        } catch (RuntimeException e) {
            throw new EntidadYaExisteException("Error al actualizar el usuario: " + e.getMessage());
        } catch (Exception e) {
            throw new EntidadYaExisteException("Error inesperado al actualizar el usuario: " + e.getMessage());
        }

        return usuarioActualizado;
    }

    @Override
    public boolean eliminarUsuario(Integer id_usuario) {
        if (id_usuario == null) {
            throw new EntidadYaExisteException("El ID del usuario no puede ser nulo.");
        }

        Usuario existente = this.gestionarUsuarioGateway.obtenerUsuarioPorId(id_usuario);
        if (existente == null) {
            throw new EntidadYaExisteException("No se encontró el usuario a eliminar.");
        }

        return this.gestionarUsuarioGateway.eliminarUsuario(id_usuario);
    }

    @Override
    public Usuario obtenerUsuarioPorId(Integer id_usuario) {
        if (id_usuario == null) {
            throw new EntidadYaExisteException("El ID del usuario no puede ser nulo.");
        }

        Usuario usuario = this.gestionarUsuarioGateway.obtenerUsuarioPorId(id_usuario);
        if (usuario == null) {
            throw new EntidadNoExisteException(
                    "Usuario no encontrado con ID: " + id_usuario);
        }

        return usuario;
    }

    @Override
    public java.util.Optional<Usuario> buscarUsuarioPorId(Integer id_usuario) {
        if (id_usuario == null) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.ofNullable(this.gestionarUsuarioGateway.obtenerUsuarioPorId(id_usuario));
    }

    @Override
    public Usuario buscarUsuarioPorCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new EntidadYaExisteException("El correo no puede estar vacío.");
        }

        Usuario usuario = this.gestionarUsuarioGateway.buscarUsuarioPorCorreo(correo);
        if (usuario == null) {
            throw new EntidadYaExisteException("No se encontró el usuario con ese correo.");
        }

        return usuario;
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return this.gestionarUsuarioGateway.listarUsuarios();
    }

}
