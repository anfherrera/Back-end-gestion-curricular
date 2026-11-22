package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.EstadoCursoOfertado;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.EstadoCursoOfertadoDTOPeticion;

@Mapper(
    componentModel = "spring"
)

public interface EstadoCursoOfertadoMapper {

    @Mapping(source = "id_estado", target = "idEstado")
    @Mapping(source = "estado_actual", target = "estadoActual")
    @Mapping(target = "fechaRegistroEstado", ignore = true)
    @Mapping(target = "objCursoOfertadoVerano", ignore = true)
    EstadoCursoOfertado mapearDeDTOPeticionAEstadoCursoOfertado(EstadoCursoOfertadoDTOPeticion dtoPeticion);


    
}
