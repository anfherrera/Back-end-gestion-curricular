package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.List;


import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarUsuarioCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarProgramaGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarRolGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarUsuarioGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

public class GestionarUsuarioCUAdapter implements GestionarUsuarioCUIntPort {

    private final GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway;
    private final GestionarRolGatewayIntPort objGestionarRolGateway;
    private final GestionarProgramaGatewayIntPort objGestionarProgramaGateway;

    private final FormateadorResultadosIntPort objFormateadorResultados;

    public GestionarUsuarioCUAdapter(GestionarUsuarioGatewayIntPort objGestionarUsuarioGateway,
                                    GestionarProgramaGatewayIntPort objGestionarProgramaGateway,
                                    GestionarRolGatewayIntPort objGestionarRolGateway,
                                     FormateadorResultadosIntPort objFormateadorResultados) {
        this.objGestionarUsuarioGateway = objGestionarUsuarioGateway;
        this.objGestionarProgramaGateway = objGestionarProgramaGateway;
        this.objGestionarRolGateway = objGestionarRolGateway;
        this.objFormateadorResultados = objFormateadorResultados;
    }

@Override
public Usuario crearUsuario(Usuario usuario) {

    Programa programa = null;
    Rol objRol = null;
    Usuario usuarioExistenteCorreo = objGestionarUsuarioGateway.buscarUsuarioPorCorreo(usuario.getCorreo());
    if (usuarioExistenteCorreo != null) {
        this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Ya existe un usuario con ese correo.");
    }

    Usuario usuarioExistenteCodigo = objGestionarUsuarioGateway.buscarUsuarioPorCodigo(usuario.getCodigo());
    if (usuarioExistenteCodigo != null) {
        this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Ya existe un usuario con ese código.");
    }
    

    if (usuario.getObjPrograma() == null) {
        this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("El programa del usuario no puede ser nulo o su ID no puede ser nulo.");
    }

    programa = this.objGestionarProgramaGateway.buscarPorIdPrograma(usuario.getObjPrograma().getId_programa());

    if (programa == null) {
        this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("El programa enviado no existe.");
    }
    objRol = this.objGestionarRolGateway.buscarRolPorNombre("Estudiante");
    if (objRol == null) {
        this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("El rol 'Estudiante' no existe.");
    }
    objRol.getUsuarios().add(usuario);
    usuario.setObjRol(objRol);
    usuario.setObjPrograma(programa);
    usuario.setId_usuario(null);

    return this.objGestionarUsuarioGateway.crearUsuario(usuario);
}

    @Override
    public Usuario actualizarUsuario(Usuario usuario) {
        Programa programa = null;
        Rol objRol = null;
        Usuario usuarioActualizado = null;
        if (usuario == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("El usuario no puede ser nulo.");
        }

        if(usuario.getId_usuario() == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("El ID del usuario no puede ser nulo.");

        }
        //this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("usuario id" + usuario.getId_usuario());

        Usuario existente = this.objGestionarUsuarioGateway.obtenerUsuarioPorId(usuario.getId_usuario());
        if (existente == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró el usuario a actualizar.");
        }

        if (usuario.getObjPrograma() == null) {
        this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("El programa del usuario no puede ser nulo");
        }

        if(usuario.getObjPrograma().getId_programa() == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("El ID del programa del usuario no puede ser nulo.");

        }

        programa = this.objGestionarProgramaGateway.buscarPorIdPrograma(usuario.getObjPrograma().getId_programa());

        if (programa == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("El programa enviado no existe.");
        }

       
        if (usuario.getObjRol() == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("El rol es nulo");
        }

        if (usuario.getObjRol().getId_rol() == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("El ID del rol no puede ser nulo.");
        }

        objRol = this.objGestionarRolGateway.bucarRolPorId(usuario.getObjRol().getId_rol());
        if (objRol == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("El rol enviado no existe.");
        }

        objRol.getUsuarios().add(usuario);
        usuario.setObjRol(objRol);
        usuario.setObjPrograma(programa);


        try{
            usuarioActualizado = this.objGestionarUsuarioGateway.actualizarUsuario(usuario);
        } catch (RuntimeException e) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Error al actualizar el usuario: " + e.getMessage());
        } catch (Exception e) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Error inesperado al actualizar el usuario: " + e.getMessage());
        }

        return usuarioActualizado;
    }

    @Override
    public boolean eliminarUsuario(Integer id_usuario) {
        if (id_usuario == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("El ID del usuario no puede ser nulo.");
        }

        Usuario existente = this.objGestionarUsuarioGateway.obtenerUsuarioPorId(id_usuario);
        if (existente == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró el usuario a eliminar.");
        }

        return this.objGestionarUsuarioGateway.eliminarUsuario(id_usuario);
    }

    @Override
    public Usuario obtenerUsuarioPorId(Integer id_usuario) {
        if (id_usuario == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("El ID del usuario no puede ser nulo.");
        }

        Usuario usuario = this.objGestionarUsuarioGateway.obtenerUsuarioPorId(id_usuario);
        if (usuario == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Usuario no encontrado.");
        }

        return usuario;
    }

    @Override
    public Usuario buscarUsuarioPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("El código no puede estar vacío.");
        }

        Usuario usuario = this.objGestionarUsuarioGateway.buscarUsuarioPorCodigo(codigo);
        if (usuario == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró el usuario con ese código.");
        }

        return usuario;
    }

    @Override
    public Usuario buscarUsuarioPorCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("El correo no puede estar vacío.");
        }

        Usuario usuario = this.objGestionarUsuarioGateway.buscarUsuarioPorCorreo(correo);
        if (usuario == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró el usuario con ese correo.");
        }

        return usuario;
    }

    @Override
    public Usuario buscarUsuarioPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("El nombre no puede estar vacío.");
        }

        Usuario usuario = this.objGestionarUsuarioGateway.buscarUsuarioPorNombre(nombre);
        if (usuario == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró el usuario con ese nombre.");
        }

        return usuario;
    }

    @Override
    public Usuario buscarSolicitudesUsuarioPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("El código no puede estar vacío.");
        }

        Usuario usuario = this.objGestionarUsuarioGateway.buscarSolicitudesUsuarioPorCodigo(codigo);
        if (usuario == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontraron solicitudes para el usuario.");
        }

        return usuario;
    }

    @Override
    public List<Usuario> buscarUsuariosPorRol(Integer idRol) {
        if (idRol == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un ID de rol válido.");
        }

        return this.objGestionarUsuarioGateway.buscarUsuariosPorRol(idRol);
    }

    @Override
    public List<Usuario> buscarUsuariosPorPrograma(Integer idPrograma) {
        if (idPrograma == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un ID de programa válido.");
        }
        //this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Buscando usuarios por programa con ID: " + idPrograma);

        return this.objGestionarUsuarioGateway.buscarUsuariosPorPrograma(idPrograma);
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return this.objGestionarUsuarioGateway.listarUsuarios();
    }

    @Override
    public Usuario buscarUsuarioPorSolicitud(Integer idSolicitud) {
        if (idSolicitud == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un ID de solicitud válido.");
        }
        Usuario usuario = this.objGestionarUsuarioGateway.buscarUsuarioPorSolicitud(idSolicitud);
        if (usuario == null) {
            this.objFormateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró un usuario con esa solicitud.");
        }

        return usuario;
    }
}
