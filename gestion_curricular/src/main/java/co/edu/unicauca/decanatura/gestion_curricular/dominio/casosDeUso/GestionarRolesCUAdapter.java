package co.edu.unicauca.decanatura.gestion_curricular.dominio.casosDeUso;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input.GestionarRolesCUIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.FormateadorResultadosIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output.GestionarRolGatewayIntPort;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;

public class GestionarRolesCUAdapter implements GestionarRolesCUIntPort {

    private final GestionarRolGatewayIntPort rolesGateway;
    private final FormateadorResultadosIntPort formateadorResultados;

    public GestionarRolesCUAdapter(GestionarRolGatewayIntPort rolesGateway,
                                   FormateadorResultadosIntPort formateadorResultados) {
        this.rolesGateway = rolesGateway;
        this.formateadorResultados = formateadorResultados;
    }

    @Override
    public Rol guardarRol(Rol rol) {
        if (rol == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("El rol no puede ser nulo.");
        }

        rol.setId_rol(null);

        if (rolesGateway.existeRolPorNombre(rol.getNombre())) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Ya existe un rol con este nombre.");
        }

        return rolesGateway.guardarRol(rol);
    }

    @Override
    public Rol actualizarRol(Rol rol) {
        if (rol == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("El rol no puede ser nulo.");
        }
        if (rol.getId_rol() == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un ID válido para actualizar.");
        }
        Rol existente = rolesGateway.obtenerRolPorId(rol.getId_rol());
        if (existente == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró el rol a actualizar.");
        }
        return rolesGateway.actualizarRol(rol);
    }

    @Override
    public boolean eliminarRol(Integer idRol) {
        if (idRol == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un ID válido.");
        }

        Rol existente = rolesGateway.obtenerRolPorId(idRol);
        if (existente == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("No se encontró el rol a eliminar.");
        }

        // VALIDACIÓN: Verificar si el rol tiene usuarios asociados
        if (rolesGateway.tieneUsuariosAsociados(idRol)) {
            formateadorResultados.retornarRespuestaErrorReglaDeNegocio(
                "No se puede eliminar el rol porque tiene usuarios asociados. " +
                "Por favor, reasigne los usuarios a otro rol antes de eliminar."
            );
        }

        return rolesGateway.eliminarRol(idRol);
    }

    @Override
    public Rol obtenerRolPorId(Integer idRol) {
        if (idRol == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un ID válido.");
        }

        Rol rol = rolesGateway.obtenerRolPorId(idRol);
        if (rol == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Rol no encontrado.");
        }

        return rol;
    }

    @Override
    public Rol obtenerRolPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un nombre válido.");
        }

        Rol rol = rolesGateway.obtenerRolPorNombre(nombre);
        if (rol == null) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Rol no encontrado con ese nombre.");
        }

        return rol;
    }

    @Override
    public boolean existeRolPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            formateadorResultados.retornarRespuestaErrorEntidadExiste("Debe proporcionar un nombre válido.");
        }

        return rolesGateway.existeRolPorNombre(nombre);
    }

    @Override
    public List<Rol> listarRoles() {
        return rolesGateway.listarRoles();
    }
}

