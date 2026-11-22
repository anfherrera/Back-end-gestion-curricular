package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.DocenteDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.DocenteDTORespuesta;

@Mapper(componentModel = "spring")
public interface DocenteMapperDominio {

    // Dominio -> DTO de respuesta
    @Mapping(target = "id_docente", source = "idDocente")
    @Mapping(target = "codigo_docente", source = "codigoDocente")
    @Mapping(target = "nombre_docente", source = "nombreDocente")
    DocenteDTORespuesta mapearDeDocenteADocenteDTORespuesta(Docente docente);

    // DTO de petición -> Dominio
    @Mapping(target = "idDocente", source = "id_docente")
    @Mapping(target = "codigoDocente", source = "codigo_docente")
    @Mapping(target = "nombreDocente", source = "nombre_docente")
    Docente mapearDeDocenteDTOPeticionADocente(DocenteDTOPeticion peticion);

}
