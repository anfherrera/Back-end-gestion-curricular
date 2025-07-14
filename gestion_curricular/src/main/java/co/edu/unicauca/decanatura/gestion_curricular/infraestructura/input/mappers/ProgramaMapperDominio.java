package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.ProgramaDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.ProgramaDTOPeticion;

@Mapper(componentModel = "spring")
public interface ProgramaMapperDominio {

    // Modelo → DTO Respuesta
    @Mapping(source = "id_programa", target = "id_programa")
    @Mapping(source = "codigo", target = "codigo")
    @Mapping(source = "nombre_programa", target = "nombre_programa")
    ProgramaDTORespuesta mappearDeProgramaAProgramaDTORespuesta(Programa programa);

    // DTO Petición → Modelo
    @Mapping(source = "id_programa", target = "id_programa")
    @Mapping(source = "codigo", target = "codigo")
    @Mapping(source = "nombre_programa", target = "nombre_programa")
    @Mapping(target = "usuarios", ignore = true)
    Programa mappearDeProgramaDTOPeticionAPrograma(ProgramaDTOPeticion peticion);

    // Listas (opcional)
    List<Programa> mappearListaDeProgramaDTOPeticionAPrograma(List<ProgramaDTOPeticion> peticiones);

    List<ProgramaDTORespuesta> mappearListaDeProgramaAProgramaDTORespuesta(List<Programa> programas);
}
