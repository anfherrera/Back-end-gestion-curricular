package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Notificacion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.NotificacionDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.NotificacionDTOPeticion;

@Mapper(
    componentModel = "spring",
    uses = {
        UsuarioMapperDominio.class,
        SolicitudMapperDominio.class
    }
)
public interface NotificacionMapperDominio {

    // Modelo → DTO Respuesta
    @Mapping(source = "id_notificacion", target = "id_notificacion")
    @Mapping(source = "tipoSolicitud", target = "tipoSolicitud")
    @Mapping(source = "tipoNotificacion", target = "tipoNotificacion")
    @Mapping(source = "titulo", target = "titulo")
    @Mapping(source = "mensaje", target = "mensaje")
    @Mapping(source = "fechaCreacion", target = "fechaCreacion")
    @Mapping(source = "leida", target = "leida")
    @Mapping(source = "esUrgente", target = "esUrgente")
    @Mapping(source = "accion", target = "accion")
    @Mapping(source = "urlAccion", target = "urlAccion")
    @Mapping(source = "objUsuario.id_usuario", target = "idUsuario")
    @Mapping(source = "objUsuario.nombre_completo", target = "nombreUsuario")
    @Mapping(source = "objUsuario.correo", target = "emailUsuario")
    @Mapping(source = "objSolicitud.id_solicitud", target = "idSolicitud")
    @Mapping(source = "objSolicitud.nombre_solicitud", target = "nombreSolicitud")
    @Mapping(source = "objCurso.id_curso", target = "idCurso")
    @Mapping(expression = "java(notificacion.getObjCurso() != null && notificacion.getObjCurso().getObjMateria() != null ? notificacion.getObjCurso().getObjMateria().getNombre() : null)", target = "nombreMateria")
    @Mapping(expression = "java(notificacion.getObjCurso() != null && notificacion.getObjCurso().getObjMateria() != null ? notificacion.getObjCurso().getObjMateria().getCodigo() + \" - \" + notificacion.getObjCurso().getObjMateria().getNombre() : null)", target = "nombreCurso")
    NotificacionDTORespuesta mappearDeNotificacionARespuesta(Notificacion notificacion);

    List<NotificacionDTORespuesta> mappearListaDeNotificacionARespuesta(List<Notificacion> notificaciones);

    // DTO Petición → Modelo (parcial, ya que necesita resolverse las relaciones)
    @Mapping(target = "id_notificacion", ignore = true) // Se genera automáticamente
    @Mapping(target = "fechaCreacion", ignore = true) // Se establece en el servicio
    @Mapping(target = "objUsuario", ignore = true) // Se debe resolver del ID
    @Mapping(target = "objSolicitud", ignore = true) // Se debe resolver del ID
    @Mapping(target = "objCurso", ignore = true) // Se debe resolver del ID
    Notificacion mappearDeNotificacionDTOPeticionANotificacion(NotificacionDTOPeticion peticion);
}


