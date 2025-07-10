package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.UsuarioDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.UsuarioDTOPeticion;


@Mapper(componentModel = "spring", uses = {ProgramaMapperDominio.class,
        RolMapperDominio.class}) // Incluye el mapper de Rol para mapear el objeto Rol
public interface UsuarioMapperDominio {

    // De modelo → DTO de respuesta
    @Mapping(source = "id_usuario", target = "id_usuario")
    @Mapping(source = "nombre_completo", target = "nombre_completo")
    @Mapping(source = "objRol", target = "rol")
    @Mapping(source = "codigo", target = "codigo")
    @Mapping(source = "correo", target = "correo")
    @Mapping(source = "estado_usuario", target = "estado_usuario")
    @Mapping(source = "objPrograma", target = "objPrograma")
    UsuarioDTORespuesta mappearDeUsuarioAUsuarioDTORespuesta(Usuario usuario);

    List<UsuarioDTORespuesta> mappearListaDeUsuarioAUsuarioDTORespuesta(List<Usuario> usuarios);

    // De DTO de petición → modelo
    @Mapping(target = "id_usuario", ignore = true) // Se ignora, porque en DTOPeticion no se envía el id
    @Mapping(target = "objRol", source = "rol") // Mapea el objeto Rol
    @Mapping(target = "cursosOfertadosInscritos", ignore = true) // Se ignora, porque en DTOPeticion tienes solo el id del programa
    @Mapping(target = "solicitudes", ignore = true) // Se ignora, porque en DTOPeticion tienes solo el id del programa
    Usuario mappearDeUsuarioDTOPeticionAUsuario(UsuarioDTOPeticion usuarioDTOPeticion);

    List<Usuario> mappearListaUsuarioDTOPeticionAListaUsuario(List<UsuarioDTOPeticion> usuarios);
}
