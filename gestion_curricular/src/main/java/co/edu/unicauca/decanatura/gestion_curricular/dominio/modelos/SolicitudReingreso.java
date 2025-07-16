package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class SolicitudReingreso extends Solicitud {
    public SolicitudReingreso(){
        super();
    }
    public SolicitudReingreso(Integer id_solicitud, String nombre_solicitud, Date fecha_registro_solicitud, boolean esSeleccionado,
                                List<EstadoSolicitud> estadosSolicitud, Usuario objUsuario, List<Documento> documentos) {
        super(id_solicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, estadosSolicitud, objUsuario, documentos);
    }
}
