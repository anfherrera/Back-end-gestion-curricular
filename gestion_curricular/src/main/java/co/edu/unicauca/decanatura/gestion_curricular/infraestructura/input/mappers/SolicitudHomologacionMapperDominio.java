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
    @Mapping(target = "rutaPmFo4For27", ignore = true) // Propiedad no mapeada
    @Mapping(target = "rutaContenidoProgramatico", ignore = true) // Propiedad no mapeada
    @Mapping(target = "objUsuario.objRol", ignore = true) // Propiedad no mapeada en Usuario
    @Mapping(target = "objUsuario.objPrograma", ignore = true) // Propiedad no mapeada en Usuario
    @Mapping(target = "objUsuario.solicitudes", ignore = true) // Propiedad no mapeada en Usuario
    @Mapping(target = "objUsuario.cursosOfertadosInscritos", ignore = true) // Propiedad no mapeada en Usuario
    // OPC-2: el JSON conserva id_usuario, nombre_completo y estado_usuario
    @Mapping(target = "objUsuario.idUsuario", source = "objUsuario.id_usuario")
    @Mapping(target = "objUsuario.nombreCompleto", source = "objUsuario.nombre_completo")
    @Mapping(target = "objUsuario.estadoUsuario", source = "objUsuario.estado_usuario")
    // OPC-2: el DTO conserva el nombre snake_case
    @Mapping(target = "idSolicitud", source = "id_solicitud")
    @Mapping(target = "nombreSolicitud", source = "nombre_solicitud")
    @Mapping(target = "periodoAcademico", source = "periodo_academico")
    @Mapping(target = "fechaCeremonia", source = "fecha_ceremonia")
    @Mapping(target = "fechaRegistroSolicitud", source = "fecha_registro_solicitud")
    // OPC-2: el DTO conserva el nombre snake_case
    @Mapping(target = "programaOrigen", source = "programa_origen")
    @Mapping(target = "programaDestino", source = "programa_destino")
    SolicitudHomologacion mapearDeSolicitudHomologacionDTOPeticionASolicitudHomologacion(SolicitudHomologacionDTOPeticion peticion);

    // Dominio → DTO Respuesta
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "tipo_solicitud", ignore = true)
    // OPC-2: el DTO conserva el nombre snake_case
    @Mapping(target = "id_solicitud", source = "idSolicitud")
    @Mapping(target = "nombre_solicitud", source = "nombreSolicitud")
    @Mapping(target = "periodo_academico", source = "periodoAcademico")
    @Mapping(target = "fecha_ceremonia", source = "fechaCeremonia")
    @Mapping(target = "fecha_registro_solicitud", source = "fechaRegistroSolicitud")
    // OPC-2: el DTO conserva el nombre snake_case
    @Mapping(target = "programa_origen", source = "programaOrigen")
    @Mapping(target = "programa_destino", source = "programaDestino")
    SolicitudHomologacionDTORespuesta mapearDeSolicitudHomologacionASolicitudHomologacionDTORespuesta(SolicitudHomologacion solicitudHomologacion);

    List<SolicitudHomologacionDTORespuesta> mapearListaDeSolicitudHomologacionARespuesta(List<SolicitudHomologacion> solicitudes);
}
