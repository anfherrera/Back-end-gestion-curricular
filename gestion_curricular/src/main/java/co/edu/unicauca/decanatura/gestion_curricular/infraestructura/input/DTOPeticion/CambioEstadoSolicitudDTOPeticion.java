package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CambioEstadoSolicitudDTOPeticion {
    private Integer idSolicitud;
    @NotBlank(message = "{Solicitud.estado.empty}")
    @Size(min = 1, max = 9, message = "{Solicitud.estado.length}")
    private String nuevoEstado;
    //private String observacion; // opcional
    
}
