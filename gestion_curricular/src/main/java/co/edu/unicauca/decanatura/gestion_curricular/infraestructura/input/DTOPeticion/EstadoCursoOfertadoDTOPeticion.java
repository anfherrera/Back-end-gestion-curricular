package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import java.util.Date;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.CursosOfertadosDTORespuesta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadoCursoOfertadoDTOPeticion {

    private Integer id_estado;

    @NotBlank(message = "{EstadoCursoOfertado.estado_actual.empty}")
    @Size(min = 3, max = 50, message = "{EstadoCursoOfertado.estado_actual.length}")
    private String estado_actual;

}
