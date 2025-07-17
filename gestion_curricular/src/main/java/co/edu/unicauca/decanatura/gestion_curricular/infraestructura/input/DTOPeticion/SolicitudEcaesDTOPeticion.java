package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import java.util.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PastOrPresent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SolicitudEcaesDTOPeticion extends SolicitudDTOPeticion {

    @NotBlank(message = "{SolicitudEcaes.tipoDocumento.empty}")
    @Size(min = 2, max = 20, message = "{SolicitudEcaes.tipoDocumento.length}")
    private String tipoDocumento;

    @NotBlank(message = "{SolicitudEcaes.numeroDocumento.empty}")
    @Size(min = 5, max = 20, message = "{SolicitudEcaes.numeroDocumento.length}")
    private String numero_documento;

    @NotNull(message = "{SolicitudEcaes.fechaExpedicion.empty}")
    @PastOrPresent(message = "{SolicitudEcaes.fechaExpedicion.pastorpresent}")
    private Date fecha_expedicion;

    @NotNull(message = "{SolicitudEcaes.fechaNacimiento.empty}")
    @PastOrPresent(message = "{SolicitudEcaes.fechaNacimiento.pastorpresent}")
    private Date fecha_nacimiento;
}
