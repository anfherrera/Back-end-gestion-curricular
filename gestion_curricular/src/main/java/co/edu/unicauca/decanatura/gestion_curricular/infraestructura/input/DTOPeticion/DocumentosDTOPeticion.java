package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;

import java.util.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PastOrPresent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentosDTOPeticion {

    private Integer id_documento;

    @NotBlank(message = "{Documento.nombre.empty}")
    @Size(min = 3, max = 100, message = "{Documento.nombre.length}")
    private String nombre;

    @NotBlank(message = "{Documento.ruta.empty}")
    @Size(min = 5, max = 255, message = "{Documento.ruta.length}")
    private String ruta_documento;

    @NotNull(message = "{Documento.fecha.empty}")
    @PastOrPresent(message = "{Documento.fecha.pastorpresent}")
    private Date fecha_documento;

    private boolean esValido;

    @NotBlank(message = "{Documento.comentario.empty}")
    @Size(min = 5, max = 200, message = "{Documento.comentario.length}")
    private String comentario;//El estudiante no sube esto

    @NotBlank(message = "{Documento.tipo.empty}")
    @Size(min = 3, max = 50, message = "{Documento.tipo.length}")
    private String tipoDocumentoSolicitudPazYSalvo;
}
