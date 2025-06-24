package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Solicitud {

    
    private Integer id_solicitud;
    private String nombre_solicitud;
    private Date fecha_registro_solicitud;

    private EstadoSolicitud objEstadoSolicitud;
    private Usuario objUsuario;
}
