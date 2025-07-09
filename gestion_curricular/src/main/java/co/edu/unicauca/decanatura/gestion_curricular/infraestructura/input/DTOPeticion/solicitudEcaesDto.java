package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class solicitudEcaesDto  {
     private Integer id_solicitud; // necesario para edición y respuesta
    private String nombre_solicitud;
    private Date fecha_registro_solicitud;

    private String tipoDocumento; // debe coincidir con el Enum, como "CC", "CE"
    private String numero_documento;
    private Date fecha_expedicion;
    private Date fecha_nacimiento;

    private Integer idUsuario; // este es el ID del usuario (clave foránea)
    private String estadoSolicitud; // este campo es util en el get, por que para el post se setea un "enviado" 
    private List<documentoDto> documentos = new ArrayList<>();

}
