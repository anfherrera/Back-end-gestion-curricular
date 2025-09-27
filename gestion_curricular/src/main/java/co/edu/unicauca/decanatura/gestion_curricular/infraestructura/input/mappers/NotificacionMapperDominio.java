package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CursoOfertadoVerano;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Notificacion;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.NotificacionDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.NotificacionDTORespuesta;

@Mapper(componentModel = "spring")
public interface NotificacionMapperDominio {

    @Mapping(target = "objUsuario", source = "idUsuario", qualifiedByName = "mapUsuarioFromId")
    @Mapping(target = "objSolicitud", source = "idSolicitud", qualifiedByName = "mapSolicitudFromId")
    @Mapping(target = "objCurso", source = "idCurso", qualifiedByName = "mapCursoFromId")
    Notificacion mappearDeDTOPeticionANotificacion(NotificacionDTOPeticion dto);

    @Mapping(target = "idUsuario", source = "objUsuario.id_usuario")
    @Mapping(target = "nombreUsuario", source = "objUsuario.correo")
    @Mapping(target = "emailUsuario", source = "objUsuario.correo")
    @Mapping(target = "idSolicitud", source = "objSolicitud.id_solicitud")
    @Mapping(target = "nombreSolicitud", source = "objSolicitud.nombre_solicitud")
    @Mapping(target = "idCurso", source = "objCurso.id_curso")
    @Mapping(target = "nombreCurso", source = "objCurso.objMateria.nombre")
    @Mapping(target = "nombreMateria", source = "objCurso.objMateria.nombre")
    NotificacionDTORespuesta mappearDeNotificacionARespuesta(Notificacion notificacion);

    @Mapping(target = "idUsuario", source = "objUsuario.id_usuario")
    @Mapping(target = "idSolicitud", source = "objSolicitud.id_solicitud")
    @Mapping(target = "idCurso", source = "objCurso.id_curso")
    NotificacionDTOPeticion mappearDeNotificacionADTOPeticion(Notificacion notificacion);

    @Named("mapUsuarioFromId")
    default Usuario mapUsuarioFromId(Integer idUsuario) {
        if (idUsuario == null) return null;
        Usuario usuario = new Usuario();
        usuario.setId_usuario(idUsuario);
        return usuario;
    }

    @Named("mapSolicitudFromId")
    default Solicitud mapSolicitudFromId(Integer idSolicitud) {
        if (idSolicitud == null) return null;
        Solicitud solicitud = new Solicitud();
        solicitud.setId_solicitud(idSolicitud);
        return solicitud;
    }

    @Named("mapCursoFromId")
    default CursoOfertadoVerano mapCursoFromId(Integer idCurso) {
        if (idCurso == null) return null;
        CursoOfertadoVerano curso = new CursoOfertadoVerano();
        curso.setId_curso(idCurso);
        return curso;
    }
}
