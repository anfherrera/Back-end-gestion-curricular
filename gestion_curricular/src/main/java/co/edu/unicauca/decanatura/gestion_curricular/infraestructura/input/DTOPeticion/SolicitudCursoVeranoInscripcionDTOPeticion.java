package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SolicitudCursoVeranoInscripcionDTOPeticion extends SolicitudDTOPeticion {

    @NotBlank(message = "{SolicitudVeranoInsc.nombreEstudiante.empty}")
    @Size(min = 3, max = 100, message = "{SolicitudVeranoInsc.nombreEstudiante.length}")
    private String nombre_estudiante;

    @NotNull(message = "{SolicitudVeranoInsc.objCursoOfertado.empty}")
    @Valid
    private CursosOfertadosDTOPeticion objCursoOfertado;

    @NotBlank(message = "{SolicitudVeranoInsc.condicion.empty}")
    @Size(min = 3, max = 50, message = "{SolicitudVeranoInsc.condicion.length}")
    private String codicion_solicitud;

    @NotBlank(message = "{SolicitudVeranoInsc.observacion.empty}")
    @Size(min = 5, max = 255, message = "{SolicitudVeranoInsc.observacion.length}")
    private String observacion;
}
