package co.edu.unicauca.decanatura.gestion_curricular.aplicacion.output;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;

public interface GestionarRolGatewayIntPort {
    
    Rol bucarRolPorId(Integer idRol);
    Rol buscarRolPorNombre(String nombre);
}
