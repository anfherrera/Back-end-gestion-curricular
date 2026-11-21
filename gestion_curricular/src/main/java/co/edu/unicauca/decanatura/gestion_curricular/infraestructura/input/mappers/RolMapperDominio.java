package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.RolDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.RolDTORespuesta;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
public interface RolMapperDominio {

    // Dominio -> DTO de respuesta
    @Mapping(target = "id_rol", source = "id_rol")
    @Mapping(target = "nombre", source = "nombre")
    RolDTORespuesta mapearDeRolARolDTORespuesta(Rol rol);

    // DTO de petición -> Dominio
    @Mapping(target = "usuarios", ignore = true)
    Rol mapearDeRolDTOPeticionARol(RolDTOPeticion peticion);

}
