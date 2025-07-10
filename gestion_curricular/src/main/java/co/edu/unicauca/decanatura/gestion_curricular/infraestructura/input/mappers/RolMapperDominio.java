package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.RolDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.RolDTORespuesta;

@Mapper(componentModel = "spring")
public interface RolMapperDominio {

    // DTO Petición -> Dominio
    @Mapping(target = "id_rol", ignore = true) // Se ignora, porque en DTOPeticion no se envía el id
    @Mapping(target = "usuarios", ignore = true) // Se ignora, porque en
    Rol mappearDeRolDTOPeticionARol(RolDTOPeticion peticion);

    // Dominio -> DTO Respuesta
    RolDTORespuesta mappearDeRolARolDTORespuesta(Rol rol);
}
