package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticaDTOPeticion {

    private Integer id_estadistica; 

    @NotNull(message = "El proceso es obligatorio")
    private String nombre; 

}
