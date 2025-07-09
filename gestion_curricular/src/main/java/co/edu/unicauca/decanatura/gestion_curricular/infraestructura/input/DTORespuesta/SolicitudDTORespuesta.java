package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudDTORespuesta {

    private Integer id_solicitud;
    private String nombre_solicitud;
    private Date fecha_registro_solicitud;
    private List<EstadoSolicitudDTORespuesta> estadosSolicitud;
    private UsuarioDTORespuesta objUsuario;
    private List<DocumentosDTORespuesta> documentos;
}
