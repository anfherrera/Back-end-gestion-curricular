package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;


import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.documentoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class solicitudEcaesSalidaDto {
    private Integer id_solicitud;
    private String nombre_solicitud;
    private Date fecha_registro_solicitud;

    private String tipoDocumento;
    private String numero_documento;
    private Date fecha_expedicion;
    private Date fecha_nacimiento;

    private String nombreUsuario;       // en vez de solo ID
    private String correoUsuario;
    private String estadoActual;        // último estado actual (si manejas historial)
    private List<documentoDto> documentos;
}
