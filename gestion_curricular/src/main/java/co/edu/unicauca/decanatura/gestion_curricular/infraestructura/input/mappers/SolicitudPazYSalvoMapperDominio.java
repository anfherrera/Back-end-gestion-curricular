package co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.mappers;

import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.UsuarioDTOPeticion;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.Usuario;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import co.edu.unicauca.decanatura.gestion_curricular.dominio.modelos.SolicitudPazYSalvo;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.peticion.SolicitudPazYSalvoDTOPeticion;
import co.edu.unicauca.decanatura.gestion_curricular.infraestructura.input.dto.respuesta.SolicitudPazYSalvoDTORespuesta;

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
    @Mapping(source = "estadosSolicitud", target = "estadosSolicitud") // CORREGIDO
    @Mapping(source = "objUsuario", target = "objUsuario")
    @Mapping(source = "documentos", target = "documentos") 
    SolicitudPazYSalvoDTORespuesta mapearDeSolicitudARespuesta(SolicitudPazYSalvo solicitud);

    List<SolicitudPazYSalvoDTORespuesta> mapearListaDeSolicitudesARespuesta(List<SolicitudPazYSalvo> solicitudes);

    // DTO Petición → Modelo
    @Mapping(target = "estadosSolicitud", ignore = true) 
    @Mapping(target = "objCursoOfertadoVerano", ignore = true) 
    @Mapping(target = "documentos", ignore = true)
    @Mapping(target = "fecha_ceremonia", ignore = true) // Se asigna desde el caso de uso si es necesario
    SolicitudPazYSalvo mapearDeSolicitudDTOPeticionASolicitud(SolicitudPazYSalvoDTOPeticion peticion);

    // OPC-2: el JSON conserva id_usuario, nombre_completo y estado_usuario
    @Mapping(target = "idUsuario", source = "id_usuario")
    @Mapping(target = "nombreCompleto", source = "nombre_completo")
    @Mapping(target = "estadoUsuario", source = "estado_usuario")
    Usuario usuarioDTOPeticionToUsuario(UsuarioDTOPeticion usuarioDTOPeticion);
}
