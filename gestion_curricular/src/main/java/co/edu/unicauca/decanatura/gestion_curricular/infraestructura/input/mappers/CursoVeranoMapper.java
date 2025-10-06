package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Materia;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.CursoVeranoDisponibleDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.CursoDisponibleDTORespuesta;

@Mapper(componentModel = "spring", uses = {
    MateriaMapperDominio.class,
    DocenteMapperDominio.class
})
public interface CursoVeranoMapper {

    // Mapear CursoOfertadoVerano a CursoVeranoDisponibleDTORespuesta
    @Mapping(source = "objMateria.nombre", target = "nombre_curso")
    @Mapping(source = "objMateria.codigo", target = "codigo_curso")
    @Mapping(source = "objMateria.nombre", target = "descripcion")
    @Mapping(source = "cupo_estimado", target = "cupo_maximo")
    @Mapping(source = "cupo_estimado", target = "cupo_disponible")
    @Mapping(source = "salon", target = "espacio_asignado")
    @Mapping(target = "fecha_inicio", expression = "java(java.time.LocalDateTime.now().plusDays(30))")
    @Mapping(target = "fecha_fin", expression = "java(java.time.LocalDateTime.now().plusDays(60))")
    @Mapping(target = "estado", expression = "java(obtenerEstadoActual(curso))")
    CursoVeranoDisponibleDTORespuesta mappearACursoVeranoDisponible(CursoOfertadoVerano curso);

    // Mapear Materia a CursoDisponibleDTORespuesta
    @Mapping(source = "id_materia", target = "id_curso")
    @Mapping(source = "nombre", target = "nombre_curso")
    @Mapping(source = "codigo", target = "codigo_curso")
    @Mapping(source = "creditos", target = "creditos")
    @Mapping(source = "nombre", target = "descripcion")
    CursoDisponibleDTORespuesta mappearACursoDisponible(Materia materia);

    // Métodos helper
    default String obtenerEstadoActual(CursoOfertadoVerano curso) {
        if (curso.getEstadosCursoOfertados() == null || curso.getEstadosCursoOfertados().isEmpty()) {
            return "Abierto";
        }
        return curso.getEstadosCursoOfertados().get(0).getEstado_actual();
    }

    // Listas
    List<CursoVeranoDisponibleDTORespuesta> mappearListaACursoVeranoDisponible(List<CursoOfertadoVerano> cursos);
    List<CursoDisponibleDTORespuesta> mappearListaACursoDisponible(List<Materia> materias);
}
