package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarRolesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarRolGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.EntidadYaExisteException;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.excepciones.ReglaNegocioExcepcion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;

public class GestionarRolesCUAdapter implements GestionarRolesCUIntPort {

    private final GestionarRolGatewayIntPort rolesGateway;

    public GestionarRolesCUAdapter(GestionarRolGatewayIntPort rolesGateway) {
        this.rolesGateway = rolesGateway;
    }

    @Override
    public Rol guardarRol(Rol rol) {
        if (rol == null) {
            throw new EntidadYaExisteException("El rol no puede ser nulo.");
        }

        rol.setId_rol(null);

        if (rolesGateway.existeRolPorNombre(rol.getNombre())) {
            throw new EntidadYaExisteException("Ya existe un rol con este nombre.");
        }

        return rolesGateway.guardarRol(rol);
    }

    @Override
    public Rol actualizarRol(Rol rol) {
        if (rol == null) {
            throw new EntidadYaExisteException("El rol no puede ser nulo.");
        }
        if (rol.getId_rol() == null) {
            throw new EntidadYaExisteException("Debe proporcionar un ID válido para actualizar.");
        }
        Rol existente = rolesGateway.obtenerRolPorId(rol.getId_rol());
        if (existente == null) {
            throw new EntidadYaExisteException("No se encontró el rol a actualizar.");
        }
        return rolesGateway.actualizarRol(rol);
    }

    @Override
    public boolean eliminarRol(Integer idRol) {
        if (idRol == null) {
            throw new EntidadYaExisteException("Debe proporcionar un ID válido.");
        }

        Rol existente = rolesGateway.obtenerRolPorId(idRol);
        if (existente == null) {
            throw new EntidadYaExisteException("No se encontró el rol a eliminar.");
        }

        // Verificar si el rol tiene usuarios asociados antes de eliminarlo
        if (rolesGateway.tieneUsuariosAsociados(idRol)) {
            throw new ReglaNegocioExcepcion(
                "No se puede eliminar el rol porque tiene usuarios asociados. " +
                "Por favor, reasigne los usuarios a otro rol antes de eliminar."
            );
        }

        return rolesGateway.eliminarRol(idRol);
    }

    @Override
    public Rol obtenerRolPorId(Integer idRol) {
        if (idRol == null) {
            throw new EntidadYaExisteException("Debe proporcionar un ID válido.");
        }

        Rol rol = rolesGateway.obtenerRolPorId(idRol);
        if (rol == null) {
            throw new EntidadYaExisteException("Rol no encontrado.");
        }

        return rol;
    }

    @Override
    public Rol obtenerRolPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new EntidadYaExisteException("Debe proporcionar un nombre válido.");
        }

        Rol rol = rolesGateway.obtenerRolPorNombre(nombre);
        if (rol == null) {
            throw new EntidadYaExisteException("Rol no encontrado con ese nombre.");
        }

        return rol;
    }

    @Override
    public List<Rol> listarRoles() {
        return rolesGateway.listarRoles();
    }
}

