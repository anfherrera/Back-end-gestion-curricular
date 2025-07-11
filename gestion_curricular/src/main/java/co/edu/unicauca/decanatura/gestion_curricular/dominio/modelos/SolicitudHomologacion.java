package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class SolicitudHomologacion extends Solicitud {
    public SolicitudHomologacion(){
        super();
    }
    public SolicitudHomologacion(Integer id_solicitud, String nombre_solicitud, Date fecha_registro_solicitud,
                                List<EstadoSolicitud> objEstadosSolicitud, Usuario objUsuario, List<Documento> documentos) {
        super(id_solicitud, nombre_solicitud, fecha_registro_solicitud, objEstadosSolicitud, objUsuario, documentos);
    }
}
