package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.CambioEstadoSolicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.CambioEstadoSolicitudDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.SolicitudDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.SolicitudDTORespuesta;

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
    SolicitudDTORespuesta mapearDeSolicitudARespuesta(Solicitud solicitud);

    List<SolicitudDTORespuesta> mapearListaDeSolicitudesARespuesta(List<Solicitud> solicitudes);

    // DTO Petición → Modelo
    @Mapping(target = "estadosSolicitud", ignore = true) // No viene en el DTO de petición
    @Mapping(target = "objCursoOfertadoVerano", ignore = true) 
    @Mapping(target = "documentos", ignore = true)
    Solicitud mapearDeSolicitudDTOPeticionASolicitud(SolicitudDTOPeticion peticion);

    CambioEstadoSolicitud mapearDeCambioEstadoSolicitudDTOPeticionACambioEstadoSolicitud(CambioEstadoSolicitudDTOPeticion solicitudPeticion);

}
