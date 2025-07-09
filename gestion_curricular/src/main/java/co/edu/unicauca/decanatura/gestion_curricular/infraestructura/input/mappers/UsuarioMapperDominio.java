package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.UsuarioDTORespuesta;

@Mapper(componentModel = "spring", uses = ProgramaMapperDominio.class)
public interface UsuarioMapperDominio {

    @Mapping(source = "id_usuario", target = "id_usuario")
    @Mapping(source = "nombre_completo", target = "nombre_completo")
    //@Mapping(source = "rol", target = "rol")
    @Mapping(source = "codigo", target = "codigo")
    @Mapping(source = "correo", target = "correo")
    @Mapping(source = "estado_usuario", target = "estado_usuario")
    @Mapping(source = "objPrograma", target = "objPrograma")
    UsuarioDTORespuesta mappearDeUsuarioAUsuarioDTORespuesta(Usuario usuario);

    List<UsuarioDTORespuesta> mappearListaDeUsuarioAUsuarioDTORespuesta(List<Usuario> usuarios);
}
