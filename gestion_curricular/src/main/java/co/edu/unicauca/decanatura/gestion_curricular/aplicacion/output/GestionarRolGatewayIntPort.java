package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import java.util.List;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;

public interface GestionarRolGatewayIntPort {
    
    // Métodos originales (mantener compatibilidad)
    Rol bucarRolPorId(Integer idRol);
    Rol buscarRolPorNombre(String nombre);
    
    // Nuevos métodos CRUD
    Rol guardarRol(Rol rol);

    Rol actualizarRol(Rol rol);

    boolean eliminarRol(Integer idRol);

    Rol obtenerRolPorId(Integer idRol);

    Rol obtenerRolPorNombre(String nombre);

    boolean existeRolPorNombre(String nombre);

    List<Rol> listarRoles();
    
    boolean tieneUsuariosAsociados(Integer idRol);
}
