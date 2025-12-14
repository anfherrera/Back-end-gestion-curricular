package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SolicitudPazYSalvo extends Solicitud {
    
    private String titulo_trabajo_grado; // Título del trabajo de grado sustentado
    private String director_trabajo_grado; // Director del trabajo de grado (ej: "PhD. Carlos Alberto Cobos Lozada")
    
    public SolicitudPazYSalvo(){
        super();
    }
    public SolicitudPazYSalvo(Integer id_solicitud, String nombre_solicitud, String periodo_academico, Date fecha_ceremonia, Date fecha_registro_solicitud,
                                List<EstadoSolicitud> estadosSolicitud, Usuario objUsuario, List<Documento> documentos, CursoOfertadoVerano objCursoOfertadoVerano) {
        super(id_solicitud, nombre_solicitud, periodo_academico, fecha_ceremonia, fecha_registro_solicitud, estadosSolicitud, objUsuario, documentos, objCursoOfertadoVerano);
    }
}
