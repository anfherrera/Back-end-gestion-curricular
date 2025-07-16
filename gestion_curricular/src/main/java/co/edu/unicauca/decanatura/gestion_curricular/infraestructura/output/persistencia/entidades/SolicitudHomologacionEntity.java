package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.entidades;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "Solicitudes_Homogolacion")
@Data
@EqualsAndHashCode(callSuper = true)
public class SolicitudHomologacionEntity extends SolicitudEntity {
    public SolicitudHomologacionEntity(){
        super();
    }
    public SolicitudHomologacionEntity(Integer id_solicitud, String nombre_solicitud, Date fecha_registro_solicitud, boolean esSeleccionado,
                                List<EstadoSolicitudEntity> estadosSolicitud, UsuarioEntity objUsuario, List<DocumentoEntity> documentos) {
        super(id_solicitud, nombre_solicitud, fecha_registro_solicitud, esSeleccionado, estadosSolicitud, objUsuario, documentos);
    }
}
