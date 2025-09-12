package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CambioEstadoSolicitud {
    private Integer idSolicitud;
    private String nuevoEstado;
    // private String observacion; // opcional
    //private EstadoSolicitudEcaes nuevoEstado;
}
