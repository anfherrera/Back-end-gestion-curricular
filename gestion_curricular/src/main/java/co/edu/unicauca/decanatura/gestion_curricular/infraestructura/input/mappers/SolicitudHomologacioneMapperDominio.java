package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudHomologacion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudHomologacionDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudHomologacionDTORespuesta;


@Mapper(
    componentModel = "spring",
    uses = {
        EstadoSolicitudMapperDominio.class,
        UsuarioMapperDominio.class,
        DocumentosMapperDominio.class
    }
)
public interface SolicitudHomologacioneMapperDominio {
    //DTO de petición → Dominio
    SolicitudHomologacion mappearDeSolicitudHomologacionDTOPeticionASolicitudHomologacion(SolicitudHomologacionDTOPeticion peticion);

    // Dominio → DTO Respuesta
    SolicitudHomologacionDTORespuesta mappearDeSolicitudHomologacionASolicitudHomologacionDTORespuesta(SolicitudHomologacion solicitudHomologacion);

    List<SolicitudHomologacionDTORespuesta> mappearListaDeSolicitudHomologacionARespuesta(List<SolicitudHomologacion> solicitudes);

    List<SolicitudHomologacion> mappearListaDeSolicitudHomologacionDTOPeticionAListaDeSolicitudHomologacion(List<SolicitudHomologacionDTOPeticion> solicitudes);
}
