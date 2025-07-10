package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.CursosOfertadosDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.CursosOfertadosDTORespuesta;

@Mapper(componentModel = "spring", uses = {
    MateriaMapperDominio.class,
    DocenteMapperDominio.class,
    UsuarioMapperDominio.class
})
public interface CursosOfertadosMapperDominio {

    // DTO Petición → Dominio
    @Mapping(source = "objMateria", target = "objMateria")
    @Mapping(source = "objDocente", target = "objDocente")
    @Mapping(source = "grupo", target = "grupo")
    @Mapping(source = "cupo_estimado", target = "cupo_estimado")
    @Mapping(source = "salon", target = "salon")
    @Mapping(target = "estadosCursoOfertados", ignore =  true)
    @Mapping(source = "estudiantesInscritos", target = "estudiantesInscritos")
    CursoOfertadoVerano mappearDeDTOPeticionACursoOfertado(CursosOfertadosDTOPeticion peticion);

    // Dominio → DTO Respuesta
    @Mapping(source = "objMateria", target = "objMateria")
    @Mapping(source = "objDocente", target = "objDocente")
    @Mapping(source = "grupo", target = "grupo")
    @Mapping(source = "cupo_estimado", target = "cupo_estimado")
    @Mapping(source = "salon", target = "salon")
    @Mapping(source = "estadosCursoOfertados", target = "estadosCursoOfertados")
    @Mapping(source = "estudiantesInscritos", target = "estudiantesInscritos")
    CursosOfertadosDTORespuesta mappearDeCursoOfertadoARespuesta(CursoOfertadoVerano curso);

    // Listas
    List<CursosOfertadosDTORespuesta> mappearListaDeCursoOfertadoARespuesta(List<CursoOfertadoVerano> cursos);
    List<CursoOfertadoVerano> mappearListaDeDTOPeticionACursoOfertado(List<CursosOfertadosDTOPeticion> peticiones);
}
