package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.EstadoSolicitudDTORespuesta;

@Mapper(
    componentModel = "spring"
)
public interface EstadoSolicitudMapperDominio {

    @Mapping(source = "id_estado", target = "id_estado")
    @Mapping(source = "estado_actual", target = "estado_actual")
    @Mapping(source = "fecha_registro_estado", target = "fecha_registro_estado")
    @Mapping(target = "objSolicitud", ignore = true)
    EstadoSolicitudDTORespuesta mappearDeEstadoSolicitudARespuesta(EstadoSolicitud estadoSolicitud);

    List<EstadoSolicitudDTORespuesta> mappearListaDeEstadosARespuesta(List<EstadoSolicitud> estados);
}
