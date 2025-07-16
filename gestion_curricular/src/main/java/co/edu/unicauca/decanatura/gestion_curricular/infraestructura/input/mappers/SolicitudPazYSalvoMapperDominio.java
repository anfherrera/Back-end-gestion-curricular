package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Solicitud;
import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudPazYSalvoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudPazYSalvoDTORespuesta;

@Mapper(
    componentModel = "spring",
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
    @Mapping(source = "fecha_registro_solicitud", target = "fecha_registro_solicitud")
    @Mapping(source = "estadosSolicitud", target = "estadosSolicitud")
    @Mapping(source = "objUsuario", target = "objUsuario")
    @Mapping(source = "documentos", target = "documentos")
    SolicitudPazYSalvoDTORespuesta mappearDeSolicitudARespuesta(SolicitudPazYSalvo solicitud);

    List<SolicitudPazYSalvoDTORespuesta> mappearListaDeSolicitudesARespuesta(List<SolicitudPazYSalvo> solicitudes);

    // DTO Petición → Modelo
    @Mapping(target = "estadosSolicitud", ignore = true) // No viene en el DTO de petición
    SolicitudPazYSalvo mappearDeSolicitudDTOPeticionASolicitud(SolicitudPazYSalvoDTOPeticion peticion);

    List<SolicitudPazYSalvo> mappearListaDeSolicitudDTOPeticionAListaSolicitud(List<SolicitudPazYSalvoDTOPeticion> peticiones); 
}
