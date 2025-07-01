package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Solicitud {

    
    private Integer id_solicitud;
    private String nombre_solicitud;
    private Date fecha_registro_solicitud;

    private EstadoSolicitud objEstadoSolicitud;

    private Set<Usuario> usuarios;

    public Solicitud() {
        this.usuarios = new HashSet<Usuario>();
    }
    
}
