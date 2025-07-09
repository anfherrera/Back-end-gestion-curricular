package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import org.mapstruct.Mapper;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Docente;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.DocenteDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.DocenteDTOPeticion;

@Mapper(componentModel = "spring")
public interface DocenteMapperDominio {

    // Dominio -> DTO de respuesta
    DocenteDTORespuesta mappearDeDocenteADocenteDTORespuesta(Docente docente);

    List<DocenteDTORespuesta> mappearDeDocentesADocentesDTORespuesta(List<Docente> docentes);

    // DTO de petición -> Dominio
    Docente mappearDeDocenteDTOPeticionADocente(DocenteDTOPeticion peticion);

    List<Docente> mappearDeDocenteDTOPeticionADocentes(List<DocenteDTOPeticion> peticiones);
}
