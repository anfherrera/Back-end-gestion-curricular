package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudPazYSalvoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudPazYSalvoDTORespuesta;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {
        EstadoSolicitudMapperDominio.class,
        UsuarioMapperDominio.class,
        DocumentosMapperDominio.class
    }
)
public interface SolicitudPazYSalvoMapperDominio {

    // Modelo → DTO Respuesta
    @Mapping(source = "id_solicitud", target = "id_solicitud")
    @Mapping(source = "nombre_solicitud", target = "nombre_solicitud")
    @Mapping(source = "periodo_academico", target = "periodo_academico")
    @Mapping(source = "fecha_registro_solicitud", target = "fecha_registro_solicitud")
    @Mapping(source = "esSeleccionado", target = "esSeleccionado")
    @Mapping(source = "estadosSolicitud", target = "estadosSolicitud") // CORREGIDO
    @Mapping(source = "objUsuario", target = "objUsuario")
    @Mapping(source = "documentos", target = "documentos") 
    SolicitudPazYSalvoDTORespuesta mappearDeSolicitudARespuesta(SolicitudPazYSalvo solicitud);

    List<SolicitudPazYSalvoDTORespuesta> mappearListaDeSolicitudesARespuesta(List<SolicitudPazYSalvo> solicitudes);

    // DTO Petición → Modelo
    @Mapping(target = "estadosSolicitud", ignore = true) 
    @Mapping(source = "esSeleccionado", target = "esSeleccionado")
    @Mapping(target = "objCursoOfertadoVerano", ignore = true) 
    @Mapping(target = "documentos", ignore = true)
    @Mapping(target = "fecha_ceremonia", ignore = true) // Se asigna desde el caso de uso si es necesario
    SolicitudPazYSalvo mappearDeSolicitudDTOPeticionASolicitud(SolicitudPazYSalvoDTOPeticion peticion);

    List<SolicitudPazYSalvo> mappearListaDeSolicitudDTOPeticionAListaSolicitud(List<SolicitudPazYSalvoDTOPeticion> peticiones);
}
