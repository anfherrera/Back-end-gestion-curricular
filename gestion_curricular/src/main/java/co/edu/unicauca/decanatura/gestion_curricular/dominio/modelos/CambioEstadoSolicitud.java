package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CambioEstadoSolicitud {
    private Integer idSolicitud;
    private String nuevoEstado;
    private String comentario; // Comentario o razón del cambio de estado (especialmente para rechazos)
    
    public CambioEstadoSolicitud() {
        // Constructor por defecto
    }
}
