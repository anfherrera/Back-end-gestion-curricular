package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.output.persistencia.mappers;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Programa;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Rol;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.solicitudEcaesSalidaDto;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.usuarioSalidaDto;

@Component
public class Mapper {
    
   
    private final ModelMapper modelMapper;

    public Mapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }


    // Método genérico
    public <D, T> D map(final T source, Class<D> destinationType) {
        return modelMapper.map(source, destinationType);
    }
    // // Métodos específicos para SolicitudEcaes
    // public SolicitudEcaes solicitudEcaesDtoADominio(solicitudEcaesSalidaDto dto) {
    //     return modelMapper.map(dto, SolicitudEcaes.class);
    // }

    // public SolicitudDTORespuesta solicitudEcaesDominioADto(SolicitudEcaes solicitud) {
    //     return modelMapper.map(solicitud, SolicitudDTORespuesta.class);
    // }
    // //===========================

    // // Métodos personalizados
    // public Usuario usuarioEntradaDtoAUsuario(usuarioEntradaDto dto) {
    //     Usuario usuario = new Usuario();
    //     usuario.setNombre_completo(dto.getNombre_completo());
    //     usuario.setCodigo(dto.getCodigo());
    //     usuario.setCorreo(dto.getCorreo());
    //     usuario.setPassword(dto.getPassword());
    //     usuario.setEstado_usuario(dto.isEstado_usuario());

    //     Rol rol = new Rol();
    //     rol.setId_rol(dto.getIdRol());
    //     usuario.setObjRol(rol);

    //     Programa programa = new Programa();
    //     programa.setId_programa(dto.getIdPrograma());
    //     usuario.setObjPrograma(programa);

    //     return usuario;
    // }

    // public usuarioSalidaDto usuarioAUsuarioSalidaDto(Usuario usuario) {
    //     usuarioSalidaDto dto = new usuarioSalidaDto();
    //     dto.setId_usuario(usuario.getId_usuario());
    //     dto.setNombre_completo(usuario.getNombre_completo());
    //     dto.setCodigo(usuario.getCodigo());
    //     dto.setCorreo(usuario.getCorreo());
    //     dto.setEstado_usuario(usuario.isEstado_usuario());

    //     if (usuario.getObjRol() != null) {
    //         dto.setNombreRol(usuario.getObjRol().getNombre());
    //     }

    //     if (usuario.getObjPrograma() != null) {
    //         dto.setNombrePrograma(usuario.getObjPrograma().getNombre_programa());
    //     }

    //     return dto;
    // }   
}
