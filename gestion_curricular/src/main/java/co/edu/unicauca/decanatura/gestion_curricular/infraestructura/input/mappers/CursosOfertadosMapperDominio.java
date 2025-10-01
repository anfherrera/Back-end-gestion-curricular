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
    UsuarioMapperDominio.class,
    EstadoCursoOfertadoMapper.class
})
public interface CursosOfertadosMapperDominio {

    // DTO Petición → Dominio
    @Mapping(source = "objMateria", target = "objMateria")
    @Mapping(source = "objDocente", target = "objDocente")
    @Mapping(source = "grupo", target = "grupo")
    @Mapping(source = "cupo_estimado", target = "cupo_estimado")
    @Mapping(source = "salon", target = "salon")
    @Mapping(target = "estadosCursoOfertados", ignore = true)
    @Mapping(target = "estudiantesInscritos", ignore = true)
    @Mapping(target = "solicitudes" , ignore = true)
    CursoOfertadoVerano mappearDeDTOPeticionACursoOfertado(CursosOfertadosDTOPeticion peticion);

    // Dominio → DTO Respuesta
    @Mapping(source = "objMateria.codigo", target = "codigo_curso")
    @Mapping(source = "objMateria.nombre", target = "nombre_curso")
    @Mapping(source = "objMateria", target = "objMateria")
    @Mapping(source = "objDocente", target = "objDocente")
    @Mapping(source = "grupo", target = "grupo")
    @Mapping(source = "cupo_estimado", target = "cupo_estimado")
    @Mapping(source = "salon", target = "salon")
    @Mapping(source = "salon", target = "espacio_asignado")
    @Mapping(source = "cupo_estimado", target = "cupo_maximo")
    @Mapping(source = "cupo_estimado", target = "cupo_disponible")
    @Mapping(source = "estadosCursoOfertados", target = "estadosCursoOfertados")
    @Mapping(source = "estudiantesInscritos", target = "estudiantesInscritos")
    @Mapping(target = "descripcion", constant = "Curso de verano")
    @Mapping(target = "fecha_inicio", constant = "2024-06-01T08:00:00Z")
    @Mapping(target = "fecha_fin", constant = "2024-07-15T17:00:00Z")
    @Mapping(target = "estado", constant = "Preinscripcion")
    CursosOfertadosDTORespuesta mappearDeCursoOfertadoARespuesta(CursoOfertadoVerano curso);

    // Listas
    List<CursosOfertadosDTORespuesta> mappearListaDeCursoOfertadoARespuesta(List<CursoOfertadoVerano> cursos);
    List<CursoOfertadoVerano> mappearListaDeDTOPeticionACursoOfertado(List<CursosOfertadosDTOPeticion> peticiones);
}
