package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.ProgramaDTORespuesta;

@Mapper(componentModel = "spring")
public interface ProgramaMapperDominio {

    @Mapping(source = "id_programa", target = "id_programa")
    @Mapping(source = "codigo", target = "codigo")
    @Mapping(source = "nombre_programa", target = "nombre_programa")
    ProgramaDTORespuesta mappearDeProgramaAProgramaDTORespuesta(Programa programa);
}
