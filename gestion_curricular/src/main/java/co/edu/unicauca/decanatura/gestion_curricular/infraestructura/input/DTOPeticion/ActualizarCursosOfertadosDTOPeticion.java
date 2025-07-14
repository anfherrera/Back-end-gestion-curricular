package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import java.util.List;

import jakarta.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActualizarCursosOfertadosDTOPeticion {
    @Valid
    private CursosOfertadosDTOPeticion curso;
    
    @Valid
    private List<SolicitudDTOPeticion> solicitudes;
}
