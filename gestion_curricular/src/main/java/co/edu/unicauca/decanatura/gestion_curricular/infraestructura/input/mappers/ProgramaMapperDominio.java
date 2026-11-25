package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.ProgramaDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.ProgramaDTORespuesta;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
public interface ProgramaMapperDominio {

    // Dominio -> DTO de respuesta
    @Mapping(target = "id_programa", source = "idPrograma")
    @Mapping(target = "nombre_programa", source = "nombrePrograma")
    ProgramaDTORespuesta mapearDeProgramaAProgramaDTORespuesta(Programa programa);

    // DTO de petición -> Dominio
    @Mapping(target = "idPrograma", source = "id_programa")
    @Mapping(target = "nombrePrograma", source = "nombre_programa")
    Programa mapearDeProgramaDTOPeticionAPrograma(ProgramaDTOPeticion peticion);

}
