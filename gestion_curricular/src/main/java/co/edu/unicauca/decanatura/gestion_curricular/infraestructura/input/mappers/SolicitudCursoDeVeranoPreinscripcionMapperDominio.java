package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudCursoVeranoPreinscripcionDTORespuesta;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {
        EstadoSolicitudMapperDominio.class,
        UsuarioMapperDominio.class,
        CursosOfertadosMapperDominio.class
    }
)

public interface SolicitudCursoDeVeranoPreinscripcionMapperDominio {
    @Mapping(target = "estadosSolicitud", source = "estadosSolicitud")
    @Mapping(target = "objUsuario", source = "objUsuario")
    @Mapping(target = "objCursoOfertado", source = "objCursoOfertadoVerano")
    @Mapping(target = "codicion_solicitud", source = "codicion_solicitud")
    @Mapping(target = "nombre_estudiante", source = "nombre_estudiante")
    @Mapping(target = "observacion", source = "observacion")
    SolicitudCursoVeranoPreinscripcionDTORespuesta mappearDeSolicitudCursoVeranoPreinscripcionARespuesta(
        SolicitudCursoVeranoPreinscripcion solicitud);

}
