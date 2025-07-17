package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudEcaes;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTOPeticion.SolicitudEcaesDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudDTORespuesta;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.DTORespuesta.SolicitudEcaesDTORespuesta;

@Mapper(
    componentModel = "spring",
    uses = {
        EstadoSolicitudMapperDominio.class,
        UsuarioMapperDominio.class,
        DocumentosMapperDominio.class
    }
)
public interface SolicitudEcaesMapperDominio {
   // DTO de petición → Dominio
    @Mapping(target = "estadosSolicitud", ignore = true) // Lo controlas desde el caso de uso
    SolicitudEcaes mappearDeSolicitudEcaesDTOPeticionASolicitudEcaes(SolicitudEcaesDTOPeticion peticion);

    List<SolicitudEcaes> mappearListaDeSolicitudEcaesDTOPeticionAListaDeSolicitudEcaes(List<SolicitudEcaesDTOPeticion> solicitudes);

    // Dominio → DTO Respuesta
    @Mapping(source = "id_solicitud", target = "id_solicitud")
    @Mapping(source = "nombre_solicitud", target = "nombre_solicitud")
    @Mapping(source = "fecha_registro_solicitud", target = "fecha_registro_solicitud")
    @Mapping(source = "estadosSolicitud", target = "estadosSolicitud")
    @Mapping(source = "objUsuario", target = "objUsuario")
    @Mapping(source = "documentos", target = "documentos")
    SolicitudEcaesDTORespuesta mappearDeSolicitudEcaesARespuesta(SolicitudEcaes solicitudEcaes);

    List<SolicitudEcaesDTORespuesta> mappearListaDeSolicitudEcaesARespuesta(List<SolicitudEcaes> solicitudes);

}
