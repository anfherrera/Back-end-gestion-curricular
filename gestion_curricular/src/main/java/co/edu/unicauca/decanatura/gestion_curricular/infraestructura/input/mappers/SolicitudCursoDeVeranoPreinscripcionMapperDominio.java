package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudCursoVeranoPreinscripcion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudCurosoVeranoPreinscripcionDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudCursoVeranoPreinscripcionDTORespuesta;

@Mapper(componentModel = "spring", uses = {
    EstadoSolicitudMapperDominio.class,
    UsuarioMapperDominio.class,
    CursosOfertadosMapperDominio.class
})

public interface SolicitudCursoDeVeranoPreinscripcionMapperDominio {
    @Mapping(target = "estadosSolicitud", ignore = true) // No se incluye en la petición
    @Mapping(target = "documentos", ignore = true) // Map documentos collection
    SolicitudCursoVeranoPreinscripcion mappearDePeticionASolicitudCursoVeranoPreinscripcion(
        SolicitudCurosoVeranoPreinscripcionDTOPeticion peticion);

    @Mapping(target = "estadosSolicitud", source = "estadosSolicitud")
    @Mapping(target = "objUsuario", source = "objUsuario")
    @Mapping(target = "objCursoOfertado", source = "objCursoOfertado")
    @Mapping(target = "codicion_solicitud", source = "codicion_solicitud")
    @Mapping(target = "nombre_estudiante", source = "nombre_estudiante")
    @Mapping(target = "observacion", source = "observacion")
    SolicitudCursoVeranoPreinscripcionDTORespuesta mappearDeSolicitudCursoVeranoPreinscripcionARespuesta(
        SolicitudCursoVeranoPreinscripcion solicitud);

    List<SolicitudCursoVeranoPreinscripcionDTORespuesta> mappearDeListaSolicitudesCursoVeranoPreinscripcionARespuesta(
        List<SolicitudCursoVeranoPreinscripcion> solicitudes);
        
}
