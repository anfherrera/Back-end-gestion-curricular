package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import org.mapstruct.Mapper;
import java.util.List;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.RolDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.RolDTOPeticion;

@Mapper(componentModel = "spring")
public interface RolMapperDominio {

    // Dominio -> DTO de respuesta
    RolDTORespuesta mappearDeRolARolDTORespuesta(Rol rol);

    List<RolDTORespuesta> mappearDeRolesARolesDTORespuesta(List<Rol> roles);

    // DTO de petición -> Dominio
    Rol mappearDeRolDTOPeticionARol(RolDTOPeticion peticion);

    List<Rol> mappearDeRolDTOPeticionARoles(List<RolDTOPeticion> peticiones);
}
