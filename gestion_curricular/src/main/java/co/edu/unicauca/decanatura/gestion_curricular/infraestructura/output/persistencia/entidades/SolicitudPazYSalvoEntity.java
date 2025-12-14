package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "Solicitudes_PazYSalvo")
@Data
@EqualsAndHashCode(callSuper = true)
public class SolicitudPazYSalvoEntity extends SolicitudEntity {

    @Column(name = "titulo_trabajo_grado", length = 500, nullable = true)
    private String titulo_trabajo_grado; // Título del trabajo de grado sustentado
    
    @Column(name = "director_trabajo_grado", length = 200, nullable = true)
    private String director_trabajo_grado; // Director del trabajo de grado (ej: "PhD. Carlos Alberto Cobos Lozada")

    public SolicitudPazYSalvoEntity(){
        super();
    }
    public SolicitudPazYSalvoEntity(Integer id_solicitud, String nombre_solicitud, String periodo_academico, Date fecha_ceremonia, Date fecha_registro_solicitud,
                                List<EstadoSolicitudEntity> estadosSolicitud, UsuarioEntity objUsuario, List<DocumentoEntity> documentos, CursoOfertadoVeranoEntity objCursoOfertadoVerano) {
        super(id_solicitud, nombre_solicitud, periodo_academico, fecha_ceremonia, fecha_registro_solicitud, estadosSolicitud, objUsuario, documentos, objCursoOfertadoVerano);
    }
    
}
