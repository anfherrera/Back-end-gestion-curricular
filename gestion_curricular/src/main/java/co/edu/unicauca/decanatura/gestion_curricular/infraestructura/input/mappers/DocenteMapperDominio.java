package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import org.mapstruct.Mapper;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.DocenteDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.DocenteDTORespuesta;

@Mapper(componentModel = "spring")
public interface DocenteMapperDominio {

    // Dominio -> DTO de respuesta
    DocenteDTORespuesta mapearDeDocenteADocenteDTORespuesta(Docente docente);

    // DTO de petición -> Dominio
    Docente mapearDeDocenteDTOPeticionADocente(DocenteDTOPeticion peticion);

}
