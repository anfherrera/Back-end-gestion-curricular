package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.EstadoCursoOfertadoDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.EstadoCursoOfertadoDTOPeticion;

@Mapper(
    componentModel = "spring"
)

public interface EstadoCursoOfertadoMapper {

    @Mapping(source = "id_estado", target = "id_estado")
    @Mapping(source = "estado_actual", target = "estado_actual")
    @Mapping(target = "fecha_registro_estado", ignore = true)
    @Mapping(target = "fecha_fin", ignore = true)
    @Mapping(target = "objCursoOfertadoVerano", ignore = true)
    EstadoCursoOfertado mappearDeDTOPeticionAEstadoCursoOfertado(EstadoCursoOfertadoDTOPeticion dtoPeticion);

    @Mapping(source = "id_estado", target = "id_estado")
    @Mapping(source = "estado_actual", target = "estado_actual")
    @Mapping(source = "fecha_registro_estado", target = "fecha_registro_estado")
    @Mapping(source = "fecha_fin", target = "fecha_fin")
    @Mapping(target = "objCursoOfertadoVerano", ignore = true)
    EstadoCursoOfertadoDTORespuesta mappearDeEstadoCursoOfertadoARespuesta(EstadoCursoOfertado estadoCursoOfertado);

    List<EstadoCursoOfertadoDTORespuesta> mappearListaDeEstadosARespuesta(List<EstadoCursoOfertado> estados);


    
}
