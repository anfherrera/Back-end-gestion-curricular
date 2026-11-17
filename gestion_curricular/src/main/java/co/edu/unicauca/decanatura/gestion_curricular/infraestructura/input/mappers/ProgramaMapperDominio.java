package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import org.mapstruct.Mapper;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.ProgramaDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.ProgramaDTOPeticion;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
public interface ProgramaMapperDominio {

    // Dominio -> DTO de respuesta
    ProgramaDTORespuesta mapearDeProgramaAProgramaDTORespuesta(Programa programa);

    // DTO de petición -> Dominio
    Programa mapearDeProgramaDTOPeticionAPrograma(ProgramaDTOPeticion peticion);

}
