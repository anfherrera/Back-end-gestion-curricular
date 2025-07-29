package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Enums.EstadoSolicitudEcaes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CambioEstadoSolicitudEcaes {
    private Integer idSolicitud;
    // private String nuevoEstado;
    // private String observacion; // opcional
    private EstadoSolicitudEcaes nuevoEstado;
}
