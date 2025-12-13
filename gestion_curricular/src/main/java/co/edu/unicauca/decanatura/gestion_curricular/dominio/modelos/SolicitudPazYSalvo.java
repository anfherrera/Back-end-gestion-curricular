package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SolicitudPazYSalvo extends Solicitud {
    public SolicitudPazYSalvo(){
        super();
    }
    public SolicitudPazYSalvo(Integer id_solicitud, String nombre_solicitud, String periodo_academico, Date fecha_ceremonia, Date fecha_registro_solicitud, Boolean esSeleccionado,
                                List<EstadoSolicitud> estadosSolicitud, Usuario objUsuario, List<Documento> documentos, CursoOfertadoVerano objCursoOfertadoVerano) {
        super(id_solicitud, nombre_solicitud, periodo_academico, fecha_ceremonia, fecha_registro_solicitud, esSeleccionado, estadosSolicitud, objUsuario, documentos, objCursoOfertadoVerano);
    }
}
