package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import org.mapstruct.Mapper;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.ProgramaDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.ProgramaDTOPeticion;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
public interface ProgramaMapperDominio {

    // Dominio -> DTO de respuesta
    ProgramaDTORespuesta mappearDeProgramaAProgramaDTORespuesta(Programa programa);

    // DTO de petición -> Dominio
    Programa mappearDeProgramaDTOPeticionAPrograma(ProgramaDTOPeticion peticion);

}
