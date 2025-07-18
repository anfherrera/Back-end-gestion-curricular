package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoIncripcion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudCursoVeranoInscripcionDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudCursoVeranoInscripcionDTORespuesta;



@Mapper(componentModel = "spring", 
uses = {
    EstadoSolicitudMapperDominio.class,
    UsuarioMapperDominio.class,
    DocumentosMapperDominio.class,
    CursosOfertadosMapperDominio.class
})
public interface SolicitudCursoDeVeranoInscripcionMapperDominio {
    
    @Mapping(target = "estadosSolicitud", ignore = true) // No viene en el DTO de petición
    @Mapping(source = "documentos", target = "documentos")
    @Mapping(target = "objCursoOfertadoVerano", source = "objCursoOfertado")
    SolicitudCursoVeranoIncripcion mappearDePeticionASolicitudCursoVeranoIncripcion(SolicitudCursoVeranoInscripcionDTOPeticion peticion);

    @Mapping(target = "estadosSolicitud", source = "estadosSolicitud")
    @Mapping(target = "objUsuario", source = "objUsuario")
    @Mapping(target = "documentos", source = "documentos")
    @Mapping(target =  "objCursoOfertado", source = "objCursoOfertadoVerano")
    @Mapping(target =  "codicion_solicitud", source = "codicion_solicitud")
    SolicitudCursoVeranoInscripcionDTORespuesta mappearDeSolicitudCursoVeranoIncripcionARespuesta(SolicitudCursoVeranoIncripcion solicitudCursoVeranoIncripcion);
    
    List<SolicitudCursoVeranoInscripcionDTORespuesta> mappearDeSolicitudesCursoVeranoIncripcionARespuesta(List<SolicitudCursoVeranoIncripcion> solicitudesCursoVeranoIncripcion);
}