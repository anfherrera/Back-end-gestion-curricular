package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadoSolicitudDTORespuesta {
    private Integer id_estado;
    private String estado_actual;
    private Date fecha_registro_estado;
    private SolicitudDTORespuesta objSolicitud;

}
