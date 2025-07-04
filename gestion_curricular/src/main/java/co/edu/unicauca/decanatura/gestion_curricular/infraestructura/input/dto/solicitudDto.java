package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

public class solicitudDto {
    private Integer id_solicitud;
    private String nombre_solicitud;
    private Date fecha_registro_solicitud;

    private EstadoSolicitud objEstadoSolicitud;

    private Set<UsuarioDto> usuarios;

    public solicitudDto() {
        this.usuarios = new HashSet<UsuarioDto>();
    }
    
}
