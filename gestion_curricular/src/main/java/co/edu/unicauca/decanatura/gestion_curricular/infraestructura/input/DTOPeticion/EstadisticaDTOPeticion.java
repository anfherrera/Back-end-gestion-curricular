package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticaDTOPeticion {

    private Integer id_estadistica; 

    @NotNull(message = "El proceso es obligatorio")
    private String nombre; 

}
