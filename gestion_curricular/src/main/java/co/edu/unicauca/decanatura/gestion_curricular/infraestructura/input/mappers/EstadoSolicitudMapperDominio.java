package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.EstadoSolicitudDTORespuesta;

@Mapper(
    componentModel = "spring"
)
public interface EstadoSolicitudMapperDominio {

    @Mapping(source = "idEstado", target = "id_estado")
    @Mapping(source = "estadoActual", target = "estado_actual")
    @Mapping(source = "fechaRegistroEstado", target = "fecha_registro_estado")
    @Mapping(source = "comentario", target = "comentario")
    @Mapping(target = "objSolicitud", ignore = true)
    EstadoSolicitudDTORespuesta mapearDeEstadoSolicitudARespuesta(EstadoSolicitud estadoSolicitud);

    List<EstadoSolicitudDTORespuesta> mapearListaDeEstadosARespuesta(List<EstadoSolicitud> estados);
}
