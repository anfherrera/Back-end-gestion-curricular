package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import java.util.Date;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudDTOPeticion {

    private Integer id_solicitud;

    @NotBlank(message = "{Solicitud.nombre.empty}")
    @Size(min = 3, max = 100, message = "{Solicitud.nombre.length}")
    private String nombre_solicitud;

    @NotNull(message = "{Solicitud.fecha.empty}")
    @PastOrPresent(message = "{Solicitud.fecha.pastorpresent}")
    private Date fecha_registro_solicitud;

    @Pattern(regexp = "^\\d{4}-[12]$", message = "{Solicitud.periodo_academico.format}")
    private String periodo_academico; // Período académico en formato YYYY-P (ej: "2024-2")

    private Boolean esSeleccionado;

    @Valid
    private EstadoSolicitudDTOPeticion estado_actual;

    @NotNull(message = "{Solicitud.usuario.empty}")
    @Valid
    private UsuarioDTOPeticion objUsuario;

    @Valid
    private List<DocumentosDTOPeticion> documentos;
}
