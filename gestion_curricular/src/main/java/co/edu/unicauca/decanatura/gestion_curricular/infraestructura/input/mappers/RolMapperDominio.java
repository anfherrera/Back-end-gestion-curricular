package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.RolDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.RolDTOPeticion;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
public interface RolMapperDominio {

    // Dominio -> DTO de respuesta
    @Mapping(target = "id_rol", source = "id_rol")
    @Mapping(target = "nombre", source = "nombre")
    RolDTORespuesta mappearDeRolARolDTORespuesta(Rol rol);

    List<RolDTORespuesta> mappearDeRolesARolesDTORespuesta(List<Rol> roles);

    // DTO de petición -> Dominio
    @Mapping(target = "usuarios", ignore = true)
    Rol mappearDeRolDTOPeticionARol(RolDTOPeticion peticion);

    List<Rol> mappearDeRolDTOPeticionARoles(List<RolDTOPeticion> peticiones);
}
