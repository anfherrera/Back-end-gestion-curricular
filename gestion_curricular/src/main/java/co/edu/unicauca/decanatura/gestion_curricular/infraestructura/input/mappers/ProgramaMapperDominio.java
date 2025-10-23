package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import org.mapstruct.Mapper;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.ProgramaDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.ProgramaDTOPeticion;

@Mapper(componentModel = "spring")
public interface ProgramaMapperDominio {

    // Dominio -> DTO de respuesta
    ProgramaDTORespuesta mappearDeProgramaAProgramaDTORespuesta(Programa programa);

    List<ProgramaDTORespuesta> mappearDeProgramasAProgramasDTORespuesta(List<Programa> programas);

    // DTO de petición -> Dominio
    Programa mappearDeProgramaDTOPeticionAPrograma(ProgramaDTOPeticion peticion);

    List<Programa> mappearDeProgramaDTOPeticionAProgramas(List<ProgramaDTOPeticion> peticiones);
}
