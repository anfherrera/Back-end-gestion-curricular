package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.SolicitudHomologacionDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.SolicitudHomologacionDTORespuesta;

@Mapper(
    componentModel = "spring",
    uses = {
        EstadoSolicitudMapperDominio.class,
        UsuarioMapperDominio.class,
        DocumentosMapperDominio.class
    }
)
public interface SolicitudHomologacionMapperDominio {
    //DTO de petición → Dominio
    @Mapping(target = "documentos", ignore = true)
    @Mapping(target = "estadosSolicitud", ignore = true)
    @Mapping(target = "objCursoOfertadoVerano", ignore = true) // Propiedad no mapeada
    @Mapping(target = "ruta_PM_FO_4_FOR_27", ignore = true) // Propiedad no mapeada
    @Mapping(target = "ruta_contenido_programatico", ignore = true) // Propiedad no mapeada
    @Mapping(target = "objUsuario.objRol", ignore = true) // Propiedad no mapeada en Usuario
    @Mapping(target = "objUsuario.objPrograma", ignore = true) // Propiedad no mapeada en Usuario
    @Mapping(target = "objUsuario.solicitudes", ignore = true) // Propiedad no mapeada en Usuario
    @Mapping(target = "objUsuario.cursosOfertadosInscritos", ignore = true) // Propiedad no mapeada en Usuario
    // OPC-2: el JSON conserva id_usuario, nombre_completo y estado_usuario
    @Mapping(target = "objUsuario.idUsuario", source = "objUsuario.id_usuario")
    @Mapping(target = "objUsuario.nombreCompleto", source = "objUsuario.nombre_completo")
    @Mapping(target = "objUsuario.estadoUsuario", source = "objUsuario.estado_usuario")
    SolicitudHomologacion mapearDeSolicitudHomologacionDTOPeticionASolicitudHomologacion(SolicitudHomologacionDTOPeticion peticion);

    // Dominio → DTO Respuesta
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "tipo_solicitud", ignore = true)
    SolicitudHomologacionDTORespuesta mapearDeSolicitudHomologacionASolicitudHomologacionDTORespuesta(SolicitudHomologacion solicitudHomologacion);

    List<SolicitudHomologacionDTORespuesta> mapearListaDeSolicitudHomologacionARespuesta(List<SolicitudHomologacion> solicitudes);
}
