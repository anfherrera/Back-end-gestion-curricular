package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudHomologacionDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudHomologacionDTORespuesta;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {
        EstadoSolicitudMapperDominio.class,
        UsuarioMapperDominio.class,
        DocumentosMapperDominio.class
    }
)
public interface SolicitudHomologacioneMapperDominio {
    //DTO de petición → Dominio
    @Mapping(target = "documentos", ignore = true)
    @Mapping(target = "estadosSolicitud", ignore = true)
    @Mapping(target = "objCursoOfertadoVerano", ignore = true) // Propiedad no mapeada
    @Mapping(target = "ruta_PM_FO_4_FOR_27", ignore = true) // Propiedad no mapeada
    @Mapping(target = "ruta_contenido_programatico", ignore = true) // Propiedad no mapeada
    @Mapping(target = "fecha_ceremonia", ignore = true) // Se asigna desde el caso de uso si es necesario
    @Mapping(target = "objUsuario.cursosOfertadosInscritos", ignore = true)
    @Mapping(target = "objUsuario.objPrograma", ignore = true)
    @Mapping(target = "objUsuario.objRol", ignore = true)
    @Mapping(target = "objUsuario.solicitudes", ignore = true)
    SolicitudHomologacion mappearDeSolicitudHomologacionDTOPeticionASolicitudHomologacion(SolicitudHomologacionDTOPeticion peticion);

    // Dominio → DTO Respuesta
    SolicitudHomologacionDTORespuesta mappearDeSolicitudHomologacionASolicitudHomologacionDTORespuesta(SolicitudHomologacion solicitudHomologacion);

    List<SolicitudHomologacionDTORespuesta> mappearListaDeSolicitudHomologacionARespuesta(List<SolicitudHomologacion> solicitudes);

    List<SolicitudHomologacion> mappearListaDeSolicitudHomologacionDTOPeticionAListaDeSolicitudHomologacion(List<SolicitudHomologacionDTOPeticion> solicitudes);
}
