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
public class SolicitudCurosoVeranoPreinscripcionDTOPeticion extends SolicitudDTOPeticion {

    @NotBlank(message = "{SolicitudVerano.nombreEstudiante.empty}")
    @Size(min = 3, max = 100, message = "{SolicitudVerano.nombreEstudiante.length}")
    private String nombre_estudiante;

    @NotNull(message = "{SolicitudVerano.objCursoOfertado.empty}")
    @Valid
    private CursosOfertadosDTOPeticion objCursoOfertado;

    @NotBlank(message = "{SolicitudVerano.condicion.empty}")
    @Size(min = 3, max = 50, message = "{SolicitudVerano.condicion.length}")
    private String codicion_solicitud;

    @NotBlank(message = "{SolicitudVerano.observacion.empty}")
    @Size(min = 5, max = 255, message = "{SolicitudVerano.observacion.length}")
    private String observacion;
}
