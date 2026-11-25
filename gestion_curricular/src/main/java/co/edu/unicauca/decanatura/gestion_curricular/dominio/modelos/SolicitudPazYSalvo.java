package co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import lombok.Data;
import lombok.EqualsAndHashCode;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.enums.EstadosSolicitud;

@Data
@EqualsAndHashCode(callSuper = true)
public class SolicitudPazYSalvo extends Solicitud {
    private String tituloTrabajoGrado;
    private String directorTrabajoGrado;
    
    public SolicitudPazYSalvo(){
        super();
    }
    public SolicitudPazYSalvo(Integer id_solicitud, String nombre_solicitud, String periodo_academico, Date fecha_ceremonia, Date fecha_registro_solicitud,
                                List<EstadoSolicitud> estadosSolicitud, Usuario objUsuario, List<Documento> documentos, CursoOfertadoVerano objCursoOfertadoVerano) {
        super(id_solicitud, nombre_solicitud, periodo_academico, fecha_ceremonia, fecha_registro_solicitud, estadosSolicitud, objUsuario, documentos, objCursoOfertadoVerano);
    }

    /**
     * Comentario del rechazo más reciente por fecha (estado RECHAZADA o RECHAZADO, sin distinguir mayúsculas),
     * recortado. Vacío si no hay rechazos o si el más reciente no tiene comentario, aunque uno anterior sí lo tenga.
     */
    public Optional<String> ultimoComentarioDeRechazo() {
        if (getEstadosSolicitud() == null) {
            return Optional.empty();
        }
        return getEstadosSolicitud().stream()
                .filter(e -> EstadosSolicitud.RECHAZADA.name().equalsIgnoreCase(e.getEstadoActual())
                        || "RECHAZADO".equalsIgnoreCase(e.getEstadoActual()))
                .sorted((a, b) -> b.getFechaRegistroEstado().compareTo(a.getFechaRegistroEstado()))
                .findFirst()
                .map(EstadoSolicitud::getComentario)
                .map(String::trim)
                .filter(comentario -> !comentario.isEmpty());
    }
}
