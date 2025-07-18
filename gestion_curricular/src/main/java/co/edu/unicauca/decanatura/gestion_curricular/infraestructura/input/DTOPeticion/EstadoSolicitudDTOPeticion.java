package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import java.util.Date;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadoSolicitudDTOPeticion {
    
 
    private Integer id_estado;

    @NotBlank(message = "{EstadoSolicitud.estado_actual.empty}")
    @Size(min = 3, max = 30, message = "{EstadoSolicitud.estado_actual.length}")

    private String estado_actual;

   
    @NotNull(message = "{EstadoSolicitud.fecha_registro_estado.empty}")
    private Date fecha_registro_estado;
}
