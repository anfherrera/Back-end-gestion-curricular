package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.DocenteDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.DocenteDTOPeticion;

@Mapper(componentModel = "spring")
public interface DocenteMapperDominio {

    // Dominio -> DTO de respuesta
    @Mapping(source = "id_docente", target = "id_docente")
    @Mapping(source = "codigo_docente", target = "codigo_docente")
    @Mapping(source = "nombre_docente", target = "nombre_docente")
    DocenteDTORespuesta mappearDeDocenteADocenteDTORespuesta(Docente docente);

    List<DocenteDTORespuesta> mappearDeDocentesADocentesDTORespuesta(List<Docente> docentes);

    // DTO de petición -> Dominio
    @Mapping(source = "id_docente", target = "id_docente")
    @Mapping(source = "codigo_docente", target = "codigo_docente")
    @Mapping(source = "nombre_docente", target = "nombre_docente")
    Docente mappearDeDocenteDTOPeticionADocente(DocenteDTOPeticion peticion);

    List<Docente> mappearDeDocenteDTOPeticionADocentes(List<DocenteDTOPeticion> peticiones);
}
