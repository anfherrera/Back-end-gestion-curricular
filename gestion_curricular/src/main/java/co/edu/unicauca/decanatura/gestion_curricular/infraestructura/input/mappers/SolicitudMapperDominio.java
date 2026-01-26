package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CambioEstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.CambioEstadoSolicitudDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudDTOPeticion;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {
        EstadoSolicitudMapperDominio.class,
        UsuarioMapperDominio.class,
        DocumentosMapperDominio.class
    }
)
public interface SolicitudMapperDominio {

    // Modelo → DTO Respuesta
    @Mapping(source = "id_solicitud", target = "id_solicitud")
    @Mapping(source = "nombre_solicitud", target = "nombre_solicitud")
    @Mapping(source = "periodo_academico", target = "periodo_academico")
    @Mapping(source = "fecha_ceremonia", target = "fecha_ceremonia")
    @Mapping(source = "fecha_registro_solicitud", target = "fecha_registro_solicitud")
    @Mapping(source = "estadosSolicitud", target = "estadosSolicitud")
    @Mapping(source = "objUsuario", target = "objUsuario")
    @Mapping(source = "documentos", target = "documentos")
    SolicitudDTORespuesta mappearDeSolicitudARespuesta(Solicitud solicitud);

    List<SolicitudDTORespuesta> mappearListaDeSolicitudesARespuesta(List<Solicitud> solicitudes);

    // DTO Petición → Modelo
    @Mapping(target = "estadosSolicitud", ignore = true) // No viene en el DTO de petición
    @Mapping(target = "objCursoOfertadoVerano", ignore = true) 
    @Mapping(target = "documentos", ignore = true)
    Solicitud mappearDeSolicitudDTOPeticionASolicitud(SolicitudDTOPeticion peticion);

    List<Solicitud> mappearListaDeSolicitudDTOPeticionAListaSolicitud(List<SolicitudDTOPeticion> peticiones);

    //Cambio para la actualizacion del estado de la solicitud
    CambioEstadoSolicitudDTOPeticion mappearDeCambioEstadoSolicitudACambioEstadoSolicitudDTOPeticion(CambioEstadoSolicitud solicitudPeticion);

    CambioEstadoSolicitud mappearDeCambioEstadoSolicitudDTOPeticionACambioEstadoSolicitud(CambioEstadoSolicitudDTOPeticion solicitudPeticion);

}
