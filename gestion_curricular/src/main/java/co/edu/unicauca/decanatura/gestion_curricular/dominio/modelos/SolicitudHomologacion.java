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
    public SolicitudHomologacion(Integer id_solicitud, String nombre_solicitud, Date fecha_registro_solicitud, Boolean esSeleccionado,
                                List<EstadoSolicitud> estadosSolicitud, Usuario objUsuario, List<Documento> documentos, CursoOfertadoVerano objCursoOfertadoVerano) {
        super(id_solicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, estadosSolicitud, objUsuario, documentos, objCursoOfertadoVerano);
    }
}
