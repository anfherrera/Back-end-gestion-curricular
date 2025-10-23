package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.input;

import java.util.List;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;

public interface GestionarRolesCUIntPort {

    Rol guardarRol(Rol rol);

    Rol actualizarRol(Rol rol);

    boolean eliminarRol(Integer idRol);

    Rol obtenerRolPorId(Integer idRol);

    Rol obtenerRolPorNombre(String nombre);

    boolean existeRolPorNombre(String nombre);

    List<Rol> listarRoles();
}

